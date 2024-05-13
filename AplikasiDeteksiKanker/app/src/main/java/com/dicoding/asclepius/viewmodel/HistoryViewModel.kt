package com.dicoding.asclepius.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.data.entity.HistoryEntity
import com.dicoding.asclepius.data.repository.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyRepository: HistoryRepository) : ViewModel() {
    fun insertHistory(historyEntity: HistoryEntity) {
        viewModelScope.launch {
            historyRepository.insertHistory(historyEntity)
        }
    }

    private val _allHistory = MutableLiveData<List<HistoryEntity>>()
    val allHistory: LiveData<List<HistoryEntity>>
        get() = _allHistory

    fun getAllHistory() {
        viewModelScope.launch {
            try {
                val historyList = historyRepository.getAllHistory()
                _allHistory.value = historyList
            } catch (e: Exception) {
                // Handle exceptions if needed
                Log.e("HistoryViewModel", "Error fetching all history: ${e.message}")
            }
        }
    }

    fun deleteHistory(historyEntity: HistoryEntity) {
        viewModelScope.launch {
            historyRepository.deleteHistory(historyEntity)
            getAllHistory()
        }
    }
}