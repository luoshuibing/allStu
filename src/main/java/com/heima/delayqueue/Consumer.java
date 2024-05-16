package com.heima.delayqueue;

import java.util.concurrent.DelayQueue;

public class Consumer implements Runnable {

    private DelayQueue<DelayItem<Order>> delayQueue;

    public Consumer(DelayQueue<DelayItem<Order>> delayQueue) {
        this.delayQueue = delayQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DelayItem<Order> delayItem = delayQueue.take();
                Order order = delayItem.getT();
                System.out.println("获取到订单ID为" + order.getOrderNo());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
