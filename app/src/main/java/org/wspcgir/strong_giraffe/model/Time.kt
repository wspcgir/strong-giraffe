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
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@Serializable(with = Time.Serializer::class)
data class Time(val value: Instant) {

    object Serializer : KSerializer<Time> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("org.wspcgir.strong_giraffe.model.Time", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): Time {
            return Time.fromEpochSecond(Long.serializer().deserialize(decoder))
        }

        override fun serialize(encoder: Encoder, value: Time) {
            Long.serializer().serialize(encoder, value.value.epochSecond)
        }
    }

    companion object {
        fun fromEpochSecond(time: Long): Time {
            return Time(Instant.ofEpochSecond(time))
        }
    }

    fun toOffsetDatetime(): OffsetDateTime {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(value.epochSecond),
            TimeZone.getDefault().toZoneId()
        )
    }

    fun asFormattedDate(): String {
        val zone = TimeZone.getDefault().toZoneId()
        val date = OffsetDateTime.ofInstant(value, zone)
        val dateFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return date.format(dateFormat)
    }

    fun asFormattedTime(): String {
        val found = this.value.epochSecond
        val zone = TimeZone.getDefault().toZoneId()
        val date = OffsetDateTime.ofInstant(value, zone)
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
        val result = date.format(timeFormat)
        return result
    }
}