package iris

import iris.model.GeoPoint
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class GeoPointSerializer : KSerializer<GeoPoint> {

    override val descriptor = buildClassSerialDescriptor(
        "GeoPoint"
    )

    override fun serialize(encoder: Encoder, value: GeoPoint) {
        if (encoder !is CloudEncoder) {
            throw IllegalStateException("GeoPointSerializer can only be used with CloudEncoder")
        }
        encoder.encodeGeoPoint(value)
    }

    override fun deserialize(decoder: Decoder): GeoPoint {
        if (decoder !is CloudDecoder) {
            throw IllegalStateException("GeoPointSerializer can only be used with CloudDecoder")
        }
        return decoder.decodeGeoPoint()
    }

}