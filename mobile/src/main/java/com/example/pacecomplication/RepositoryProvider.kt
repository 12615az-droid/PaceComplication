package com.example.pacecomplication

/**
 * Временный провайдер зависимостей до полной интеграции DI (Koin).
 */
object RepositoryProvider {
    val locationRepository: LocationRepository by lazy { LocationRepository() }
}