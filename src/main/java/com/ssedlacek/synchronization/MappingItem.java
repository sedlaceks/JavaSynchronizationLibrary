package com.ssedlacek.synchronization;

public record MappingItem(String primarySystemNameField, String secondarySystemNameField, SyncDirection syncDirection, OverwritePolicy overwritePolicy) {
}