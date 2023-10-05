package com.ssedlacek.synchronization.tests;

import lombok.Getter;

@Getter
public class HogwartsHouse {
    private final String name;

    public HogwartsHouse(String name) {
        this.name = name;
    }
}