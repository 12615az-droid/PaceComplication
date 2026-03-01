package com.example.pacecomplication.logger

class StateLogStorage(
    private val files: LogFilesManager,
    private val writer: JsonlFileWriter
) {
    suspend fun appendEvent(jsonLine: String, sessionId: String?) {
        val file = if (sessionId == null) {
            files.appLogFile()
        } else {
            files.sessionLogFile(sessionId)
        }

        writer.appendLine(file, jsonLine)
    }
}