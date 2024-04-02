package com.heima.item.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.item.pojo.Item;
import com.heima.item.pojo.ItemStock;
import com.heima.item.service.IItemService;
import com.heima.item.service.IItemStockService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class RedisHandler implements InitializingBean {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IItemService itemService;

    @Autowired
    private IItemStockService itemStockService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<Item> itemList = itemService.list();
        for (Item item : itemList) {
            String str = MAPPER.writeValueAsString(item);
            stringRedisTemplate.opsForValue().set("item:id:" + item.getId(), str);
        }
        List<ItemStock> stockList = itemStockService.list();
        for (ItemStock itemStock : stockList) {
            String str = MAPPER.writeValueAsString(itemStock);
            stringRedisTemplate.opsForValue().set("stock:id:" + itemStock.getId(), str);
        }
    }

    public void saveItem(Item item){
        try {
            stringRedisTemplate.opsForValue().set("item:id:"+item.getId(),MAPPER.writeValueAsString(item));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delItem(Item item){
        stringRedisTemplate.delete("item:id:" + item.getId());
    }





















}
