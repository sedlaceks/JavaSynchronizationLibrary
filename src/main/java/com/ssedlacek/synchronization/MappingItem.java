package com.ssedlacek.synchronization;

import lombok.Getter;

@Getter
public class MappingItem {
    private final String primarySystemNameField;

    private final String secondarySystemNameField;

    private final SyncDirection syncDirection;

    private final OverwritePolicy overwritePolicy;

    public MappingItem(String primarySystemNameField, String secondarySystemNameField, SyncDirection syncDirection, OverwritePolicy overwritePolicy) {
        this.primarySystemNameField = primarySystemNameField;
        this.secondarySystemNameField = secondarySystemNameField;
        this.syncDirection = syncDirection;
        this.overwritePolicy = overwritePolicy;
    }
}