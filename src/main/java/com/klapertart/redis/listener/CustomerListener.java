package com.klapertart.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author klapertart
 * @since 8/21/2024
 */

@Slf4j
@Component
public class CustomerListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        var event = new String(message.getBody());
        log.info("Receive message : {}", event);
    }
}
