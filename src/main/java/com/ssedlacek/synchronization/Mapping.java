package com.ssedlacek.synchronization;


/**
 * Base class for mappings to be used in synchronization. This class should be inherited only for defining custom property mappings to be used for synchronization.
 */
public abstract class Mapping {
    public Mapping() {};


    /** Provides custom property mapping items to be used for synchronization for a specific synchronizer.
     */
    public abstract MappingItem[] getMappingItems();
}
