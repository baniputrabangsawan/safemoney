package com.jagauang.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, BookEntity::class, PaymentMethodEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class JagaUangDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun bookDao(): BookDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var instance: JagaUangDatabase? = null

        fun getInstance(context: Context): JagaUangDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    JagaUangDatabase::class.java,
                    "jaga_uang.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
