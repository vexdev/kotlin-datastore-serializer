package iris

import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyValue
import com.google.cloud.datastore.ListValue
import com.google.cloud.datastore.NullValue
import com.google.cloud.datastore.StringValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equals.shouldBeEqual
import iris.BooleanMock.Companion.BOOLEAN_MOCK
import iris.BooleanMock.Companion.BOOLEAN_MOCK_ENTITY
import iris.BooleanMockWithKey.Companion.BOOLEAN_MOCK_KEY
import iris.BooleanMockWithKey.Companion.BOOLEAN_MOCK_KEY_ENTITY
import iris.BooleanMockWithLongKey.Companion.BOOLEAN_MOCK_LONG_KEY
import iris.BooleanMockWithLongKey.Companion.BOOLEAN_MOCK_LONG_KEY_ENTITY
import iris.ComplexMock.Companion.COMPLEX_MOCK
import iris.ComplexMock.Companion.COMPLEX_MOCK_ENTITY
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.assertThrows


class CloudDecoderTest : FunSpec({
    context("decode values") {
        test("boolean case") {
            decodeFromEntity<BooleanMock>(BOOLEAN_MOCK_ENTITY.build()) shouldBeEqualToComparingFields BOOLEAN_MOCK
        }
        test("boolean case with key") {
            val actual = decodeFromEntity<BooleanMockWithKey>(BOOLEAN_MOCK_KEY_ENTITY.build())
            val expected = BOOLEAN_MOCK_KEY
            actual shouldBeEqualToComparingFields expected
            actual.key shouldBeEqual expected.key
        }
        test("boolean case with long key") {
            val actual = decodeFromEntity<BooleanMockWithLongKey>(BOOLEAN_MOCK_LONG_KEY_ENTITY.build())
            val expected = BOOLEAN_MOCK_LONG_KEY
            actual shouldBeEqualToComparingFields expected
            actual.key shouldBeEqual expected.key
        }
        test("geopoint case") {
            val entity = ContainsLatLng.CONTAINS_GEOPOINT_ENTITY.build()
            val decoded = decodeFromEntity<ContainsLatLng>(entity)
            decoded shouldBeEqualToComparingFields ContainsLatLng.CONTAINS_GEOPOINT
        }
        test("complex case") {
            decodeFromEntity<ComplexMock>(COMPLEX_MOCK_ENTITY.build()) shouldBeEqualToComparingFields COMPLEX_MOCK
        }
        test("nested mock") {
            val entity = NestedMock.NESTED_MOCK_ENTITY.build()
            val decoded = decodeFromEntity<NestedMock>(entity)
            decoded shouldBeEqualToComparingFields NestedMock.NESTED_MOCK
        }
        test("map mock") {
            val entity = MapMock.MAP_MOCK_ENTITY.build()
            val decoded = decodeFromEntity<MapMock>(entity)
            decoded shouldBeEqualToComparingFields MapMock.MAP_MOCK
        }
        test("key ref mock") {
            val entity = KeyRefMock.KEY_REF_MOCK_ENTITY.build()
            val decoded = decodeFromEntity<KeyRefMock>(entity)
            decoded shouldBeEqualToComparingFields KeyRefMock.KEY_REF_MOCK
        }
        test("class has more keys than entity") {
            // Simulates the case where the class has a new key which is not present in the entity
            // E.G. when the class is a new version and the entity is an old version.
            // In this case the default value should be used.
            val entity = Entity.newBuilder().set("id", "1234").build()
            val decoded = decodeFromEntity<ClassWithDefaultValue>(entity)
            decoded shouldBeEqualToComparingFields ClassWithDefaultValue("1234", "default123")
        }
        test("class has more keys than entity (negative test)") {
            val entity = Entity.newBuilder().set("id", "1234").set("newValue", "abcd").build()
            val decoded = decodeFromEntity<ClassWithDefaultValue>(entity)
            decoded shouldBeEqualToComparingFields ClassWithDefaultValue("1234", "abcd")
        }
        test("class has less keys than entity (by default not strict)") {
            val entity = Entity.newBuilder().set("astring", "1234").set("anotherString", "abcd").build()
            val decoded = decodeFromEntity<SingleString>(entity)
            decoded shouldBeEqualToComparingFields SingleString("1234")
        }
        test("class has less keys than entity (when strict)") {
            val entity = Entity.newBuilder().set("astring", "1234").set("anotherString", "abcd").build()
            assertThrows<SerializationException> {
                decodeFromEntity<SingleStringStrict>(entity)
            }
        }
        test("different ordered keys") {
            val entity = Entity.newBuilder().set("b", 1).set("a", "test").build()
            val decoded = decodeFromEntity<DifferentOrderedKeys>(entity)
            decoded shouldBeEqualToComparingFields DifferentOrderedKeys("test", 1)
        }
        test("missing key should not throw exception") {
            val entity = ProdExample1.matchingEntity
            decodeFromEntity<ProdExample1>(entity)
        }
    }
})

@Serializable
data class DifferentOrderedKeys(
    val a: String,
    val b: Int
)

@Serializable
data class ClassWithDefaultValue(val id: String, val newValue: String = "default123")

@Serializable
data class SingleString(val astring: String)

@StrictDeserialization
@Serializable
data class SingleStringStrict(val astring: String)

@Serializable
data class ProdExample1(
    val missing: Double = 1.0,
    val existing: String
) {
    companion object {
        val matchingEntity = Entity.newBuilder(Key.newBuilder("project", "entity", "id1").build())
            .set("existing", StringValue.of("existingValue"))
            .build()
    }
}