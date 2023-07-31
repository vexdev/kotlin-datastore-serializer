# kotlin-datastore-serializer

Kotlin Serializer for the Google Cloud Datastore

## Usage

Add the following to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}
implementation("com.vexdev:kotlin-datastore-serializer:0.1.1")
```

Then, you can use the Serializer to convert your data classes to entities:

```kotlin
@Serializable
data class MyDataClass(val id: String, val value: String)

val entity = encodeToEntity(MyDataClass("id", "value"))
```

Or your entities to data classes:

```kotlin
val dataClass = decodeFromEntity<MyDataClass>(entity)
```

## Releasing

To release a new version, run the following command:

```bash
./gradlew publish closeAndReleaseStagingRepository
``` 