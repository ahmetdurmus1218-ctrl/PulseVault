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

/** v2 -> v3: adds bank name + virtual-card flag, both plaintext (printed on the card). */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vault_items ADD COLUMN bank TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vault_items ADD COLUMN isVirtual INTEGER NOT NULL DEFAULT 0")
    }
}

/** v3 -> v4: adds the favorite flag (plaintext, purely a UI convenience). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vault_items ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [VaultItem::class], version = 4, exportSchema = false)
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
    }
}
