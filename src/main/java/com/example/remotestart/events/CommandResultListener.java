package com.example.remotestart.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CommandResultListener {

    private final ObjectMapper objectMapper;
    private final CommandResultHandler handler;

    public CommandResultListener(ObjectMapper objectMapper, CommandResultHandler handler) {
        this.objectMapper = objectMapper;
        this.handler = handler;
    }

    @KafkaListener(topics = "${app.topics.command-results}")
    public void onMessage(ConsumerRecord<String, String> record) throws JsonProcessingException {
        handler.handle(objectMapper.readValue(record.value(), CommandResultEvent.class));
    }
}
