package com.udemy.libraries.acceptancetests.helpers.protobuf

import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import java.time.Instant
import java.util.Date

@Deprecated("It will be moved out of this library")
class TimeConverter(private val jsonProtoPrinter: JsonFormat.Printer) {
    @Suppress("MagicNumber")
    fun dateToTimestamp(date: Date): Timestamp =
        Timestamp.newBuilder().setSeconds(date.time / 1000).setNanos((date.time % 1000).toInt() * 1000000).build()
    fun timestampToDate(timestamp: Timestamp): Date? {
        return if (timestamp != Timestamp.getDefaultInstance()) {
            Date.from(Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong()))
        } else {
            null
        }
    }

    fun timestamp(value: Date): String {
        return jsonProtoPrinter.print(this.dateToTimestamp(value))
    }
}