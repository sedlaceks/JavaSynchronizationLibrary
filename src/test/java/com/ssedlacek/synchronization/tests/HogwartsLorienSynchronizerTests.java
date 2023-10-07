package com.ssedlacek.synchronization.tests;

import com.ssedlacek.synchronization.GetterNotFoundException;
import com.ssedlacek.synchronization.SetterNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class HogwartsLorienSynchronizerTests {
    private final HogwartsLorienSynchronizer synchronizer = new HogwartsLorienSynchronizer();


    public HogwartsLorienSynchronizerTests() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }


    @Test
    public void given_higherAmountOfObjectsToSync_then_ensureReasonableTimeIsTakenToSyncAll() throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException {
        var hogwartsStudents = new ArrayList<HogwartsStudent>();

        var lorienStudents = new ArrayList<LorienStudent>();

        var hStudents = 0;

        var lStudents = 0;

        for (var i = 0; i <= 50000; i++) {
            var hStudent = HogwartsStudent.builder()
                    .withHogwartsId(hStudents)
                    .withFirstName("Donald")
                    .withLastName("Wellington")
                    .withHouse(new HogwartsHouse("Grand"))
                    .build();

            hStudents++;

            hogwartsStudents.add(hStudent);

            var lStudent = LorienStudent.builder()
                    .withLorienId(lStudents)
                    .withFirstName("Random")
                    .withLastName(null)
                    .withTree(new LorienTree("LorienTree1"))
                    .build();

            lStudents++;

            lorienStudents.add(lStudent);
        }

        for (var hogwartsStudent : hogwartsStudents) {
            var lorienStudent = lorienStudents.stream().filter(s -> s.getLorienId().equals(hogwartsStudent.getHogwartsId())).findFirst();

            synchronizer.fakeSynchronize(hogwartsStudent, lorienStudent.get());
        }

        Assertions.assertIterableEquals(hogwartsStudents.stream().map(HogwartsStudent::getLastName).collect(Collectors.toList()),
                lorienStudents.stream().map(LorienStudent::getLastName).collect(Collectors.toList()));
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
                .withLastName(null) /* Demonstration purpose.*/
                .build();

        synchronizer.fakeSynchronize(hogwartsStudent, lorienStudent);

        Assertions.assertAll(() -> {
            Assertions.assertEquals(0, lorienStudent.getLorienId());
            Assertions.assertEquals("Legolas", lorienStudent.getFirstName());
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
            Assertions.assertEquals("Slytherin", lorienStudent.getTree().getName());
        });
    }
}