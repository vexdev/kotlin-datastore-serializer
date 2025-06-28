package iris

import com.google.cloud.datastore.Key
import kotlinx.serialization.KSerializer
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
        encoder.encodeKey(value)
    }

    override fun deserialize(decoder: Decoder): Key {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("KeySerializer can only be used with CloudDecoder")
        }
        return decoder.decodeKey()
    }
}