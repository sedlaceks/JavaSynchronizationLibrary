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
Then we create a class that will inherit PropertySynchronizer and provide type information:
```
public class HogwartsLorienSynchronizer extends PropertySynchronizer<HogwartsStudent, LorienStudent, HogwartsLorienStudentMapping> {

    public HogwartsLorienSynchronizer() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(HogwartsStudent.class, LorienStudent.class, HogwartsLorienStudentMapping.class);
    }
}
```
The extends expression here
```
PropertySynchronizer<HogwartsStudent, LorienStudent, HogwartsLorienStudentMapping>
```
means that we will be syncing objects of type HogwartsStudent and LorienStudent, accordingly to HogwartsLorienStudentMapping.
First generic parameter is Primary System Type, second generic parameter is Secondary System class. Mapping is used for defining what properties of given types on what conditions will be synchronized by default by PropertySynchronizer methods.

## Synchronization
We have defined mapping for our objects and created class that extends PropertySynchronizer with correct generic signature. How do we synchronize properties between the objects of our generic types?

The two main methods (that can be used in your synchronizer class) are:
```
protected boolean updateByPrimary(@NonNull TPrimary primary, @NonNull TSecondary secondary) throws InvocationTargetException, IllegalAccessException, GetterNotFoundException, SetterNotFoundException
```
UpdateByPrimary method will look into Mapping class of the synchronizer, and will take all MappingItems that are:
1. ToSecondarySystem
2. Bidirectional

and will set secondary object properties accordingly to the mapping - by values from primary object.
```
protected boolean updateBySecondary(@NonNull TSecondary second, @NonNull TPrimary first) throws GetterNotFoundException, SetterNotFoundException, InvocationTargetException, IllegalAccessException
```
UpdateBySecondry method will look into Mapping class of the synchronizer, and will take all MappingItems that are:
1. ToPrimarySystem
2. Bidirectional

and will set primary object properties accordingly to the mapping - by values from secondary object.

Note: The methods are not final on purpose - in case you would like to add something, just override it. You can call super.updateByPrimary/Secondary from your overriden method if needed.

## Value transformation
The library is ready to handle complex types to synchronize between your objects. I know that complex types is used in real world, that's why I added logic to allow transformation of values between objects.

How to do it? 
```
protected Object transformToPrimary(Object value, String secondaryFieldName, TPrimary first, TSecondary second) {
    return value;
}
```
```
protected Object transformToSecondary(Object value, String primaryFieldName, TSecondary second, TPrimary first) {
    return value;
}
```
If overridden, the two methods above allows you to handle the values as you want during the synchronization. Both methods are called before the value is set to the object.

Usage is pretty simple, you can override it and add a custom logic of transformation. For example:

```
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
```
This will tell the synchronizer that everytime a object of class HogwartsHouse (which is not primitive, it's a custom class) is being synced to secondary object, it will return a value of LorienTree - because the target in secondary object is actually LorienTree class.

## Synchronization settings
You can pass a custom getter/setter prefixes to synchronization if needed. By default, "get + propertyName" and "set + propertyName" is used for obtaining getter and setters from objects.

In case you don't use the prefixes, or you want to set your own, override this method:
```
protected SynchronizationSettings setSynchronizationSettings()
```
You can then return your own settings via constructor:
```
public SynchronizationSettings(String getterPrefix, String setterPrefix)
```
The synchronization will take it on mind when calling getters/setters.

As for now, only synchronization settings are getter and setter prefixes. There might be additional settings in the future.
## Important notes
Keep in mind that the library only sets property values on the objects - no update in your systems, external systems is not done by the library!
You still need to call some APIs, or save the objects in your database. Library only helps to set property values between two objects.

## Reflection
The library uses Reflection to handle property synchronization with caching. That means methods of classes are cached from the start, then retrieved from memory in runtime and invoked on objects.
I wondered how that would perform, so I wrote a simple tests to demonstrate:
```
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
```
I included custom transformation as well and I know it's a very basic scenario, just a few properties etc., but just so you would have an idea.
I created a fake "context" for the test of 50000 hogwarts students and 50000 lorien students with same IDs, some I could match them - simulate a real scenario where you find matching object that you would want to synchronize.

Given 50000 objects to synchronize via the library, the test takes around 11-12 seconds to complete.

When I changed it to 10000, the test ran under a second. Of course, I have a simple scenario, but in case you are afraid of it - don't be. Even though reflection is used, it's fast. I believe I still could find space to improve the performance, but for now reflection is OK. Still, I am not a Java developer (I am .NET dev :D).

## License
The code is licensed under MIT license. Feel free to use it anywhere you want. Pull requests/feature requests are welcomed.