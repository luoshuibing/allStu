package com.hmdp.utils;

public class SnowflakeIdGenerator {
    // 起始时间戳，可以根据实际情况设置一个合适的值
    private static final long START_TIMESTAMP = 1609459200000L; // 2021-01-01 00:00:00

    // 每部分所占的位数
    private static final long SEQUENCE_BIT = 12; // 序列号位数
    private static final long MACHINE_BIT = 5;   // 机器标识位数
    private static final long DATA_CENTER_BIT = 5; // 数据中心标识位数

    // 每部分的最大值
    private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private static final long MAX_DATA_CENTER_NUM = -1L ^ (-1L << DATA_CENTER_BIT);

    // 每部分向左的位移
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;

    private long dataCenterId; // 数据中心ID
    private long machineId;    // 机器ID
    private long sequence = 0L; // 序列号
    private long lastTimestamp = -1L; // 上次生成ID的时间戳

    public SnowflakeIdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException("Data center ID can't be greater than " + MAX_DATA_CENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("Machine ID can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    // 生成唯一ID
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT) | (dataCenterId << DATA_CENTER_LEFT) | (machineId << MACHINE_LEFT) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);
        for (int i = 0; i < 10; i++) {
            long id = idGenerator.nextId();
            System.out.println("Generated ID: " + id);
        }
    }
}
