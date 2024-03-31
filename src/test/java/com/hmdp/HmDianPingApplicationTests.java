package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Test
    public void getSnowId() throws UnknownHostException {
        long l = new Sequence(InetAddress.getLocalHost()).nextId();
        System.out.println(l);
    }

    @Test
    public void generateToken() throws Exception {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("token.txt"), "UTF-8"));
        List<User> list = userService.list();
        for (User user : list) {
            String token = UUID.randomUUID().toString(true);
            String key = LOGIN_USER_KEY + token;
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
            stringRedisTemplate.opsForHash().putAll(key, userMap);
            bufferedWriter.write(token);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        bufferedWriter.close();


    }


}
