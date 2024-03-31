package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static DefaultRedisScript<Long> SECKILL_SCRIPT_V1;

    private static DefaultRedisScript<Long> SECKILL_SCRIPT_V2;

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    private ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private IVoucherOrderService iVoucherOrderService;

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandlerV2());
    }

    private class VoucherOrderHandlerV2 implements Runnable {

        String queueName = "stream.orders";

        @Override
        public void run() {
            while (true) {
                try {
                    //获取消息队列中的订单信息  XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STEAMS stream.orders >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(Consumer.from("g1", "c1"), StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)), StreamOffset.create(queueName, ReadOffset.lastConsumed()));
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    handleVoucherOrder(voucherOrder);
                    //确认 SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单状态异常", e);
                    handlePendingList();


                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    //获取消息队列中的订单信息  XREADGROUP GROUP g1 c1 COUNT 1 STEAMS stream.orders 0,读取的是pending-list的数据信息
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(Consumer.from("g1", "c1"), StreamReadOptions.empty().count(1), StreamOffset.create(queueName, ReadOffset.from("0")));
                    if (list == null || list.isEmpty()) {
                        //没有异常消息
                        break;
                    }
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    handleVoucherOrder(voucherOrder);
                    //确认 SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理pending-list订单状态异常", e);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            RLock lock = redissonClient.getLock("lock:order:" + voucherOrder);
            try {
                boolean isLock = lock.tryLock(1000, 2000, TimeUnit.SECONDS);
                if (!isLock) {
                    log.error("不允许重复下单");
                    return;
                }
                iVoucherOrderService.createVoucherOrder(voucherOrder);
            } catch (Exception e) {
                //do nothing
                log.error("逻辑异常", e);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 从阻塞队列中拿消息
     */
    private class VoucherOrderHandlerV1 implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    VoucherOrder voucherOrder = orderTasks.take();
                    handleVoucherOrder(voucherOrder);
                } catch (InterruptedException e) {
                    log.error("处理订单状态异常", e);
                }
            }
        }

        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            RLock lock = redissonClient.getLock("lock:order:" + voucherOrder);
            try {
                boolean isLock = lock.tryLock(1000, 2000, TimeUnit.SECONDS);
                if (!isLock) {
                    log.error("不允许重复下单");
                    return;
                }
                iVoucherOrderService.createVoucherOrder(voucherOrder);
            } catch (Exception e) {
                //do nothing
                log.error("逻辑异常", e);
            } finally {
                lock.unlock();
            }
        }
    }

    static {
        SECKILL_SCRIPT_V1 = new DefaultRedisScript<>();
        SECKILL_SCRIPT_V1.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT_V1.setResultType(Long.class);

        SECKILL_SCRIPT_V2 = new DefaultRedisScript<>();
        SECKILL_SCRIPT_V2.setLocation(new ClassPathResource("seckill-stream.lua"));
        SECKILL_SCRIPT_V2.setResultType(Long.class);
    }


    /**
     * 超卖问题
     * 一人一单问题
     * 集群模式syschronized无效
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        long orderId = redisIdWorker.nextId("order");
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT_V2, Collections.emptyList(), voucherId.toString(), UserHolder.getUser().getId().toString(), String.valueOf(orderId));
        if (result != 0) {
            return Result.fail(result == 1 ? "库存不足" : "不能重复下单");
        }
        iVoucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
        return Result.ok(orderId);
    }

    /**
     * 通过阻塞队列完成异步秒杀任务
     *
     * @param voucherId
     * @return
     */
    private Result seckillVoucherByBlockedQueue(Long voucherId) {
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT_V1, Collections.emptyList(), voucherId.toString(), UserHolder.getUser().getId().toString());
        if (result != 0) {
            return Result.fail(result == 1 ? "库存不足" : "不能重复下单");
        }

        long orderId = redisIdWorker.nextId("order");
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdWorker.nextId("order"));
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(UserHolder.getUser().getId());
        orderTasks.add(voucherOrder);
        iVoucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
        return Result.ok(orderId);
    }

    /**
     * 通过Redisson完成秒杀
     *
     * @param voucherId
     * @return
     */
    private Result seckillVoucherV2Redisson(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束");
        }
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        UserDTO user = UserHolder.getUser();
        RLock lock = redissonClient.getLock("lock:order:" + user.getId());
        try {
            boolean isLock = lock.tryLock(1000, 2000, TimeUnit.SECONDS);
            if (!isLock) {
                return Result.fail("非法请求");
            }
            //获取代理对象
            IVoucherOrderService voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return voucherOrderService.createVoucherOrder(voucherId, seckillVoucher);
        } catch (Exception e) {
            //do nothing
            return Result.fail("逻辑异常");
        } finally {
            lock.unlock();
        }
    }


    /**
     * redis -简单版本实现
     *
     * @param voucherId
     * @return
     */
    private Result seckillVoucherRedisV1(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束");
        }
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        UserDTO user = UserHolder.getUser();
        SimpleRedisLock simpleRedisLock = new SimpleRedisLock(stringRedisTemplate, "order:" + user.getId());
        boolean isLock = simpleRedisLock.tryLock(2000);
        if (!isLock) {
            return Result.fail("非法请求");
        }
        try {
            //获取代理对象
            IVoucherOrderService voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return voucherOrderService.createVoucherOrder(voucherId, seckillVoucher);
        } finally {
            simpleRedisLock.unlock();
        }
    }

    /**
     * 单机超卖问题
     *
     * @param voucherId
     * @return
     */
    private Result seckillVoucherSynchronized(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束");
        }
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        UserDTO user = UserHolder.getUser();
        synchronized (user.getId().toString().intern()) {
            //获取代理对象
            IVoucherOrderService voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return voucherOrderService.createVoucherOrder(voucherId, seckillVoucher);
        }
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId, SeckillVoucher seckillVoucher) {
        UserDTO user = UserHolder.getUser();
        Integer count = query().eq("user_id", user.getId()).eq("voucher_id", seckillVoucher.getVoucherId()).count();
        if (count > 0) {
            return Result.fail("用户已经购买过了");
        }
        boolean result = seckillVoucherService.update().setSql("stock=stock-1").eq("voucher_id", seckillVoucher.getVoucherId()).gt("stock", 0).update();
        if (!result) {
            return Result.fail("库存不足");
        }
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdWorker.nextId("order"));
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(UserHolder.getUser().getId());
        save(voucherOrder);
        return Result.ok(voucherOrder.getId());
    }

    @Override
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Integer count = query().eq("user_id", voucherOrder.getUserId()).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            log.error("用户已经下单");
            return;
        }
        boolean result = seckillVoucherService.update().setSql("stock=stock-1").eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0).update();
        if (!result) {
            log.error("库存不足");
            return;
        }
        save(voucherOrder);
    }
}


