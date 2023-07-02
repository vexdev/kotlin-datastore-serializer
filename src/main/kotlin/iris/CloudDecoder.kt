package iris

import com.google.cloud.datastore.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
class CloudDecoder(
    val entity: FullEntity<*>?,
    val list: List<Value<*>>?,
    val elementCount: Int = 0,
    var elementName: String = "",
    private val isRoot: Boolean = true,
) : AbstractDecoder() {
    private var elementIndex = 0
    private val isList = list != null

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (isRoot)
            return CloudDecoder(entity, null, descriptor.elementsCount, descriptor.getElementName(0), false)

        val element = getValue()

        if (element is ListValue) {
            val list = element.get()
            return CloudDecoder(null, list, list.size, elementName, false)
        }
        if (element is EntityValue) {
            val entity = element.get()
            return CloudDecoder(entity, null, entity.names.size, elementName, false)
        }
        throw IllegalStateException("Unknown element type")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementCount) return CompositeDecoder.DECODE_DONE
        elementName = descriptor.getElementName(elementIndex)
        return elementIndex++
    }

    private fun getValue(): Value<*> = if (!isList) {
        entity!!.getValue(elementName)
    } else {
        list!![elementIndex-1]
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
    val decoder = CloudDecoder(entity, null)
    return decoder.decodeSerializableValue(deserializer)
}

inline fun <reified T> decodeFromEntity(entity: FullEntity<*>): T = decodeFromEntity(entity, serializer())