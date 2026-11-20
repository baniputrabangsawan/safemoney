package com.jagauang.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["book_id", "transaction_day", "created_at_ms", "id"])]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val book_id: String,
    val type: String,
    val amount_idr: Long,
    val transaction_day: Long,
    val time_minutes: Int,
    val category_code: String?,
    val payment_method_id: String?,
    val transfer_source_id: String?,
    val transfer_destination_id: String?,
    val note: String?,
    val attachment_path: String?,
    val recurring_code: String?,
    val created_at_ms: Long,
    val updated_at_ms: Long,
)
