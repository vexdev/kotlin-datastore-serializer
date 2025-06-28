package iris

import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.KeyFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import iris.BooleanMock.Companion.BOOLEAN_MOCK
import iris.BooleanMock.Companion.BOOLEAN_MOCK_ENTITY
import iris.BooleanMockWithKey.Companion.BOOLEAN_MOCK_KEY
import iris.BooleanMockWithKey.Companion.BOOLEAN_MOCK_KEY_ENTITY
import iris.BooleanMockWithLongKey.Companion.BOOLEAN_MOCK_LONG_KEY
import iris.BooleanMockWithLongKey.Companion.BOOLEAN_MOCK_LONG_KEY_ENTITY
import iris.ComplexMock.Companion.COMPLEX_MOCK
import iris.ComplexMock.Companion.COMPLEX_MOCK_ENTITY


class CloudEncoderTest : FunSpec({
    context("encode values") {
        test("boolean case") {
            encodeToEntity(BOOLEAN_MOCK).build() entityEquals BOOLEAN_MOCK_ENTITY.build()
        }
        test("complex case") {
            encodeToEntity(COMPLEX_MOCK).build() entityEquals COMPLEX_MOCK_ENTITY.build()
        }
        test("boolean with key case") {
            val kf = KeyFactory("project").apply { setKind("KIND") }
            val actual = encodeToEntity(BOOLEAN_MOCK_KEY, kf).build()
            val expected = BOOLEAN_MOCK_KEY_ENTITY.build()
            actual entityEquals expected
            actual.key.id shouldBe expected.key.id
            actual.key.name shouldBe expected.key.name
        }
        test("invalid placement of cloud key") {
            val kf = KeyFactory("project").apply { setKind("KIND") }
            shouldThrow<IllegalStateException> {
                encodeToEntity(InvalidPlacementOfCloudKey(1), kf).build()
            }
        }
        test("invalid double cloud key") {
            val kf = KeyFactory("project").apply { setKind("KIND") }
            shouldThrow<IllegalStateException> {
                encodeToEntity(InvalidDoubleCloudKey("key", 1), kf).build()
            }
        }
        test("boolean with long key case") {
            val kf = KeyFactory("project").apply { setKind("KIND") }
            val actual = encodeToEntity(BOOLEAN_MOCK_LONG_KEY, kf).build()
            val expected = BOOLEAN_MOCK_LONG_KEY_ENTITY.build()
            actual entityEquals expected
            actual.key.id shouldBe expected.key.id
            actual.key.name shouldBe expected.key.name
        }
        test("contains GeoPoint") {
            val actual = encodeToEntity(ContainsLatLng.CONTAINS_GEOPOINT).build()
            val expected = ContainsLatLng.CONTAINS_GEOPOINT_ENTITY.build()
            actual entityEquals expected
        }
        test("nested mock") {
            val actual = encodeToEntity(NestedMock.NESTED_MOCK).build()
            val expected = NestedMock.NESTED_MOCK_ENTITY.build()
            actual entityEquals expected
        }
        test("map mock") {
            val entity = encodeToEntity(MapMock.MAP_MOCK).build()
            val expectedEntity = MapMock.MAP_MOCK_ENTITY.build()
            entity entityEquals expectedEntity
        }
        test("key ref mock") {
            val entity = encodeToEntity(KeyRefMock.KEY_REF_MOCK).build()
            val expectedEntity = KeyRefMock.KEY_REF_MOCK_ENTITY.build()
            entity entityEquals expectedEntity
        }
    }

})

infix fun <T : FullEntity<*>, U : T> T.entityEquals(expected: U?) = this.properties shouldBe expected?.properties