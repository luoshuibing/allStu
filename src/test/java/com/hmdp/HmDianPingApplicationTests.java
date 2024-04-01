package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.service.IShopService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Resource
    private IShopService shopService;

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

    /**
     * 生成店铺的geo缓存
     */
    @Test
    public void loadShopData() {
        String key = RedisConstants.SHOP_GEO_KEY;
        List<Shop> list = shopService.list();
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            Long typeId = entry.getKey();
            List<Shop> shops = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> geos = new ArrayList<>(shops.size());
            for (Shop shop : shops) {
                RedisGeoCommands.GeoLocation geoLocation = new RedisGeoCommands.GeoLocation(shop.getId().toString(), new Point(shop.getX(), shop.getY()));
                geos.add(geoLocation);
//                stringRedisTemplate.opsForGeo().add(key + typeId, new Point(shop.getX(), shop.getY()), shop.getId().toString());
            }
            stringRedisTemplate.opsForGeo().add(key + typeId, geos);
        }
    }

    @Test
    public void testHyperLoglog() {
        String[] users = new String[1000];
        int j = 0;
        for (int i = 0; i < 1000000; i++) {
            j = i % 1000;
            users[j] = "user_" + i;
            if (j == 999) {
                stringRedisTemplate.opsForHyperLogLog().add("hl2", users);
            }
        }
        Long count = stringRedisTemplate.opsForHyperLogLog().size("hl2");
        System.out.println("count:" + count);
    }

    @Test
    public void testSetHyperLoglog() {
        String[] users = new String[1000];
        int j;
        for (int i = 0; i < 1000000; i++) {
            j = i % 1000;
            users[j] = "user_" + i;
            if (j == 999) {
                stringRedisTemplate.opsForSet().add("hl3", users);
            }
        }
        Long count = stringRedisTemplate.opsForSet().size("hl3");
        System.out.println("count:" + count);
    }


}
