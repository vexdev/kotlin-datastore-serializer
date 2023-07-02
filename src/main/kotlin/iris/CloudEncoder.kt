package iris

import com.google.cloud.datastore.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * A [CompositeEncoder] that encodes to a [FullEntity.Builder].
 */
@OptIn(ExperimentalSerializationApi::class)
class CloudEncoder : AbstractEncoder() {
    lateinit var entityBuilder: FullEntity.Builder<IncompleteKey>
    private var elementName = ""
    private val queue: MutableList<SerializationEnvironment> = mutableListOf()
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        queue.add(CollectionEnvironment(elementName, mutableListOf()))
        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        queue.add(SerializableEnvironment(elementName, Entity.newBuilder()))
        return super.beginStructure(descriptor)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        val finishedEnv = queue.removeLast()
        if (queue.isEmpty()) {
            if (finishedEnv !is SerializableEnvironment)
                throw IllegalStateException("The root element must be a serializable environment")
            entityBuilder = finishedEnv.entityBuilder
            return
        }
        val currentEnv = queue.last()
        if (finishedEnv is CollectionEnvironment) {
            if (currentEnv is SerializableEnvironment) {
                currentEnv.entityBuilder.set(
                    finishedEnv.elementName,
                    ListValue.of(finishedEnv.serializedElements)
                )
            } else {
                (currentEnv as CollectionEnvironment).serializedElements.add(ListValue.of(finishedEnv.serializedElements))
            }
        } else {
            val finishedSerializableEnvironment = finishedEnv as SerializableEnvironment
            if (currentEnv is SerializableEnvironment) {
                currentEnv.entityBuilder.set(
                    finishedSerializableEnvironment.elementName,
                    finishedSerializableEnvironment.entityBuilder.build()
                )
            } else {
                (currentEnv as CollectionEnvironment).serializedElements.add(
                    EntityValue(
                        finishedSerializableEnvironment.entityBuilder.build()
                    )
                )
            }
        }
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        elementName = descriptor.getElementName(index)
        return super.encodeElement(descriptor, index)
    }

    private fun encodeValue(value: Value<*>) {
        val env = queue.last()
        if (env is CollectionEnvironment)
            env.serializedElements.add(value)
        else
            (env as SerializableEnvironment).entityBuilder.set(elementName, value)
    }

    override fun encodeBoolean(value: Boolean) {
        encodeValue(BooleanValue(value))
    }

    override fun encodeByte(value: Byte) {
        encodeValue(LongValue(value.toLong()))
    }

    override fun encodeChar(value: Char) {
        encodeValue(LongValue(value.code.toLong()))
    }

    override fun encodeDouble(value: Double) {
        encodeValue(DoubleValue(value))
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeValue(StringValue(enumDescriptor.getElementName(index)))
    }

    override fun encodeFloat(value: Float) {
        encodeValue(DoubleValue(value.toDouble()))
    }

    override fun encodeInt(value: Int) {
        encodeValue(LongValue(value.toLong()))
    }

    override fun encodeLong(value: Long) {
        encodeValue(LongValue(value))
    }

    override fun encodeShort(value: Short) {
        encodeValue(LongValue(value.toLong()))
    }

    override fun encodeString(value: String) {
        encodeValue(StringValue(value))
    }

    override fun encodeNull() {
        encodeValue(NullValue.of())
    }

    override fun encodeValue(value: Any) {
        throw NotImplementedError("encodeValue is not implemented as it is not used in serialization")
    }
}

sealed interface SerializationEnvironment

data class SerializableEnvironment(
    val elementName: String,
    val entityBuilder: FullEntity.Builder<IncompleteKey>,
) : SerializationEnvironment

data class CollectionEnvironment(
    val elementName: String,
    val serializedElements: MutableList<Value<*>>
) : SerializationEnvironment

fun <T> encodeToEntity(serializer: SerializationStrategy<T>, value: T): FullEntity.Builder<IncompleteKey> {
    val encoder = CloudEncoder()
    encoder.encodeSerializableValue(serializer, value)
    return encoder.entityBuilder
}

inline fun <reified T> encodeToEntity(value: T) = encodeToEntity(serializer(), value)