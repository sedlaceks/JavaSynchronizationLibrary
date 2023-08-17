package com.ssedlacek.synchronization;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

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
