package com.ssedlacek.synchronization.tests;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(setterPrefix = "with")
public class HogwartsStudent {
    @Setter
    private Integer hogwartsId;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private HogwartsHouse house;
}
