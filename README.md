# JavaSynchronizationLibrary
Helper library for synchronizing properties between two objects via reflection using pre-defined mapping by you.
## Description
OOP means working with a lot of objects, custom classes etc., but in the real world, we usually call external systems, using different types from different systems. But in the end, we could easily end up in a situation where we need to update object B by properties from object A and vice versa. There could be some options of overwriting values (e. g. only if the property is null, or maybe overwrite always..), transformation of values so the property can be set to another object etc. JavaSynchronizationLibrary solves this.
## Java version
The package is targeting Java 11, I downgraded it from 17, since I don't know what do Java devs use in their real world projects.
I used Amazon Corretto as a JDK during development.
## Example of usage
Let's say we have class HogwartsStudent and LorienStudent in our system and we want to synchronize data between them.
```
@Getter
@Builder(setterPrefix = "with")
public class HogwartsStudent {
    @Setter
    private Integer hogwartsId;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private HogwartsHouse house;
}
```
```
@Getter
@Builder(setterPrefix = "with")
public class LorienStudent {
    @Setter
    private Integer lorienId;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private LorienTree tree;
}
```
First we need to create a class that inherits Mapping class and implement its abstract method to define what should be synchronized from primary object to secondary object - in our case the HogwartsStudent will be primary and LorienStudent secondary:
```
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
```
Then we create a class that will inherit SynchronizationBase and provide type information:
```
public class HogwartsLorienSynchronizer extends SynchronizationBase<HogwartsStudent, LorienStudent, HogwartsLorienStudentMapping> {

    public HogwartsLorienSynchronizer() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(HogwartsStudent.class, LorienStudent.class, HogwartsLorienStudentMapping.class);
    }
}
```
The extends expression here
```
SynchronizationBase<HogwartsStudent, LorienStudent, HogwartsLorienStudentMapping>
```
means that we will be syncing objects of type HogwartsStudent and LorienStudent, accordingly to HogwartsLorienStudentMapping.
First generic parameter is Primary System Type, second generic parameter is Secondary System class. Mapping is used for defining what properties of given types on what conditions will be synchronized by default by SynchronizationBase methods.

--TBD