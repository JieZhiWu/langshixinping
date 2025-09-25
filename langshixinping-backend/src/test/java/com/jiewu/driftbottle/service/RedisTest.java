package com.jiewu.driftbottle.service;

import com.jiewu.driftbottle.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    //
    @Resource
    private RedisTemplate redisTemplate;
    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("jiewuString","dog");
        valueOperations.set("jiewuInt",1);
        valueOperations.set("jiewuDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("jiewu");
        valueOperations.set("jiewuUser",user);
        //查
        Object jiewu = valueOperations.get("jiewuString");
        Assertions.assertTrue("dog".equals((String)jiewu));
        jiewu = valueOperations.get("jiewuInt");
        Assertions.assertTrue(1==((Integer)jiewu));
        jiewu = valueOperations.get("jiewuDouble");
        Assertions.assertTrue(2.0==((Double)jiewu));
        System.out.println(valueOperations.get("jiewuUser"));
//        valueOperations.set("jiewuString","dog");
//        redisTemplate.delete("jiewuString");
    }
}