package iris

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "Instant",
            kind = kotlinx.serialization.descriptors.PrimitiveKind.STRING
        )

    override fun serialize(encoder: Encoder, value: Instant) {
        if (encoder !is CloudEncoder) {
            throw IllegalStateException("InstantSerializer can only be used with CloudEncoder")
        }
        encoder.encodeInstant(value)
    }

    override fun deserialize(decoder: Decoder): Instant {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("InstantSerializer can only be used with CloudDecoder")
        }
        return decoder.decodeInstant()
    }

}