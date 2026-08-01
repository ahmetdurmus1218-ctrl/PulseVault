package com.screenpulsedev.pulsevault.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromCategory(category: VaultCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): VaultCategory = VaultCategory.valueOf(value)
}

@Database(entities = [VaultItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context): VaultDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "pulsevault.db"
                ).build().also { INSTANCE = it }
            }
    }
}
