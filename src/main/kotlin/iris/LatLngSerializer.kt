package iris

import com.google.cloud.datastore.LatLng
import com.google.cloud.datastore.LatLngValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LatLngSerializer : KSerializer<LatLng> {

    override val descriptor = buildClassSerialDescriptor(
        "LatLng"
    )

    override fun serialize(encoder: Encoder, value: LatLng) {
        if (encoder !is CloudEncoder) {
            throw IllegalStateException("LatLngSerializer can only be used with CloudEncoder")
        }
        encoder.encodeValue(
            LatLngValue.of(LatLng.of(value.latitude, value.longitude))
        )
    }

    override fun deserialize(decoder: Decoder): LatLng {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("LatLngSerializer can only be used with CloudDecoder")
        }
        val value = decoder.getValue() as LatLngValue
        return value.get().let {
            LatLng.of(it.latitude, it.longitude)
        }
    }

}

typealias IrisLatLng = @Serializable(LatLngSerializer::class) LatLng