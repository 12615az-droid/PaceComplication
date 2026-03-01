package com.example.pacecomplication.history

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object SessionIdGenerator {
    private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
        .withZone(ZoneOffset.UTC)

    fun newId(now: Instant = Instant.now()): String {
        val ts = fmt.format(now)
        val tail = Random.nextInt(0, 0x10000).toString(16).padStart(4, '0')
        return "$ts-$tail"
    }
}