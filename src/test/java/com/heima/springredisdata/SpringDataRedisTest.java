package com.heima.springredisdata;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.heima.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testString() {
        redisTemplate.opsForValue().set("四代火影", "波风水门");
        Object result = redisTemplate.opsForValue().get("四代火影");
        System.out.println("value:=" + result);
    }

    @Test
    void testSaveUser() {
        redisTemplate.opsForValue().set("user:100", new User("宇智波佐助", 20));
        //获取数据
        Object result = redisTemplate.opsForValue().get("user:100");
        System.out.println("value=" + result);
    }

    @Test
    void testStringSaveUser() throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set("user:101", mapper.writeValueAsString(new User("漩涡鸣人", 20)));
        //获取数据
        String result = stringRedisTemplate.opsForValue().get("user:101");

        User user = mapper.readValue(result, User.class);
        System.out.println("value=" + user);
    }

    @Test
    void testHash() {
        stringRedisTemplate.opsForHash().put("user:102", "name", "小樱");
        stringRedisTemplate.opsForHash().put("user:102", "age", "20");
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries("user:102");
        System.out.println("entries=" + entries);
    }


}
