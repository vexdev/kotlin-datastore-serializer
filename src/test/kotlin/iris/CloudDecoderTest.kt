package iris

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import iris.BooleanMock.Companion.BOOLEAN_MOCK
import iris.BooleanMock.Companion.BOOLEAN_MOCK_ENTITY
import iris.ComplexMock.Companion.COMPLEX_MOCK
import iris.ComplexMock.Companion.COMPLEX_MOCK_ENTITY


class CloudDecoderTest : FunSpec({
    context("decode values") {
        test("boolean case") {
            decodeFromEntity<BooleanMock>(BOOLEAN_MOCK_ENTITY.build()) shouldBeEqualToComparingFields BOOLEAN_MOCK
        }
        test("complex case") {
            decodeFromEntity<ComplexMock>(COMPLEX_MOCK_ENTITY.build()) shouldBeEqualToComparingFields COMPLEX_MOCK
        }
    }
})
