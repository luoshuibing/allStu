package com.heima.delayqueue;

import java.util.concurrent.DelayQueue;

public class Producer implements Runnable{

    private DelayQueue<DelayItem<Order>> delayQueue;

    public Producer(DelayQueue<DelayItem<Order>> delayQueue) {
        this.delayQueue = delayQueue;
    }

    @Override
    public void run() {
        Order order1 = new Order("o_1", "200");
        DelayItem<Order> delayItem1 = new DelayItem<Order>(5000,order1);
        delayQueue.add(delayItem1);

        Order order2 = new Order("o_2", "400");
        DelayItem<Order> delayItem2 = new DelayItem<Order>(10000,order2);
        delayQueue.add(delayItem2);
    }
}
