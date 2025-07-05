package iris

import com.google.cloud.Timestamp
import com.google.cloud.datastore.TimestampValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.max
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
        encoder.encodeValue(
            TimestampValue.of(
                Timestamp.ofTimeSecondsAndNanos(
                    value.epochSeconds,
                    value.nanosecondsOfSecond
                )
            )
        )
    }

    override fun deserialize(decoder: Decoder): Instant {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("InstantSerializer can only be used with CloudDecoder")
        }
        return (decoder.getValue() as TimestampValue).get()
            .let { Instant.fromEpochSeconds(it.seconds, max(it.nanos, 0)) }
    }

}

@OptIn(ExperimentalTime::class)
typealias IrisInstant = @Serializable(InstantSerializer::class) Instant