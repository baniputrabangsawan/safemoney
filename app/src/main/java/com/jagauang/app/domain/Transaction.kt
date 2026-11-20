package com.jagauang.app.domain

import java.time.LocalDate

data class Transaction(
    val id: String,
    val bookId: String,
    val type: TransactionType,
    val amountIdr: Long,
    val day: LocalDate,
    val timeMinutes: Int,
    val categoryCode: String?,
    val paymentMethodId: String?,
    val transferSourceId: String?,
    val transferDestinationId: String?,
    val note: String?,
    val attachmentPath: String?,
    val recurringCode: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)
