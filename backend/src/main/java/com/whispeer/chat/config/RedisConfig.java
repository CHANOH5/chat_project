package com.whispeer.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
// Redis에 대한 활성 연결을 처리하고 가져오는 데 필요한 인터페이스는 RedisConnection, RedisConnectionFactory임

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    // 어디로 접속할지
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
       return new LettuceConnectionFactory(host,port);
    } // redisConnectionFactory()

    // 어떤 직렬화쓸지
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        return template;
    } // stringRedisTemplate()

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Key는 문자열, Value는 JSON 직렬화
        var stringSerializer = new StringRedisSerializer();
        var jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);

        // Hash 자료구조에서도 직렬화 적용
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    } // redisTemplate
    
} // end class
