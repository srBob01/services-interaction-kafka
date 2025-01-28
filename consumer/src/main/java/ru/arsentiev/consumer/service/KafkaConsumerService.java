package ru.arsentiev.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.arsentiev.consumer.model.Item;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final EmailService emailService;

    // Читаем топик, заданный в application.yaml
    @KafkaListener(topics = "${producer.kafka.topic}")
    public void consume(Item item) {
        log.info("Получено сообщение: {}", item);
        String email = item.getOwner().getEmail();
        emailService.sendEmail(
                email,
                "Скидка на " + item.getName(),
                "Владелец " + email + ", для товара \"" + item.getName() + "\" появилась скидка!"
        );
    }
}
