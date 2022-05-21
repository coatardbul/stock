package com.coatardbul.stock.service.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2021/12/20
 *
 * @author Su Xiaolei
 */
@Service
public class RedisService  {
    @Autowired
    RedisTemplate redisTemplate;

    public void set() {
        redisTemplate.opsForValue().set("hel222lo","value",12, TimeUnit.MINUTES);

        Object hello = redisTemplate.opsForValue().get("hello");

        int i=1;
    }
}
