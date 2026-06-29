package com.bobon.mypace.core.logger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

class JsonlFileWriter {

    private val mutexesByFile = ConcurrentHashMap<String, Mutex>()

    suspend fun appendLine(file: File, line: String) {
        appendLines(file, listOf(line))
    }

    suspend fun appendLines(file: File, lines: List<String>) = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext

        val mutex = mutexFor(file)

        mutex.withLock {
            file.parentFile?.mkdirs()

            FileOutputStream(file, true).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    lines.forEach { line ->
                        writer.write(line)
                        writer.write("\n")
                    }
                    writer.flush()
                }
            }
        }
    }

    private fun mutexFor(file: File): Mutex {
        return mutexesByFile.getOrPut(file.absolutePath) { Mutex() }
    }
}