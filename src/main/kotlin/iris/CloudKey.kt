package iris

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Annotation to mark a property as a key in a Cloud Datastore entity. Needs to be used with the
 * [encodeToEntityKey] and [decodeFromEntityKey] functions.
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.PROPERTY)
@SerialInfo
annotation class CloudKey
