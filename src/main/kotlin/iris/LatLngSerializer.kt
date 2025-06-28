package iris

import com.google.cloud.datastore.LatLng
import kotlinx.serialization.KSerializer
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
        encoder.encodeLatLng(value)
    }

    override fun deserialize(decoder: Decoder): LatLng {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("LatLngSerializer can only be used with CloudDecoder")
        }
        return decoder.decodeLatLng()
    }

}