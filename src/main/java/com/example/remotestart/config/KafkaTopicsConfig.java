package com.example.remotestart.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic commandResultsTopic(@Value("${app.topics.command-results}") String name) {
        return TopicBuilder.name(name).partitions(6).replicas(1).build();
    }
}
