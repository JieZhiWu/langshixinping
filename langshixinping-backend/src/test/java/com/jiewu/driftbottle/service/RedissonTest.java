package com.jiewu.driftbottle.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list，数据存在本地 JVM 内存中
        List<String> list = new ArrayList<>();
        list.add("jiewu");
        System.out.println("list:" + list.get(0));
        // list.remove(0);

        // 数据存在 redis 的内存中
        RList<Object> rList = redissonClient.getList("test-list");
        rList.add("jiewu");
        System.out.println("rlist:" + rList.get(0));
        // rList.remove(0);

        // map
        Map<String, Integer> map = new HashMap<>();
        map.put("jiewu", 10);
        map.get("jiewu");
//        map.remove("jiewu");

        RMap<Object, Object> rMap = redissonClient.getMap("test-map");
    }
}