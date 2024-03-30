package com.hmdp;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootTest
class HmDianPingApplicationTests {

    @Test
    public void getSnowId() throws UnknownHostException {
        long l = new Sequence(InetAddress.getLocalHost()).nextId();
        System.out.println(l);
    }


}
