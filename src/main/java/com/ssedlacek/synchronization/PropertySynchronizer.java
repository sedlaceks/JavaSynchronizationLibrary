package com.ssedlacek.synchronization;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Function;


/**
 * Provides convenient property synchronization between two objects using reflection.
 *
 * @param <TPrimary>   Primary system type to be synchronized.
 * @param <TSecondary> Secondary system type to be synchronized.
 * @param <TMapping>   Mapping class to be used for obtaining definition for property synchronization.
 */
public abstract class PropertySynchronizer<TPrimary, TSecondary, TMapping extends Mapping> {
    private final Class<TPrimary> primaryClass;

    private final Class<TSecondary> secondaryClass;

    private final TMapping mapping;

    protected final MappingItem[] getMappingItems() {
        return mapping.getMappingItems();
    }

    private final HashMap<String, Method> primaryClassMethods = new HashMap<>();

    private Method getPrimaryClassMethod(String methodName) {
        if (primaryClassMethods.isEmpty()) {
            for (var method : ReflectionHelper.getClassMethods(primaryClass)) {
                primaryClassMethods.put(method.getName(), method);
            }
        }
        return primaryClassMethods.get(methodName);
    }

    private final HashMap<String, Method> secondaryClassMethods = new HashMap<>();

    private Method getSecondaryClassMethod(String methodName) {
        if (secondaryClassMethods.isEmpty()) {
            for (var method : ReflectionHelper.getClassMethods(secondaryClass)) {
                secondaryClassMethods.put(method.getName(), method);
            }
        }
        return secondaryClassMethods.get(methodName);
    }


    private final SynchronizationSettings settings;


    public PropertySynchronizer(Class<TPrimary> primaryClass, Class<TSecondary> secondaryClass, Class<TMapping> mapping)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.primaryClass = primaryClass;
        this.secondaryClass = secondaryClass;
        this.mapping = mapping.getDeclaredConstructor().newInstance();
        settings = setSynchronizationSettings();
    }


    /**
     * Allows the caller to define custom SynchronizationSettings if needed - for example setter/getter prefixes. By default, "set + propertyName" and "get + propertyName" is used.
     */
    protected SynchronizationSettings setSynchronizationSettings() {
        return new SynchronizationSettings();
    }


    /**
     * @param primary Object that will provide property values.
     * @param secondary Object that will be updated by property values from primary according to mapping.
     * @return True if secondary object was changed, otherwise false.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws GetterNotFoundException Custom exception that is thrown when getter method is not found on object.
     * @throws SetterNotFoundException Custom exception that is thrown when setter method is not found on object.
     */
    protected boolean updateByPrimary(@NonNull TPrimary primary, @NonNull TSecondary secondary) throws InvocationTargetException, IllegalAccessException, GetterNotFoundException, SetterNotFoundException {
        var isChanged = false;

        for (var item : mapping.getMappingItems()) {
            if ((item.getSyncDirection() != SyncDirection.ToSecondarySystem) && (item.getSyncDirection() != SyncDirection.Bidirectional)) {
                continue;
            }

            isChanged |= updateSecondByFirst(primary, secondary, item, (d) -> transformToSecondary(d, item.getPrimarySystemNameField(), secondary, primary));
        }

        return isChanged;
    }


    /**
     * @param second Object that will provide property values.
     * @param first Object that will be updated by property values from primary according to mapping.
     * @return True if primary object was changed, otherwise false.
     * @throws GetterNotFoundException Custom exception that is thrown when getter method is not found on object.
     * @throws SetterNotFoundException Custom exception that is thrown when setter method is not found on object.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected boolean updateBySecondary(@NonNull TSecondary second, @NonNull TPrimary first) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var isChanged = false;

        for (var item : mapping.getMappingItems()) {
            if ((item.getSyncDirection() != SyncDirection.ToPrimarySystem) && (item.getSyncDirection() != SyncDirection.Bidirectional)) {
                continue;
            }

            isChanged |= updateSecondByFirst(second, first, item, (d) -> transformToPrimary(d, item.getSecondarySystemNameField(), first, second));
        }

        return isChanged;
    }


    private boolean updateSecondByFirst(Object first, Object second, MappingItem item, Function<Object, Object> transformMethod) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var hasChanged = false;

        final var primarySystemFieldGetter = getPrimaryClassMethod(settings.getGetterPrefix() + StringUtils.capitalize(item.getPrimarySystemNameField()));

         if (primarySystemFieldGetter == null) {
             throw new GetterNotFoundException("No getter found in primary object for property with name " + item.getPrimarySystemNameField());
         }

        final var primarySystemFieldGetterValue = primarySystemFieldGetter.invoke(first);

        final var secondarySystemFieldGetter = getSecondaryClassMethod(settings.getGetterPrefix() + StringUtils.capitalize(item.getSecondarySystemNameField()));

        if (secondarySystemFieldGetter == null) {
            throw new GetterNotFoundException("No getter found in class for property with name " + item.getSecondarySystemNameField());
        }

        final var secondarySystemFieldSetter = getSecondaryClassMethod(settings.getSetterPrefix() + StringUtils.capitalize(item.getSecondarySystemNameField()));

        if (secondarySystemFieldSetter == null) {
            throw new SetterNotFoundException("No setter found in secondary object for property with name " + item.getSecondarySystemNameField());
        }

        final var secValue = secondarySystemFieldGetter.invoke(second);

        final var transformed = transformMethod.apply(primarySystemFieldGetterValue);

        switch (item.getOverwritePolicy()) {
            case Always:
                secondarySystemFieldSetter.invoke(second, transformed);

                hasChanged = true;

                break;
            case IfNull:
                if (secValue == null) {
                    secondarySystemFieldSetter.invoke(second, transformed);

                    hasChanged = true;
                }
                break;
            case IfNotNull:
                if (secValue != null) {
                    secondarySystemFieldSetter.invoke(second, transformed);

                    hasChanged = true;
                }
                break;
        }

        return hasChanged;
    }


    /**
     * Override this method to handle specific types to translate correctly to primary system. Used for transforming complex types to some type etc.
     */
    protected Object transformToPrimary(Object value, String secondaryFieldName, TPrimary first, TSecondary second) {
        return value;
    }


    /**
     * Override this method to handle specific types to translate correctly to secondary system. Used for transforming complex types to some type etc.
     */
    protected Object transformToSecondary(Object value, String primaryFieldName, TSecondary second, TPrimary first) {
        return value;
    }
}