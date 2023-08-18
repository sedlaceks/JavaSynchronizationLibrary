package com.ssedlacek.synchronization.tests;

import com.ssedlacek.synchronization.*;

public class HogwartsLorienStudentMapping extends Mapping {
    @Override
    public MappingItem[] getMappingItems() {
        return new MappingItem[] {
            new MappingItem("hogwartsId", "lorienId", SyncDirection.ToSecondarySystem, OverwritePolicy.IfNull),
            new MappingItem("firstName", "firstName", SyncDirection.ToSecondarySystem, OverwritePolicy.IfNull),
            new MappingItem("lastName", "lastName", SyncDirection.ToSecondarySystem, OverwritePolicy.IfNull),
            new MappingItem("house", "tree", SyncDirection.ToSecondarySystem, OverwritePolicy.IfNull)
        };
    }
}
