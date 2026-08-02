package com.screenpulsedev.pulsevault.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromCategory(category: VaultCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): VaultCategory = VaultCategory.valueOf(value)

    @TypeConverter
    fun fromNetwork(network: CardNetwork): String = network.name

    @TypeConverter
    fun toNetwork(value: String): CardNetwork = CardNetwork.valueOf(value)
}

/** v1 -> v2: adds the two plaintext display fields for the realistic card UI. Keeps existing rows. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vault_items ADD COLUMN network TEXT NOT NULL DEFAULT 'OTHER'")
        db.execSQL("ALTER TABLE vault_items ADD COLUMN lastFourDigits TEXT NOT NULL DEFAULT ''")
    }
}

@Database(entities = [VaultItem::class], version = 2, exportSchema = false)
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
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
