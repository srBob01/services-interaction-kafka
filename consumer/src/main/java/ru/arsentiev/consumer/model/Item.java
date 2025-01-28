package ru.arsentiev.consumer.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Item {
    private Long id;
    private String name;
    private Owner owner;
}