package com.ssedlacek.synchronization;

import java.lang.reflect.InvocationTargetException;

public class HogwartsLorienSynchronizer extends SynchronizationBase<HogwartsStudent, LorienStudent, HogwartsLorienStudentMapping> {
    public HogwartsLorienSynchronizer() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(HogwartsStudent.class, LorienStudent.class, HogwartsLorienStudentMapping.class);
    }


    @Override
    public void synchronize() throws InvocationTargetException, IllegalAccessException, GetterNotFoundException, SetterNotFoundException {

    }


    public void fakeSynchronize(HogwartsStudent hogwartsStudent, LorienStudent lorienStudent) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        updateByPrimary(hogwartsStudent, lorienStudent);
    }


    @Override
    protected Object transformToSecondary(Object value, String primaryFieldName, LorienStudent second, HogwartsStudent first) {
        if (value == null) {
            return super.transformToSecondary(value, primaryFieldName, second, first);
        }
        if (value.getClass() == HogwartsHouse.class) {
            return new LorienTree(((HogwartsHouse)value).name());
        }

        return super.transformToSecondary(value, primaryFieldName, second, first);
    }
}