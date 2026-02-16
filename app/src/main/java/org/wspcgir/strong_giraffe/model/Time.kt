package org.wspcgir.strong_giraffe.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.OffsetDateTime
import java.util.TimeZone

@Serializable(with = Time.Serializer::class)
data class Time(val value: Instant) {

    object Serializer : KSerializer<Time> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("org.wspcgir.strong_giraffe.model.Time", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): Time {
            return Time(Instant.ofEpochSecond(Long.serializer().deserialize(decoder)))
        }

        override fun serialize(encoder: Encoder, value: Time) {
            Long.serializer().serialize(encoder, value.value.epochSecond)
        }
    }

    fun toOffsetDatetime(): OffsetDateTime {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(value.epochSecond),
            TimeZone.getDefault().toZoneId()
        )
    }
}