package com.heima.delayqueue;

public class Order {

    private String orderNo;

    private String money;

    public Order(String orderNo, String money) {
        this.orderNo = orderNo;
        this.money = money;
    }

    public String getOrderNo() {
        return orderNo;
    }
}
