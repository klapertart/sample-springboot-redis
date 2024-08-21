package com.klapertart.redis.publisher;

import com.klapertart.redis.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author tritronik
 * @since 8/21/2024
 */

@Component
public class OrderPublisher {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    @PostConstruct
    public void run(){
        Order order = new Order(UUID.randomUUID().toString(), 1000L);
        ObjectRecord<String, Order> record = ObjectRecord.create("orders", order);
        redisTemplate.opsForStream().add(record);
    }
}
