package ru.arsentiev.producer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.arsentiev.producer.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
