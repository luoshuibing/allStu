package com.heima.jedis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * 火影忍者
 */
public class JedisTest {

    private Jedis jedis;

    @BeforeEach
    void setup() {
//        jedis = new Jedis("192.168.88.102");
        jedis = JedisConnectionPool.getJedis();
        jedis.auth("123456");
        jedis.select(0);
    }

    @Test
    void testString() {
        String result = jedis.set("九尾", "鸣人");
        System.out.println("result=" + result);
        String name = jedis.get("九尾");
        System.out.println("name=" + name);
    }

    @Test
    void testHash() {
        jedis.hset("忍刀七人众","断刀","再不斩");
        jedis.hset("忍刀七人众","大刀","鬼鲛");
        jedis.hset("忍刀七人众","雷刀","雷牙");
        jedis.hset("忍刀七人众","双刀","满月");
        jedis.hset("忍刀七人众","长刀","满月");
        jedis.hset("忍刀七人众","钝刀","满月");
        jedis.hset("忍刀七人众","爆刀","满月");
        Map<String, String> map = jedis.hgetAll("忍刀七人众");
        System.out.println(map);
    }

    @AfterEach
    void tearDown() {
        if (jedis != null) {
            jedis.close();
        }
    }


}
