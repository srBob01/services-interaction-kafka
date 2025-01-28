package ru.arsentiev.producer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.arsentiev.producer.repository.ItemRepository;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemSchedulerService {

    private final ItemRepository itemRepository;
    private final KafkaProducerService producerService;
    private final Random random = new Random();

    @Scheduled(fixedRate = 15000)
    public void publishRandomItem() {
        long count = itemRepository.count();
        if (count == 0) {
            return;
        }
        long randomId = random.nextLong(count) + 1;
        itemRepository.findById(randomId).ifPresent(item -> {
            producerService.sendItem(item);
            log.info("Опубликовали товар: {}", item.getName());
        });
    }
}
