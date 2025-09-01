package com.fit4j.helpers.time

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Deprecated("It will be moved out of this library")
class TimeFixer(val timeZone: ZoneId = ZoneId.of("UTC")) {
    var fixedClock: Clock? = null

    // This method fixes LocalDateTime.now() to return the given @timeString
    // @timeString e.g. "2014-12-22T10:15:30.00Z"
    fun fixNow(timeString: String) {
        fixNow(Instant.parse(timeString))
    }

    fun fixNow(instant: Instant): Instant {
        fixedClock = Clock.fixed(instant, timeZone)
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedClock!!.instant().atZone(timeZone).toLocalDateTime()
        return fixedClock!!.instant()
    }

    fun resetNow() {
        unmockkStatic(LocalDateTime::class)
    }

}
