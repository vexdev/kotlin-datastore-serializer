@file:OptIn(ExperimentalTime::class)

package iris

import com.google.cloud.Timestamp
import com.google.cloud.datastore.BooleanValue
import com.google.cloud.datastore.DoubleValue
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.EntityValue
import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.IncompleteKey
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyValue
import com.google.cloud.datastore.LatLng
import com.google.cloud.datastore.LatLngValue
import com.google.cloud.datastore.ListValue
import com.google.cloud.datastore.LongValue
import com.google.cloud.datastore.NullValue
import com.google.cloud.datastore.StringValue
import com.google.cloud.datastore.TimestampValue
import iris.BooleanMock.Companion.BOOLEAN_MOCK
import iris.BooleanMock.Companion.BOOLEAN_MOCK_ENTITY
import iris.ComplexMock.Companion.COMPLEX_MOCK
import iris.ComplexMock.Companion.COMPLEX_MOCK_ENTITY
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class BooleanMock(val value: Boolean) {
    companion object {
        val BOOLEAN_MOCK = BooleanMock(true)
        val BOOLEAN_MOCK_ENTITY: FullEntity.Builder<IncompleteKey> = Entity.newBuilder().set("value", true)
    }
}

@Serializable
data class InvalidPlacementOfCloudKey(
    @CloudKey
    val key: Int
)

@Serializable
data class InvalidDoubleCloudKey(
    @CloudKey
    val key: String,
    @CloudKey
    val key2: Long
)

@Serializable
data class BooleanMockWithKey(
    val value: Boolean,
    @CloudKey
    val key: String
) {
    companion object {
        val BOOLEAN_MOCK_KEY_KEY = Key.newBuilder("project", "KIND", "keyValue00").build()
        val BOOLEAN_MOCK_KEY = BooleanMockWithKey(true, "keyValue00")
        val BOOLEAN_MOCK_KEY_ENTITY: Entity.Builder = Entity.newBuilder(BOOLEAN_MOCK_KEY_KEY).set("value", true)
    }
}

@Serializable
data class BooleanMockWithLongKey(
    val value: Boolean,
    @CloudKey
    val key: Long
) {
    companion object {
        val BOOLEAN_MOCK_LONG_KEY_KEY = Key.newBuilder("project", "KIND", null).setId(155L).build()
        val BOOLEAN_MOCK_LONG_KEY = BooleanMockWithLongKey(true, 155L)
        val BOOLEAN_MOCK_LONG_KEY_ENTITY: Entity.Builder =
            Entity.newBuilder(BOOLEAN_MOCK_LONG_KEY_KEY).set("value", true)
    }
}

@Serializable
data class ContainsLatLng(
    @Serializable(with = LatLngSerializer::class)
    val value: LatLng,
    @Serializable(with = LatLngSerializer::class)
    val nullable: LatLng?,
    @Serializable(with = LatLngSerializer::class)
    val nullableFilled: LatLng?,
    @Serializable(with = LatLngSerializer::class)
    val nullableWithDefault: LatLng = LatLng.of(6.0, 7.0)
) {
    companion object {
        val CONTAINS_GEOPOINT =
            ContainsLatLng(
                value = LatLng.of(1.0, 2.0), nullable = null, nullableFilled = LatLng.of(3.0, 4.0)
            )
        val CONTAINS_GEOPOINT_ENTITY: FullEntity.Builder<IncompleteKey> = Entity.newBuilder()
            .set("value", LatLngValue.of(LatLng.of(1.0, 2.0)))
            .set("nullable", NullValue.of())
            .set("nullableFilled", LatLngValue.of(LatLng.of(3.0, 4.0)))
            .set("nullableWithDefault", LatLngValue.of(LatLng.of(6.0, 7.0)))
    }
}

@Serializable
data class MapMock(
    val map: Map<String, String>,
) {
    companion object {
        val MAP_MOCK = MapMock(mapOf("key" to "value"))
        val MAP_MOCK_ENTITY: FullEntity.Builder<IncompleteKey> = Entity.newBuilder()
            .set("map", ListValue(StringValue("key"), StringValue("value")))
    }
}

@Serializable
data class NestedKey(
    @Serializable(with = KeySerializer::class)
    val key: Key
)

@Serializable
data class KeyRefMock(
    @CloudKey
    val id: String,
    @Serializable(with = KeySerializer::class)
    val remoteId: Key,
    val nestedKey: NestedKey
) {
    companion object {
        val KEY_REF_KEY = Key.newBuilder("project", "KIND", null).setName("CIAO").build()
        val KEY_REF_MOCK = KeyRefMock(
            "CIAO", Key.newBuilder("project", "KIND", "keyValue00").build(),
            NestedKey(Key.newBuilder("project", "KIND", "nestedKey").build())
        )
        val KEY_REF_MOCK_ENTITY: Entity.Builder = Entity.newBuilder(KEY_REF_KEY)
            .set("remoteId", KeyValue.of(KEY_REF_MOCK.remoteId))
            .set(
                "nestedKey", EntityValue(
                    Entity.newBuilder()
                        .set("key", KeyValue.of(KEY_REF_MOCK.nestedKey.key))
                        .build()
                )
            )
    }
}

@Serializable
data class ComplexMock(
    val booleanValue: Boolean,
    val byteValue: Byte,
    val charValue: Char,
    val doubleValue: Double,
    val enumValue: EnumMock,
    val floatValue: Float,
    val intValue: Int,
    val longValue: Long,
    val shortValue: Short,
    val stringValue: String,
    val serializableValue: BooleanMock,
    val nullableBooleanValue: Boolean?,
    val nullableSerializableValue: BooleanMock?,
    val booleanList: List<Boolean>,
    val byteList: List<Byte>,
    val charList: List<Char>,
    val doubleList: List<Double>,
    val enumList: List<EnumMock>,
    val floatList: List<Float>,
    val intList: List<Int>,
    val longList: List<Long>,
    val shortList: List<Short>,
    val stringList: List<String>,
    val serializableList: List<BooleanMock>,
    val nullableStringList: List<String?>,
    val nullableSerializableList: List<BooleanMock?>,
    val stringArray: Array<String>,
    val nullableStringArray: Array<String?>,
    val stringSet: Set<String>,
    val nullableStringSet: Set<String?>,
    @Serializable(with = LatLngSerializer::class)
    val geoPoint: LatLng,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
) {
    companion object {
        val COMPLEX_MOCK = ComplexMock(
            booleanValue = true,
            byteValue = 1,
            charValue = 'a',
            doubleValue = 1.0,
            enumValue = EnumMock.A,
            floatValue = 1.0f,
            intValue = 1,
            longValue = 1L,
            shortValue = 1,
            stringValue = "a",
            serializableValue = BOOLEAN_MOCK,
            nullableBooleanValue = true,
            nullableSerializableValue = BOOLEAN_MOCK,
            booleanList = listOf(true),
            byteList = listOf(1),
            charList = listOf('a'),
            doubleList = listOf(1.0),
            enumList = listOf(EnumMock.A),
            floatList = listOf(1.0f),
            intList = listOf(1),
            longList = listOf(1L),
            shortList = listOf(1),
            stringList = listOf("a"),
            serializableList = listOf(BOOLEAN_MOCK),
            nullableStringList = listOf("a", null),
            nullableSerializableList = listOf(BOOLEAN_MOCK, null),
            stringArray = arrayOf("a"),
            nullableStringArray = arrayOf("a", null),
            stringSet = setOf("a"),
            nullableStringSet = setOf("a", null),
            geoPoint = LatLng.of(1.0, 2.0),
            timestamp = Instant.fromEpochSeconds(123456, 0) // Example timestamp
        )
        val COMPLEX_MOCK_ENTITY: FullEntity.Builder<IncompleteKey> = Entity.newBuilder()
            .set("booleanValue", true)
            .set("byteValue", 1)
            .set("charValue", 'a'.code.toLong())
            .set("doubleValue", 1.0)
            .set("enumValue", "A")
            .set("floatValue", 1.0f.toDouble())
            .set("intValue", 1)
            .set("longValue", 1L)
            .set("shortValue", 1)
            .set("stringValue", "a")
            .set("serializableValue", EntityValue(BOOLEAN_MOCK_ENTITY.build()))
            .set("nullableBooleanValue", true)
            .set("nullableSerializableValue", EntityValue(BOOLEAN_MOCK_ENTITY.build()))
            .set("booleanList", ListValue(BooleanValue(true)))
            .set("byteList", ListValue(LongValue(1)))
            .set("charList", ListValue(LongValue('a'.code.toLong())))
            .set("doubleList", ListValue(DoubleValue(1.0)))
            .set("enumList", ListValue(StringValue("A")))
            .set("floatList", ListValue(DoubleValue(1.0f.toDouble())))
            .set("intList", ListValue(LongValue(1)))
            .set("longList", ListValue(LongValue(1L)))
            .set("shortList", ListValue(LongValue(1)))
            .set("stringList", ListValue(StringValue("a")))
            .set("serializableList", ListValue(EntityValue(BOOLEAN_MOCK_ENTITY.build())))
            .set("nullableStringList", listOf(StringValue("a"), NullValue()))
            .set("nullableSerializableList", listOf(EntityValue(BOOLEAN_MOCK_ENTITY.build()), NullValue()))
            .set("stringArray", listOf(StringValue("a")))
            .set("nullableStringArray", listOf(StringValue("a"), NullValue()))
            .set("stringSet", listOf(StringValue("a")))
            .set("nullableStringSet", listOf(StringValue("a"), NullValue()))
            .set("geoPoint", LatLngValue.of(LatLng.of(1.0, 2.0)))
            .set("timestamp", TimestampValue.of(Timestamp.ofTimeMicroseconds(123456000000)))
    }
}

@Serializable
data class NestedMock(
    val string: String,
    val nested: ComplexMock
) {
    companion object {
        val NESTED_MOCK = NestedMock("test", COMPLEX_MOCK)
        val NESTED_MOCK_ENTITY: FullEntity.Builder<IncompleteKey> = Entity.newBuilder()
            .set("string", "test")
            .set("nested", EntityValue(COMPLEX_MOCK_ENTITY.build()))
    }
}

enum class EnumMock {
    A, B, C
}