package com.heima.delayqueue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayItem<T> implements Delayed {

    // 纳秒值
    private long delayTime;

    private T t;

    public DelayItem(long delayTime, T t) {
        this.delayTime = TimeUnit.NANOSECONDS.convert(delayTime, TimeUnit.MILLISECONDS) + System.nanoTime();
        this.t = t;
    }

    public T getT() {
        return t;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delayTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long result = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return result == 0 ? 0 : (result > 0 ? 1 : -1);
    }


}
