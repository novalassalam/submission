package com.dicoding.asclepius.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.asclepius.data.entity.HistoryEntity

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY id DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Delete()
    suspend fun deleteHistory(historyEntity: HistoryEntity)
}