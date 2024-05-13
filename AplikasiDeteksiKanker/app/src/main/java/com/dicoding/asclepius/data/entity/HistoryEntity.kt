package com.dicoding.asclepius.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "history"
)
data class HistoryEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "label")
    var label: String = "Cancer",

    @ColumnInfo(name = "image")
    var image: String = "",

    @ColumnInfo(name = "score")
    var score: Float = 0.0f,

    @ColumnInfo(name = "date")
    var date: String = ""
)