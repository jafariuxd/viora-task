package com.example.data

import com.example.ui.screens.AgendaItemData
import kotlinx.coroutines.flow.Flow

class AgendaRepository(private val agendaDao: AgendaDao) {
    val allItems: Flow<List<AgendaItemData>> = agendaDao.getAllItems()

    suspend fun insertAll(items: List<AgendaItemData>) {
        agendaDao.insertAll(items)
    }

    suspend fun clearAndInsert(items: List<AgendaItemData>) {
        agendaDao.clearAll()
        agendaDao.insertAll(items)
    }

    suspend fun clearEvents() {
        agendaDao.clearAll()
    }
}
