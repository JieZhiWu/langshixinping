package com.jiewu.driftbottle;



import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 */
@SpringBootApplication
@MapperScan("com.jiewu.driftbottle.mapper")
@EnableScheduling   // 开启定时任务
public class DriftBottleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriftBottleApplication.class, args);
    }

}

