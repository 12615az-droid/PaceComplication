package com.example.pacecomplication.logger

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LogFilesManager(
    private val context: Context,
    private val dirName: String = "logs",
    private val appTtlDays: Long = 2L,
    private val sessionTtlDays: Long = 2L, // пока тоже 2
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

    fun cleanupOldLogs(nowMs: Long = System.currentTimeMillis()) {

        fun deleteOld(prefix: String, ttlDays: Long) {
            val cutoffMs = nowMs - ttlDays * 24L * 60L * 60L * 1000L

            logsDir.listFiles()
                ?.asSequence()
                ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.endsWith(".jsonl") }
                ?.forEach { file ->
                    if (file.lastModified() < cutoffMs) {
                        runCatching { file.delete() }
                    }
                }
        }

        deleteOld("app-", appTtlDays)
        deleteOld("session-", sessionTtlDays)
    }
}