package it.gromov.zscore.service

import it.gromov.zscore.storage.PlayerStore

interface StorageService : Service {
    val nodeId: String
    fun store(): PlayerStore
}
