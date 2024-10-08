package com.klapertart.redis;

import com.klapertart.redis.model.Product;
import com.klapertart.redis.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.data.redis.support.collections.RedisSet;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author klapertart
 * @since 12/20/2023
 */

@SpringBootTest
@Slf4j
public class RedisTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductRepository repository;

    @Test
    void testString() {
        Assertions.assertNotNull(redisTemplate);
    }

    @Test
    void testValue() throws InterruptedException {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();

        operations.set("name", "HAMKA", Duration.ofSeconds(2));
        Assertions.assertEquals("HAMKA", operations.get("name"));

        Thread.sleep(3_000L);

        Assertions.assertNull(operations.get("name"));
    }

    @Test
    void testList() {
        ListOperations<String, String> operations = redisTemplate.opsForList();
        operations.rightPush("keylist", "abdillah");
        operations.rightPush("keylist", "hamka");

        Assertions.assertEquals("abdillah", operations.leftPop("keylist"));
        Assertions.assertEquals("hamka", operations.leftPop("keylist"));
    }

    @Test
    void testSet() {
        SetOperations<String, String> operations = redisTemplate.opsForSet();
        operations.add("students", "abdillah");
        operations.add("students", "abdillah");
        operations.add("students", "hamka");
        operations.add("students", "hamka");


        Set<String> students = operations.members("students");

        Assertions.assertEquals(2, students.size());
        assertThat(students, hasItems("abdillah", "hamka"));
    }

    @Test
    void testZSet() {
        ZSetOperations<String, String> operations = redisTemplate.opsForZSet();
        operations.add("score", "abdillah", 200);
        operations.add("score", "hamka", 150);
        operations.add("score", "amrullah", 100);

        Assertions.assertEquals("abdillah", operations.popMax("score").getValue());
        Assertions.assertEquals("hamka", operations.popMax("score").getValue());
        Assertions.assertEquals("amrullah", operations.popMax("score").getValue());
    }

    @Test
    void testHash() {
        HashOperations<String, Object, Object> operations = redisTemplate.opsForHash();

        // cara 1
//        operations.put("user:1", "id", "1");
//        operations.put("user:1", "name", "hamka");
//        operations.put("user:1", "email", "hamka@gmail.com");

        // cara 2
        Map<String, String> map = new HashMap<>();
        map.put("id", "1");
        map.put("name", "hamka");
        map.put("email", "hamka@gmail.com");
        operations.putAll("user:1", map);

        Assertions.assertEquals("1", operations.get("user:1", "id"));
        Assertions.assertEquals("hamka", operations.get("user:1", "name"));
        Assertions.assertEquals("hamka@gmail.com", operations.get("user:1", "email"));

        redisTemplate.delete("user:1");
    }

    @Test
    void testHyperLogLog() {
        HyperLogLogOperations<String, String> operations = redisTemplate.opsForHyperLogLog();
        operations.add("traffics", "abdillah", "hamka", "hamza");
        operations.add("traffics", "hana", "hamka", "abdillah");

        Assertions.assertEquals(4L, operations.size("traffics"));
    }

    @Test
    void testTransaction() {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                operations.opsForValue().set("test1", "abdillah", Duration.ofSeconds(2));
                operations.opsForValue().set("test2", "hamka", Duration.ofSeconds(2));

                operations.exec();
                return null;
            }
        });

        Assertions.assertEquals("abdillah", redisTemplate.opsForValue().get("test1"));
        Assertions.assertEquals("hamka", redisTemplate.opsForValue().get("test2"));

    }

    @Test
    void testPipeline() {
        List<Object> statuses = redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.opsForValue().set("test", "abdillah", Duration.ofSeconds(2));
                operations.opsForValue().set("test", "hamka", Duration.ofSeconds(2));
                operations.opsForValue().set("test", "hamza", Duration.ofSeconds(2));

                return null;
            }
        });

        assertThat(statuses, hasSize(3));
        assertThat(statuses, hasItem(true));
        assertThat(statuses, not(hasItem(false)));
    }

    @Test
    void testStream() {
        StreamOperations<String, Object, Object> operations = redisTemplate.opsForStream();

        MapRecord<String, String, String> record = MapRecord.create("stream-1", Map.of(
                "name", "abdillah hamka hamza",
                "address", "bandung"
        ));

        // save 10 data to stream [stream-1]
        for (int i = 0; i < 10; i++) {
            operations.add(record);
        }
    }

    @Test
    void testStreamSubscribe() {
        StreamOperations<String, Object, Object> operations = redisTemplate.opsForStream();

        try {
            operations.createGroup("stream-1", "sample-group");
        }catch (RedisSystemException e){
            // group already exists
        }

        List<MapRecord<String, Object, Object>> records = operations.read(Consumer.from("sample-group", "stream-1"), StreamOffset.create("stream-1", ReadOffset.lastConsumed()));

        for (MapRecord<String, Object, Object> record: records){
            System.out.println(record);
        }
    }

    @Test
    void testPubSub() {
        // subscribe
        redisTemplate.getConnectionFactory().getConnection().subscribe(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                var event = new String(message.getBody());
                log.info("EVENT: {}", event);
            }
        }, "my-channel".getBytes());

        // publisher
        for (int i = 0; i < 10; i++) {
            redisTemplate.convertAndSend("my-channel", "Hello world - " + i);
        }
    }

    @Test
    void testRedisList() {
        // set data in redis
        List<String> list = RedisList.create("kids", redisTemplate);
        list.add("abdillah");
        list.add("hamka");

        // get data from redis
        List<String> names = redisTemplate.opsForList().range("kids", 0, -1);
        assertThat(names, hasItems("abdillah", "hamka"));
    }

    @Test
    void testRedisSet() {
        Set<String> set = RedisSet.create("birds", redisTemplate);
        set.addAll(Set.of("Elang", "Garuda", "Rajawali"));
        set.addAll(Set.of("Nuri", "Elang", "Gagak"));

        Assertions.assertEquals(5, set.size());

        Set<String> birds = redisTemplate.opsForSet().members("birds");
        assertThat(birds, hasItems("Elang", "Garuda", "Rajawali", "Nuri", "Gagak"));
    }

    @Test
    void testRedisMap() {
        Map<String, String> map = new DefaultRedisMap<>("employee:1", redisTemplate);
        map.put("name", "hamka");
        map.put("address", "indonesia");
        assertThat(map, hasEntry("name", "hamka"));

        Map<Object, Object> entries = redisTemplate.opsForHash().entries("employee:1");
        assertThat(entries, hasEntry("name", "hamka"));
    }

    @Test
    void testRepository() {
        Product product = Product.builder()
                .id("1")
                .name("Keyboard")
                .price(10_000_000L)
                .build();

        repository.save(product);

        Map<Object, Object> map = redisTemplate.opsForHash().entries("products:1");
        Assertions.assertEquals(product.getName(), map.get("name"));
        Assertions.assertEquals(product.getPrice().toString(), map.get("price"));


        Product product2 = repository.findById("1").get();
        Assertions.assertEquals(product, product2);
    }

    @Test
    void testTtl() throws InterruptedException {
        Product product = Product.builder()
                .id("1")
                .name("Keyboard")
                .price(10_000_000L)
                .ttl(3L)
                .build();

        repository.save(product);

        Assertions.assertTrue(repository.findById("1").isPresent());

        Thread.sleep(Duration.ofSeconds(5L).toMillis());
        Assertions.assertFalse(repository.findById("1").isPresent());
    }
}
