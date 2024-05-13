package com.dicoding.asclepius.data.repository

import com.dicoding.asclepius.data.dao.HistoryDao
import com.dicoding.asclepius.data.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    suspend fun insertHistory(historyEntity: HistoryEntity) {
        historyDao.insertHistory(historyEntity)
    }

    suspend fun getAllHistory(): List<HistoryEntity> {
        return withContext(Dispatchers.IO) {
            historyDao.getAllHistory()
        }
    }

    suspend fun deleteHistory(historyEntity: HistoryEntity) {
        historyDao.deleteHistory(historyEntity)
    }
}