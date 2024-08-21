package com.klapertart.redis.config;

import com.klapertart.redis.listener.OrderListener;
import com.klapertart.redis.model.Order;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

/**
 * @author tritronik
 * @since 8/21/2024
 */

@Configuration
@Slf4j
public class RedisConfig {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Bean
    public Subscription orderSubscription(StreamMessageListenerContainer<String, ObjectRecord<String, Order>> orderContainer, OrderListener orderListener){
        try {
            redisTemplate.opsForStream().createGroup("orders", "my-group");
        } catch (RedisSystemException e) {
            if (e.getRootCause() instanceof RedisCommandExecutionException
                    && e.getRootCause().getMessage().contains("BUSYGROUP")) {
                log.warn("Group already exists: {}", e.getMessage());
            } else {
                throw e; // Rethrow if it's another exception
            }
        }

        var offset = StreamOffset.create("orders", ReadOffset.lastConsumed());
        var consumer = Consumer.from("my-group","consumer-1");
        var readRequest = StreamMessageListenerContainer.StreamReadRequest.builder(offset)
                .consumer(consumer)
                .autoAcknowledge(true)
                .cancelOnError(throwable -> false)
                .errorHandler(throwable -> log.error(throwable.getMessage()))
                .build();

        return orderContainer.register(readRequest, orderListener);
    }

    @Bean(destroyMethod = "stop", initMethod = "start")
    public StreamMessageListenerContainer<String, ObjectRecord<String, Order>> orderContainer(RedisConnectionFactory connectionFactory){
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(5))
                .targetType(Order.class)
                .build();

        return StreamMessageListenerContainer.create(connectionFactory, options);
    }
}
