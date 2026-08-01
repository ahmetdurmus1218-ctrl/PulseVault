package com.screenpulsedev.pulsevault.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getById(id: Long): VaultItem?

    @Insert
    suspend fun insert(item: VaultItem): Long

    @Update
    suspend fun update(item: VaultItem)

    @Delete
    suspend fun delete(item: VaultItem)
}
