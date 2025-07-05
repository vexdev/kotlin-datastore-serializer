package iris

import com.google.cloud.datastore.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
class CloudDecoder(
    val entity: FullEntity<*>?,
    val list: List<Value<*>>?,
    val elementCount: Int = 0,
    var elementName: String = "",
    private val isRoot: Boolean,
    private val isStrict: Boolean = false
) : AbstractDecoder() {
    private var elementIndex = 0
    private val isList = list != null
    private var cloudKeyFound = 0
    private var visitingKey = false
    private var hasKey = false

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (isRoot) {
            // For the root decoder we need to deserialize each element of the entity
            var totalElementsToDeserialize = entity!!.names.size.coerceAtLeast(descriptor.elementsCount)
            if ((0 until descriptor.elementsCount).any { i ->
                    descriptor.getElementAnnotations(i).any { annotation -> annotation is CloudKey }
                }) {
                // If there is a CloudKey annotation, we need to deserialize the key as well (Add it to the total count)
                hasKey = true
                totalElementsToDeserialize++
            }
            val strict = descriptor.annotations.any { it is StrictDeserialization }
            return CloudDecoder(
                entity,
                null,
                totalElementsToDeserialize,
                descriptor.getElementName(0),
                false,
                isStrict = strict
            )
        }

        val element = getValue()

        if (element is ListValue) {
            val list = element.get()
            return CloudDecoder(null, list, list.size, elementName, false)
        }
        if (element is EntityValue) {
            val entity = element.get()
            return CloudDecoder(entity, null, entity.names.size, elementName, false)
        }
        if (element is NullValue) {
            // Trying to decode a NullValue as a structure is not valid, this happens when trying to decode a null value to a non-nullable type.
            throw SerializationException("Cannot decode NullValue as a non-null structure. Element name: $elementName")
        }
        throw IllegalStateException("Unknown element type: ${element::class.simpleName}. Name: $elementName")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementCount) return CompositeDecoder.DECODE_DONE
        if (isList) {
            // Just continue decoding the list, the element name is not relevant here.
            return elementIndex++
        }
        // From here on, we are decoding a single entity (Not a list).
        if (elementIndex == descriptor.elementsCount) {
            // No more elements to decode in the descriptor, looks like the entity has more elements than the descriptor.
            if (isStrict) throw SerializationException("Entity has more elements than the descriptor")
            // If strict mode is off, we just skip the rest of the elements in the entity.
            return CompositeDecoder.DECODE_DONE
        }
        elementName = descriptor.getElementName(elementIndex)
        if (descriptor.getElementAnnotations(elementIndex).find { it is CloudKey } != null) {
            cloudKeyFound++
            if (cloudKeyFound > 1) throw IllegalStateException("Only one CloudKey is allowed")
            visitingKey = true
        }
        while (!entity!!.contains(elementName) && !visitingKey) {
            // If the element is not present in the entity (And is not a key), we skip it.
            elementIndex++
            if (elementIndex == elementCount) return CompositeDecoder.DECODE_DONE
            elementName = descriptor.getElementName(elementIndex)
        }
        return elementIndex++
    }

    internal fun getValue(): Value<*> {
        if (visitingKey) {
            visitingKey = false
            if (entity !is Entity) throw IllegalStateException("Must use a Entity (Not a FullEntity) to extract key")
            if (entity.key.hasName()) return StringValue.of(entity.key.name)
            if (entity.key.hasId()) return LongValue.of(entity.key.id)
            throw IllegalStateException("Key has no name or id")
        }
        return if (!isList) {
            if (!entity!!.contains(elementName))
                return NullValue.of()
            entity.getValue(elementName)
        } else {
            list!![elementIndex - 1]
        }
    }

    override fun decodeBoolean(): Boolean = (getValue() as BooleanValue).get()

    override fun decodeByte(): Byte = (getValue() as LongValue).get().toByte()

    override fun decodeChar(): Char = (getValue() as LongValue).get().toInt().toChar()

    override fun decodeDouble(): Double = (getValue() as DoubleValue).get()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        (getValue() as StringValue).get().let { enumDescriptor.getElementIndex(it) }

    override fun decodeFloat(): Float = (getValue() as DoubleValue).get().toFloat()

    override fun decodeInt(): Int = (getValue() as LongValue).get().toInt()

    override fun decodeLong(): Long = (getValue() as LongValue).get()

    override fun decodeNotNullMark(): Boolean = getValue() != NullValue.of()

    override fun decodeShort(): Short = (getValue() as LongValue).get().toShort()

    override fun decodeString(): String = (getValue() as StringValue).get()

}

fun <T> decodeFromEntity(entity: FullEntity<*>, deserializer: DeserializationStrategy<T>): T {
    val decoder = CloudDecoder(entity, null, isRoot = true)
    return decoder.decodeSerializableValue(deserializer)
}

inline fun <reified T> decodeFromEntity(entity: FullEntity<*>): T = decodeFromEntity(entity, serializer())