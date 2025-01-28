package ru.arsentiev.producer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.arsentiev.producer.entity.Item;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Item> kafkaTemplate;
    private final String topicName;

    public KafkaProducerService(
            KafkaTemplate<String, Item> kafkaTemplate,
            @Value("${producer.kafka.topic}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void sendItem(Item item) {
        kafkaTemplate.send(topicName, String.valueOf(item.getOwner().getEmail()), item);
    }
}
