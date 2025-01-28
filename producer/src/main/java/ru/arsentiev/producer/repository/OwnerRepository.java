package ru.arsentiev.producer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.arsentiev.producer.entity.Owner;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
}