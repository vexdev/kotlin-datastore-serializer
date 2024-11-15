# kotlin-datastore-serializer

Kotlin Serializer for the Google Cloud Datastore, based on kotlinx.serialization.

## Features

- Encode data classes to entities
- Decode entities to data classes
- Support for encoding and decoding key names `String` or key ids `Long`

## Usage

Add the following to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}
implementation("com.vexdev:kotlin-datastore-serializer:0.2.0")
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

## Annotations

### `@CloudKey`

The `@CloudKey` annotation is used to mark a property in a data class that represents the key of an entity in 
Google Cloud Datastore. Only one property in a data class can be annotated with `@CloudKey`. 
If more than one property is annotated, an `IllegalStateException` will be thrown during decoding.

Only `String` or `Long` properties can be annotated with `@CloudKey`. If a property of any other type is annotated,
an `IllegalStateException` will be thrown during encoding.

Properties annotated with `@CloudKey` will be used to set the key of the entity when encoding and decoding, and will not
be included in the entity's properties.

Example usage:

```kotlin
@Serializable
data class MyDataClass(
    @CloudKey val id: String,
    val value: String
)
```

## Releasing

To release a new version, run the following command:

```bash
./gradlew publish closeAndReleaseStagingRepository
```