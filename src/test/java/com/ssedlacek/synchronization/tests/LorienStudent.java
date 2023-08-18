package com.ssedlacek.synchronization.tests;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(setterPrefix = "with")
public class LorienStudent {
    @Setter
    private Integer lorienId;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private LorienTree tree;
}
