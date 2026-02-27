package com.example.pacecomplication.Logger

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
}