package com.heima.delayqueue;

import com.heima.delayqueue.Consumer;
import com.heima.delayqueue.DelayItem;
import com.heima.delayqueue.Order;
import com.heima.delayqueue.Producer;

import java.util.concurrent.DelayQueue;

public class DelayQueueTest {

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<DelayItem<Order>> delayqueue = new DelayQueue<DelayItem<Order>>();

        new Thread(new Producer(delayqueue)).start();
        new Thread(new Consumer(delayqueue)).start();
        for (int i = 1; i < 15; i++) {
            Thread.sleep(1000);
            System.out.println("经过"+i*1000+"毫秒");
        }
    }

}
