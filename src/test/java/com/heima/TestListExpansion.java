package com.heima;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestListExpansion {

    @Test
    public void testList1() {
        //18
        List<Integer> list = new ArrayList<Integer>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            list.add(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("消耗时间：" + (end - start));
    }

    @Test
    public void testList2() {
        List<Integer> list = new ArrayList<Integer>(100000000);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            list.add(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("消耗时间：" + (end - start));
    }

    @Test
    public void test3(){
        for (int i = 0; i < 100000000; i++) {
            if((i%3)==(i&3)){

            }else{
                System.out.println(i);
                System.out.println("ERROR");
            }

            if((i&3)>3){
                System.out.println(i);
                System.out.println("===================================");
            }
        }
    }

    @Test
    public void test4(){
        int i = 543439 & 3;
        System.out.println(i);
    }


}
