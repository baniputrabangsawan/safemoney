package com.jagauang.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods ORDER BY created_at_ms ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(method: PaymentMethodEntity)

    @Query("UPDATE payment_methods SET name = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)
}
