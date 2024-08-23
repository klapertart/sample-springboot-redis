package com.klapertart.redis.repository;

import com.klapertart.redis.model.Product;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

/**
 * @author tritronik
 * @since 8/23/2024
 */

@Repository
public interface ProductRepository extends KeyValueRepository<Product, String> {
}
