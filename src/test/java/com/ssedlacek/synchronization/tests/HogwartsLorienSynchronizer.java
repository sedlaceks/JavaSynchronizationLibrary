package com.ssedlacek.synchronization.tests;

import com.ssedlacek.synchronization.GetterNotFoundException;
import com.ssedlacek.synchronization.SetterNotFoundException;
import com.ssedlacek.synchronization.SynchronizationBase;

import java.lang.reflect.InvocationTargetException;

public class HogwartsLorienSynchronizer extends SynchronizationBase<HogwartsStudent, LorienStudent, HogwartsLorienStudentMapping> {
    public HogwartsLorienSynchronizer() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(HogwartsStudent.class, LorienStudent.class, HogwartsLorienStudentMapping.class);
    }


    public void fakeSynchronize(HogwartsStudent hogwartsStudent, LorienStudent lorienStudent) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        updateByPrimary(hogwartsStudent, lorienStudent);
    }


    @Override
    protected Object transformToSecondary(Object value, String primaryFieldName, LorienStudent second, HogwartsStudent first) {
        if (value == null) {
            return super.transformToSecondary(null, primaryFieldName, second, first);
        }
        if (value.getClass() == HogwartsHouse.class) {
            return new LorienTree(((HogwartsHouse)value).getName());
        }

        return super.transformToSecondary(value, primaryFieldName, second, first);
    }
}