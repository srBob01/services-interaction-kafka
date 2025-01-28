package ru.arsentiev.consumer.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Owner {
    private String email;
    private String name;
}