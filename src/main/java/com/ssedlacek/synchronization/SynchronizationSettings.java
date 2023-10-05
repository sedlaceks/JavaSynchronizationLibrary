package com.ssedlacek.synchronization;

import lombok.Getter;

@Getter
public class SynchronizationSettings {
    private static final String GETTER_PREFIX = "get";

    private static final String SETTER_PREFIX = "set";

    private final String getterPrefix;

    private final String setterPrefix;


    public SynchronizationSettings() {
        this(GETTER_PREFIX, SETTER_PREFIX);
    }


    public SynchronizationSettings(String getterPrefix, String setterPrefix) {
        this.getterPrefix = getterPrefix;
        this.setterPrefix = setterPrefix;
    }
}