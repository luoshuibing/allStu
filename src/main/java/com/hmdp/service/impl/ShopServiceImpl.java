package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    @Override
    public Result queryById(Long id) throws InterruptedException {
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    /**
     * 缓存击穿，热点key，过期时间
     *
     * @param id
     * @return
     * @throws InterruptedException
     */
    private Shop queryWithLogicExpire(Long id) throws InterruptedException {
        String key = getShopId(CACHE_SHOP_KEY, id);
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isEmpty(shopJson)) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            return shop;
        }
        String lockKey = getShopId(RedisConstants.LOCK_SHOP_KEY, id);
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }
        return shop;
    }

    /**
     * 缓存击穿，热点key处理，分布式锁方案
     *
     * @param id
     * @return
     * @throws InterruptedException
     */
    private Shop queryWithMutex(Long id) throws InterruptedException {
        String key = getShopId(CACHE_SHOP_KEY, id);
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotEmpty(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        if (shopJson != null) {
            return null;
        }
        Shop shop = null;
        try {
            boolean isLock = tryLock(RedisConstants.LOCK_SHOP_KEY + id);
            /**
             * 自己加入自旋锁
             */
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            shop = getById(id);
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unlock(RedisConstants.LOCK_SHOP_KEY + id);
        }
        return shop;
    }

    /**
     * 缓存穿透解决方案，缓存空数据
     *
     * @param id
     * @return
     */
    private Shop queryWithCachePassThrough(Long id) {
        String key = getShopId(CACHE_SHOP_KEY, id);
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotEmpty(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        if (shopJson != null) {
            return null;
        }
        Shop shop = getById(id);
        if (shop == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    private static String getShopId(String cacheShopKey, Long id) {
        return cacheShopKey + id;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private boolean unlock(String key) {
        Boolean flag = stringRedisTemplate.delete(key);
        return BooleanUtil.isTrue(flag);
    }

    public void saveShop2Redis(Long id, Long expireSeconds) {
        Shop shop = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(getShopId(CACHE_SHOP_KEY, id), JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺ID不能为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(getShopId(CACHE_SHOP_KEY, shop.getId()));
        return Result.ok();
    }
}
