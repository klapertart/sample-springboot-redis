package com.klapertart.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRedisRepositories
public class BelajarSpringRedisApplication {

	public static void main(String[] args) {
		SpringApplication.run(BelajarSpringRedisApplication.class, args);
	}

}
