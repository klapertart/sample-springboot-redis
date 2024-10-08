package com.klapertart.redis.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author klapertart
 * @since 8/21/2024
 */

@Component
public class CustomerPublisher {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void publish(){
        redisTemplate.convertAndSend("customers", "Hamka " + UUID.randomUUID().toString());
    }
}
