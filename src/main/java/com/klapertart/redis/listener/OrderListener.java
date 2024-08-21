package com.klapertart.redis.listener;

import com.klapertart.redis.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

/**
 * @author klapertart
 * @since 8/21/2024
 */

@Component
@Slf4j
public class OrderListener implements StreamListener<String, ObjectRecord<String, Order>> {
    @Override
    public void onMessage(ObjectRecord<String, Order> message) {
        Order order = message.getValue();
        log.info("Receive message : {}", order);
    }
}
