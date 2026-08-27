package com.example.ui.screens

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda_items")
data class AgendaItemData(
    @PrimaryKey val id: String,
    val day: String,
    val type: String,
    val isOnline: Boolean,
    val time: String,
    val title: String,
    val originalDateTime: String = "",
    val htmlLink: String = "",
    val isPast: Boolean = false
)
