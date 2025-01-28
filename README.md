# Пример взаимодействия микросервисов с использованием Apache Kafka

## Описание проекта

Данный проект демонстрирует взаимодействие микросервисов через Apache Kafka. Реализованы два микросервиса:

1. **Producer** - отвечает за публикацию сообщений в Kafka.
2. **Consumer** - читает сообщения из Kafka и отправляет email уведомления.

Проект использует **Spring Boot**, **PostgreSQL** для хранения данных, **Kafka** для асинхронной коммуникации и **MailHog**
для тестирования email-уведомлений.

---

## Общий процесс

1. **Producer**:
    - Публикует сообщения о товарах из базы данных PostgreSQL в Kafka-топик `items-topic` с использованием Spring Kafka.
    - Каждое сообщение содержит информацию о товаре (ID, название, email владельца).

2. **Consumer**:
    - Подписывается на Kafka-топик `items-topic` и получает сообщения.
    - Для каждого сообщения отправляет email владельцу товара с уведомлением о скидке.

3. **MailHog**:
    - Эмулирует SMTP-сервер и позволяет просматривать отправленные письма через веб-интерфейс.

## Kafka

- Сообщения в Kafka публикуются и потребляются **асинхронно**, что позволяет достичь высокой производительности и
  масштабируемости.
- Топик `items-topic` настроен с **двумя партициями**:
    - Каждая партиция хранится на своем брокере (например, `kafka1` и `kafka2`).
    - Для отказоустойчивости каждая партиция имеет **реплику** на другом брокере.
- Такой подход обеспечивает распределение нагрузки между брокерами и повышает надежность.

---

## Структура проекта

- **Producer**:
    - Хранит информацию о товарах и их владельцах в базе данных PostgreSQL.
    - Периодически выбирает случайный товар и публикует его в Kafka-топик.

- **Consumer**:
    - Читает сообщения из Kafka-топика.
    - Обрабатывает полученные данные и отправляет уведомления по электронной почте.

---

## Docker Compose

```yaml
services:
  # Zookeeper для координации Kafka
  zookeeper:
    image: bitnami/zookeeper:latest
    container_name: zookeeper
    environment:
      ALLOW_ANONYMOUS_LOGIN: "yes"
    ports:
      - "2181:2181"

  # Первый брокер Kafka
  kafka1:
    image: bitnami/kafka:latest
    container_name: kafka1
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      ALLOW_PLAINTEXT_LISTENER: "yes"
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka1:9092
    ports:
      - "9092:9092"

  # Второй брокер Kafka
  kafka2:
    image: bitnami/kafka:latest
    container_name: kafka2
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      ALLOW_PLAINTEXT_LISTENER: "yes"
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka2:9093
    ports:
      - "9093:9093"

  # PostgreSQL для Producer
  producer-db:
    image: postgres:15-alpine
    container_name: producer_db
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: producer_db
    ports:
      - "5432:5432"

  # MailHog для тестирования email
  mailhog:
    image: mailhog/mailhog
    container_name: mailhog
    ports:
      - "1025:1025"   # SMTP
      - "8025:8025"   # Web-интерфейс

  # Producer микросервис
  producer:
    build:
      context: ./producer
      dockerfile: Dockerfile
    container_name: producer_app
    depends_on:
      - producer-db
      - kafka1
      - kafka2
    environment:
      DB_HOST: producer-db
      DB_PORT: 5432
      DB_NAME: producer_db
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      KAFKA_HOST_1: kafka1
      KAFKA_PORT_1: 9092
      KAFKA_HOST_2: kafka2
      KAFKA_PORT_2: 9093
    ports:
      - "8080:8080"

  # Consumer микросервис
  consumer:
    build:
      context: ./consumer
      dockerfile: Dockerfile
    container_name: consumer_app
    depends_on:
      - kafka1
      - kafka2
      - mailhog
    environment:
      KAFKA_HOST_1: kafka1
      KAFKA_PORT_1: 9092
      KAFKA_HOST_2: kafka2
      KAFKA_PORT_2: 9093
    ports:
      - "8081:8081"
```

1. **Zookeeper**:
    - Управляет конфигурацией кластеров Kafka.
    - Координирует брокеров.

2. **Брокеры Kafka**:
    - В проекте используются два брокера для демонстрации распределённости.
    - Топик `items-topic` автоматически создаётся с двумя партициями.

3. **Публикация сообщений**:
    - Producer публикует сообщения с ключами (email владельца товара) для управления партиционированием.

4. **Чтение сообщений**:
    - Consumer подписывается на `items-topic` и обрабатывает сообщения асинхронно.

---

## Пример кода

### Producer: публикация сообщений

Producer использует `KafkaTemplate` для отправки сообщений в топик.

```java

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
```

### Consumer: обработка сообщений

Consumer подписывается на топик и отправляет email уведомления.

```java

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final EmailService emailService;

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
```

---

## Как запустить проект

1. Убедитесь, что Docker и Docker Compose установлены на вашем компьютере.
2. Выполните команду для запуска всех сервисов:

   ```bash
   docker-compose up --build
   ```

3. Откройте веб-интерфейс MailHog по адресу `http://localhost:8025` для просмотра отправленных писем.

---

## Пример работы

### Консольный вывод Consumer

1. **Получение сообщения**:
   ```
   Partition:0     owner2@example.com      {"id":22,"name":"ItemB_of_owner2@example.com","owner":{"email":"owner2@example.com","name":"Owner 2"}}
   ```

2. **Отправка email**:
   ```
   Получено сообщение: Item(id=22, name=ItemB_of_owner2@example.com, owner=Owner(email=owner2@example.com, name=Owner 2))
   Отправлено письмо на owner2@example.com
   ```

---

## Преимущества

1. **Асинхронная обработка**:
    - Сообщения публикуются и обрабатываются асинхронно.
    - Высокая производительность благодаря неблокирующей архитектуре.

2. **Распределение нагрузки**:
    - Сообщения распределяются между двумя партициями, что позволяет равномерно загружать брокеры.
    - Репликация партиций повышает надежность и отказоустойчивость.

3. **Масштабируемость**:
    - Возможность добавления новых партиций и брокеров при увеличении нагрузки.
