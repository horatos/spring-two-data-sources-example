package com.example;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "entries")
public class Entry {
    @Id
    Integer id;

    String s;
}
