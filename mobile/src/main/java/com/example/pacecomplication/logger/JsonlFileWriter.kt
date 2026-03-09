package com.example.pacecomplication.logger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class JsonlFileWriter {
    // чтобы параллельные записи не перемешивались в одной строке
    private val mutex = Mutex()

    suspend fun appendLine(file: File, line: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            file.parentFile?.mkdirs()

            FileOutputStream(file, /* append = */ true).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { w ->
                    w.write(line)
                    w.write("\n")
                    w.flush()
                }
            }
        }
    }

    suspend fun appendLines(file: File, lines: List<String>) = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext

        mutex.withLock {
            file.parentFile?.mkdirs()

            FileOutputStream(file, /* append = */ true).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { w ->
                    lines.forEach { line ->
                        w.write(line)
                        w.write("\n")
                    }
                    w.flush()
                }
            }
        }
    }
}