package com.heima.hashedWheelTimer;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 订单取消任务
 * </pre>
 *
 * @author loopy_y
 * @since 2022/6/14
 */
@Slf4j
public class OrderCancelTimerTask implements TimerTask {

    private String orderId;

    public OrderCancelTimerTask(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        log.info("==== 订单【{}】取消操作执行中 ====", orderId);
        // TODO 以下省略具体业务实现
    }

}
