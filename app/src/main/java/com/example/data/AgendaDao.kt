package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ui.screens.AgendaItemData
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_items ORDER BY originalDateTime ASC")
    fun getAllItems(): Flow<List<AgendaItemData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgendaItemData>)

    @Query("DELETE FROM agenda_items")
    suspend fun clearAll()
}
