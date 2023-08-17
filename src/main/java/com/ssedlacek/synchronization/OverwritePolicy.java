package com.ssedlacek.synchronization;


/**
 * Defines a policy for overwriting property values during synchronization.
 */
public enum OverwritePolicy {
    None, Always, IfNull, IfNotNull
}
