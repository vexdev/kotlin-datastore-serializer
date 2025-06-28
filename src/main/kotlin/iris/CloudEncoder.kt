package iris

import com.google.cloud.Timestamp
import com.google.cloud.datastore.*
import iris.model.GeoPoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A [CompositeEncoder] that encodes to a [FullEntity.Builder].
 */
@OptIn(ExperimentalSerializationApi::class)
class CloudEncoder : AbstractEncoder() {
    lateinit var entityBuilder: FullEntity.Builder<IncompleteKey>
    var keyName: String? = null
    var keyId: Long? = null

    private var cloudKeyCount = 0
    private var cloudKey = false
    private var elementName = ""
    private val queue: MutableList<SerializationEnvironment> = mutableListOf()
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (hasCloudKeyAnnotation(descriptor)) {
            invalidCloudKey()
        }
        queue.add(CollectionEnvironment(elementName, mutableListOf()))
        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (hasCloudKeyAnnotation(descriptor)) {
            invalidCloudKey()
        }
        queue.add(SerializableEnvironment(elementName, Entity.newBuilder()))
        return super.beginStructure(descriptor)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (hasCloudKeyAnnotation(descriptor)) {
            invalidCloudKey()
        }
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
        if (hasCloudKeyAnnotation(descriptor, index)) {
            foundCloudKey()
        }
        elementName = descriptor.getElementName(index)
        return super.encodeElement(descriptor, index)
    }

    private fun encodeValue(value: Value<*>) {
        if (cloudKey) {
            if (keyId == null && keyName == null) {
                invalidCloudKey()
            }
            cloudKey = false
            return // Do not add the value to the entity
        }
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
        if (cloudKey) {
            keyId = value
        }
        encodeValue(LongValue(value))
    }

    override fun encodeShort(value: Short) {
        encodeValue(LongValue(value.toLong()))
    }

    override fun encodeString(value: String) {
        if (cloudKey) {
            keyName = value
        }
        encodeValue(StringValue(value))
    }

    override fun encodeNull() {
        encodeValue(NullValue.of())
    }

    override fun encodeValue(value: Any) {
        throw NotImplementedError("encodeValue is not implemented as it is not used in serialization")
    }

    internal fun encodeGeoPoint(value: GeoPoint) {
        val env = requireSerializableEnvironment()
        env.entityBuilder.set(
            elementName,
            LatLngValue.of(LatLng.of(value.latitude, value.longitude))
        )
    }

    @OptIn(ExperimentalTime::class)
    internal fun encodeInstant(value: Instant) {
        val env = requireSerializableEnvironment()
        env.entityBuilder.set(
            elementName,
            TimestampValue.of(Timestamp.ofTimeSecondsAndNanos(value.epochSeconds, value.nanosecondsOfSecond))
        )
    }

    private fun requireSerializableEnvironment(): SerializableEnvironment {
        if (queue.isEmpty() || queue.last() !is SerializableEnvironment) {
            throw IllegalStateException("SerializableEnvironment is required for this operation")
        }
        return queue.last() as SerializableEnvironment
    }

    private fun foundCloudKey() {
        cloudKeyCount++
        if (cloudKeyCount > 1) {
            throw IllegalStateException("CloudKey can only be used once")
        }
        cloudKey = true
    }

    private fun hasCloudKeyAnnotation(serialDescriptor: SerialDescriptor, index: Int? = null): Boolean {
        if (index == null) {
            return serialDescriptor.annotations.any { it is CloudKey }
        }
        return serialDescriptor.getElementAnnotations(index).any { it is CloudKey }
    }

    private fun invalidCloudKey() {
        throw IllegalStateException("$elementName is not a valid Key. CloudKey can only be used with a String keyName or Long keyId")
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

fun <T> encodeToEntity(
    serializer: SerializationStrategy<T>,
    value: T,
): FullEntity.Builder<IncompleteKey> {
    val encoder = CloudEncoder()
    encoder.encodeSerializableValue(serializer, value)
    return encoder.entityBuilder
}

fun <T> encodeToEntity(
    serializer: SerializationStrategy<T>,
    value: T,
    keyFactory: KeyFactory
): Entity.Builder {
    val encoder = CloudEncoder()
    encoder.encodeSerializableValue(serializer, value)
    if (encoder.keyId == null && encoder.keyName == null) {
        throw IllegalStateException("Key not found")
    }
    val key: Key = encoder.keyName?.let { keyFactory.newKey(it) } ?: keyFactory.newKey(encoder.keyId!!)
    return Entity.newBuilder(key, encoder.entityBuilder.build())
}

inline fun <reified T> encodeToEntity(value: T) = encodeToEntity(serializer(), value)

inline fun <reified T> encodeToEntity(value: T, keyFactory: KeyFactory) =
    encodeToEntity(serializer(), value, keyFactory)