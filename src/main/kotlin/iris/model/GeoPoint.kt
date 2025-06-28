package iris.model

import iris.GeoPointSerializer
import kotlinx.serialization.Serializable

@Serializable(
    with = GeoPointSerializer::class
)
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
