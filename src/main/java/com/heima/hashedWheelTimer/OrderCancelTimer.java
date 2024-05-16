package com.heima.hashedWheelTimer;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * 时间管理队列工具
 * </pre>
 *
 * @author yjj
 * @since 2022/6/14
 */
@Slf4j
public class OrderCancelTimer {

    private static HashedWheelTimer timer;
    /**
     * 延迟执行时间，可配置在配置文件中，这里为了方便测试设置为20s
     */
    private static long delay = 20L;
    /**
     * 存储Timeout对象，建立订单id与Timeout关系，用于通过订单id找到Timeout从队列中移除
     */
    private static Map<String, Timeout> timeoutMap = new HashMap<String, Timeout>();

    static {
        // 创建延迟队列实例，可以设置时间轮长度及刻度等，这里直接使用默认的
        timer = new HashedWheelTimer();
    }

    /**
     * 将任务添加进队列
     * @param timerTask 任务
     * @param orderId 订单id
     */
    public static void addNewTimeout(TimerTask timerTask, String orderId) {
        log.info("订单号【{}】准备添加进延迟队列", orderId);
        Timeout timeout = timer.newTimeout(timerTask, delay, TimeUnit.SECONDS);
        timeoutMap.put(orderId, timeout);
        log.info("订单号【{}】添加进延迟队列成功", orderId);
    }

    /**
     * 将任务从队列中移除
     * @param orderId 订单id （本示例中是通过订单id关联的Timeout对象，所以移除时需要根据此字段查询到Timeout对象）
     * @return 是/否成功移除
     */
    public static boolean delTimeout(String orderId) {
        log.info("订单号【{}】准备从延迟队列中移除", orderId);
        Timeout timeout = timeoutMap.get(orderId);
        boolean cancel = timeout.cancel();
        log.info("订单号【{}】从延迟队列中移除结果为：【{}】", orderId, cancel);
        if (cancel) {
            // 任务从队列中移除成功后，移除订单id和Timeout的关联关系
            timeoutMap.remove(orderId);
        }
        return cancel;
    }
}

