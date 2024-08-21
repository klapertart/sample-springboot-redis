package com.klapertart.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tritronik
 * @since 8/21/2024
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private Long amount;
}
