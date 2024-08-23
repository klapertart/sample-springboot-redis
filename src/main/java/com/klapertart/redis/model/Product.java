package com.klapertart.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

/**
 * @author klapertart
 * @since 8/23/2024
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@KeySpace("products")
public class Product {
    @Id
    private String id;

    private String name;

    private Long price;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long ttl = -1L;
}
