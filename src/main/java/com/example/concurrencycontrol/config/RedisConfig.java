package com.example.concurrencycontrol.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisLettuceConnectionFactory(){
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    // serializer 설정으로 redis-cli를 통해 직접 데이터를 조회할 수 있도록 함
    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisLettuceConnectionFactory());

        return redisTemplate;
    }

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

//    @Bean
//    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient){
//        return new RedissonConnectionFactory(redissonClient);
//    }

    @Bean
    public DefaultRedisScript<Long> script() {
        String luaScript =
                "local userKey = KEYS[1] " +
                        "local countKey = KEYS[2] " +
                        "local userInfoKey = KEYS[3] " +

                        "local userId = ARGV[1] " +
                        "local maxTickets = tonumber(ARGV[2]) " +
                        "local eventId = ARGV[3] " +
                        "local name = ARGV[4] " +
                        "local phone = ARGV[5] " +

                        // 사용자 중복 체크
                        "if redis.call('SADD', userKey, userId) == 0 then " +
                        "    return 0 " + // 이미 신청한 사용자
                        "end " +

                        // 티켓 수량 증가 및 초과 여부 확인
                        "local currentCount = redis.call('INCR', countKey) " +
                        "if currentCount > maxTickets then " +
                        "    redis.call('DECR', countKey) " + // 수량 복구
                        "    redis.call('SREM', userKey, userId) " + // 사용자 제거
                        "    return -1 " + // 티켓 초과
                        "end " +

                        // 사용자 정보 저장
                        "redis.call('HSET', userInfoKey, 'userId', userId, 'eventId', eventId, 'name', name, 'phone', phone) " +
                        "return 1"; // 티켓 신청 성공

        return new DefaultRedisScript<>(luaScript, Long.class);
    }

}
