package com.hmdp.service.impl;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
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

    private static DefaultRedisScript<Long> SECKILL_SCRIPT;

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    private ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private IVoucherOrderService iVoucherOrderService;

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {

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
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
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
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId + "", UserHolder.getUser().getId() + "");
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


