package iris

import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class KeySerializer : KSerializer<Key> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("Key")

    override fun serialize(encoder: Encoder, value: Key) {
        if (encoder !is CloudEncoder) {
            throw IllegalStateException("KeySerializer can only be used with CloudEncoder")
        }
        encoder.encodeValue(
            KeyValue.of(value)
        )
    }

    override fun deserialize(decoder: Decoder): Key {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("KeySerializer can only be used with CloudDecoder")
        }
        return (decoder.getValue() as KeyValue).get()
    }
}

typealias IrisKey = @Serializable(KeySerializer::class) Key