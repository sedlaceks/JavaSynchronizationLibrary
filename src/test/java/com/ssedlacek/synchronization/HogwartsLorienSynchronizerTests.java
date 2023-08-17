package com.ssedlacek.synchronization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public final class HogwartsLorienSynchronizerTests {
    private final HogwartsLorienSynchronizer synchronizer = new HogwartsLorienSynchronizer();


    public HogwartsLorienSynchronizerTests() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }


    @Test
    public void given_mappingToSecondarySystem_then_secondaryObjectIsUpdatedAccordingToMapping() throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var hogwartsStudent = HogwartsStudent.builder()
                .withHogwartsId(123)
                .withFirstName("Harry")
                .withLastName("Potter")
                .build();

        var lorienStudent = LorienStudent.builder()
                .withLorienId(0)
                .withFirstName("Legolas")
                .withLastName("From Woods")
                .build();

        synchronizer.fakeSynchronize(hogwartsStudent, lorienStudent);

        Assertions.assertAll(() -> {
            Assertions.assertEquals(123, lorienStudent.getLorienId());
            Assertions.assertEquals("Harry", lorienStudent.getFirstName());
            Assertions.assertEquals("Potter", lorienStudent.getLastName());
        });
    }


    @Test
    public void given_propertyWithCustomTransformLogic_then_correctValueIsSyncedToSecondary() throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var hogwartsStudent = HogwartsStudent.builder()
                .withHouse(new HogwartsHouse("Slytherin"))
                .build();

        var lorienStudent = LorienStudent.builder().build();

        synchronizer.fakeSynchronize(hogwartsStudent, lorienStudent);

        Assertions.assertAll(() -> {
            Assertions.assertNotNull(lorienStudent.getTree());
            Assertions.assertEquals("Slytherin", lorienStudent.getTree().treeName());
        });
    }
}
