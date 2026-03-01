package com.example.pacecomplication.logger

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LogFilesManager(
    private val context: Context,
    private val dirName: String = "logs",
    private val appTtlDays: Long = 2L, // <-- меняешь тут
) {
    private val logsDir: File by lazy {
        File(context.filesDir, dirName).apply { mkdirs() }
    }

    private val dateFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"))

    fun ensureDirs() {
        logsDir.mkdirs()
    }

    /** app-YYYY-MM-DD.jsonl */
    fun appLogFile(nowMs: Long = System.currentTimeMillis()): File {
        val day = dateFmt.format(Instant.ofEpochMilli(nowMs))
        return File(logsDir, "app-$day.jsonl")
    }

    /** session-<sessionId>.jsonl (sessionId уже содержит время/uuid как решишь) */
    fun sessionLogFile(sessionId: String): File {
        return File(logsDir, "session-$sessionId.jsonl")
    }

    /** Удаляет только app-логи старше TTL. Session логи не трогает. */
    fun cleanupOldAppLogs(nowMs: Long = System.currentTimeMillis()) {
        val cutoffMs = nowMs - appTtlDays * 24L * 60L * 60L * 1000L

        logsDir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.startsWith("app-") && it.name.endsWith(".jsonl") }
            ?.forEach { file ->
                // lastModified подходит для TTL (без парсинга даты из имени)
                if (file.lastModified() < cutoffMs) {
                    runCatching { file.delete() }
                }
            }
    }
}