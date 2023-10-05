package com.ssedlacek.synchronization;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;


/** Provides convenient property synchronization between two objects using reflection.
 * @param <TPrimary> Primary system type to be synchronized.
 * @param <TSecondary> Secondary system type to be synchronized.
 * @param <TMapping> Mapping class to be used for obtaining definition for property synchronization.
 */
public abstract class SynchronizationBase<TPrimary, TSecondary, TMapping extends Mapping> {
    private final Class<TPrimary> primaryClass;

    private final Class<TSecondary> secondaryClass;

    private final TMapping mapping;

    protected final MappingItem[] getMappingItems() {
        return mapping.getMappingItems();
    }

    private Method[] primaryClassMethods;

    private Method[] getPrimaryClassMethods() {
        if (primaryClassMethods == null) {
            primaryClassMethods = ReflectionHelper.getClassMethods(primaryClass);
        }
        return primaryClassMethods;
    }

    private Method[] secondaryClassMethods;

    private Method[] getSecondaryClassMethods() {
        if (secondaryClassMethods == null) {
            secondaryClassMethods = ReflectionHelper.getClassMethods(secondaryClass);
        }
        return secondaryClassMethods;
    }


    private final SynchronizationSettings settings;


    public SynchronizationBase(Class<TPrimary> primaryClass, Class<TSecondary> secondaryClass, Class<TMapping> mapping)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.primaryClass = primaryClass;
        this.secondaryClass = secondaryClass;
        this.mapping = mapping.getDeclaredConstructor().newInstance();
        settings = setSynchronizationSettings();
    }


    protected SynchronizationSettings setSynchronizationSettings() {
        return new SynchronizationSettings();
    }


    protected boolean updateByPrimary(@NonNull TPrimary primary, @NonNull TSecondary secondary) throws InvocationTargetException, IllegalAccessException, GetterNotFoundException, SetterNotFoundException {
        var isChanged = false;

        for (var item : mapping.getMappingItems()) {
            if ((item.getSyncDirection() != SyncDirection.ToSecondarySystem) && (item.getSyncDirection() != SyncDirection.Bidirectional)) {
                continue;
            }

            isChanged |= updateFirstBySecond(primary, secondary, item, (d) -> transformToSecondary(d, item.getPrimarySystemNameField(), secondary, primary));
        }

        return isChanged;
    }


    protected boolean updateBySecondary(@NonNull TSecondary second, @NonNull TPrimary first) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var isChanged = false;

        for (var item : mapping.getMappingItems()) {
            if ((item.getSyncDirection() != SyncDirection.ToPrimarySystem) && (item.getSyncDirection() != SyncDirection.Bidirectional)) {
                continue;
            }

            isChanged |= updateFirstBySecond(second, first, item, (d) -> transformToPrimary(d, item.getSecondarySystemNameField(), first, second));
        }

        return isChanged;
    }


    private boolean updateFirstBySecond(Object first, Object second, MappingItem item, Function<Object, Object> transformMethod) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var hasChanged = false;

        final var primarySystemFieldGetter = Arrays.stream(getPrimaryClassMethods()).filter(m ->
                        m.getName().equals(settings.getGetterPrefix() + StringUtils.capitalize(item.getPrimarySystemNameField())))
                .findFirst()
                .orElseThrow(() -> new GetterNotFoundException("No getter found in primary object for property with name " + item.getPrimarySystemNameField()));

        final var primarySystemFieldGetterValue = primarySystemFieldGetter.invoke(first);

        // Obtain current value first.
        final var secondarySystemFieldGetter = Arrays.stream(getSecondaryClassMethods()).filter(d ->
                        d.getName().equals(settings.getGetterPrefix() + StringUtils.capitalize(item.getSecondarySystemNameField())))
                .findFirst()
                .orElseThrow(() -> new GetterNotFoundException("No getter found in class for property with name " + item.getSecondarySystemNameField()));

        // Prepare setter.
        final var secondarySystemFieldSetter = Arrays.stream(getSecondaryClassMethods()).filter(c ->
                        c.getName().equals(settings.getSetterPrefix() + StringUtils.capitalize(item.getSecondarySystemNameField())))
                .findFirst()
                .orElseThrow(() -> new SetterNotFoundException("No setter found in secondary object for property with name " + item.getSecondarySystemNameField()));

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


    /** Override this method to handle specific types to translate correctly to primary system. Used for transforming complex types to some type etc.
     */
    protected Object transformToPrimary(Object value, String secondaryFieldName, TPrimary first, TSecondary second) {
        return value;
    }


    /** Override this method to handle specific types to translate correctly to secondary system. Used for transforming complex types to some type etc.
     */
    protected Object transformToSecondary(Object value, String primaryFieldName, TSecondary second, TPrimary first) {
        return value;
    }}