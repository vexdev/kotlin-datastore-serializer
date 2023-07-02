package iris

import com.google.cloud.datastore.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import iris.BooleanMock.Companion.BOOLEAN_MOCK
import iris.BooleanMock.Companion.BOOLEAN_MOCK_ENTITY
import iris.ComplexMock.Companion.COMPLEX_MOCK
import iris.ComplexMock.Companion.COMPLEX_MOCK_ENTITY
import kotlinx.serialization.Serializable


class CloudEncoderTest : FunSpec({
    context("encode values") {
        test("boolean case") {
            encodeToEntity(BOOLEAN_MOCK).build() entityEquals BOOLEAN_MOCK_ENTITY.build()
        }
        test("complex case") {
            encodeToEntity(COMPLEX_MOCK).build() entityEquals COMPLEX_MOCK_ENTITY.build()
        }
    }

})

infix fun <T : FullEntity<*>, U : T> T.entityEquals(expected: U?) = this.properties shouldBe expected?.properties

@Serializable
data class BooleanMock(val value: Boolean) {
    companion object {
        val BOOLEAN_MOCK = BooleanMock(true)
        val BOOLEAN_MOCK_ENTITY: FullEntity.Builder<IncompleteKey> = Entity.newBuilder().set("value", true)
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
    }
}

enum class EnumMock {
    A, B, C
}