package com.bobon.mypace.domain.pace

/**
 * Чистая доменная модель данных о темпе.
 * Не содержит строкового форматирования, только сырые данные.
 */
data class PaceUpdate(
    val secondsPerKm: Double
)
