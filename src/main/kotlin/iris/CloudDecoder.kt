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
    private val isRoot: Boolean,
    private val withKey: Boolean
) : AbstractDecoder() {
    private var elementIndex = 0
    private val isList = list != null
    private var cloudKeyFound = 0
    private var visitingKey = false

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (isRoot) {
            if (withKey) {
                if (entity == null) throw IllegalStateException("Entity is null")
                if (entity !is Entity) throw IllegalStateException("Must use a Entity (Not a FullEntity) to extract key")
            }
            return CloudDecoder(entity, null, descriptor.elementsCount, descriptor.getElementName(0), false, withKey)
        }

        val element = getValue()

        if (element is ListValue) {
            val list = element.get()
            return CloudDecoder(null, list, list.size, elementName, false, withKey)
        }
        if (element is EntityValue) {
            val entity = element.get()
            return CloudDecoder(entity, null, entity.names.size, elementName, false, withKey)
        }
        throw IllegalStateException("Unknown element type")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementCount) return CompositeDecoder.DECODE_DONE
        elementName = descriptor.getElementName(elementIndex)
        if (withKey && descriptor.getElementAnnotations(elementIndex).find { it is CloudKey } != null) {
            cloudKeyFound++
            if (cloudKeyFound > 1) throw IllegalStateException("Only one CloudKey is allowed")
            visitingKey = true
        }
        return elementIndex++
    }

    private fun getValue(): Value<*> {
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
    val decoder = CloudDecoder(entity, null, isRoot = true, withKey = false)
    return decoder.decodeSerializableValue(deserializer)
}

fun <T> decodeFromEntityKey(entity: Entity, deserializer: DeserializationStrategy<T>): T {
    val decoder = CloudDecoder(entity, null, isRoot = true, withKey = true)
    return decoder.decodeSerializableValue(deserializer)
}

inline fun <reified T> decodeFromEntity(entity: FullEntity<*>): T = decodeFromEntity(entity, serializer())

inline fun <reified T> decodeFromEntityKey(entity: Entity): T = decodeFromEntityKey(entity, serializer())