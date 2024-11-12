package iris

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


class CloudDecoderTest : FunSpec({
    context("decode values") {
        test("boolean case") {
            decodeFromEntity<BooleanMock>(BOOLEAN_MOCK_ENTITY.build()) shouldBeEqualToComparingFields BOOLEAN_MOCK
        }
        test("boolean case with key") {
            val actual = decodeFromEntityKey<BooleanMockWithKey>(BOOLEAN_MOCK_KEY_ENTITY.build())
            val expected = BOOLEAN_MOCK_KEY
            actual shouldBeEqualToComparingFields expected
            actual.key shouldBeEqual expected.key
        }
        test("boolean case with long key") {
            val actual = decodeFromEntityKey<BooleanMockWithLongKey>(BOOLEAN_MOCK_LONG_KEY_ENTITY.build())
            val expected = BOOLEAN_MOCK_LONG_KEY
            actual shouldBeEqualToComparingFields expected
            actual.key shouldBeEqual expected.key
        }
        test("complex case") {
            decodeFromEntity<ComplexMock>(COMPLEX_MOCK_ENTITY.build()) shouldBeEqualToComparingFields COMPLEX_MOCK
        }
    }
})
