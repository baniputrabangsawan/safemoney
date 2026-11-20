package com.jagauang.app.data.repository

import com.jagauang.app.data.local.BookEntity
import com.jagauang.app.data.local.CategoryEntity
import com.jagauang.app.data.local.PaymentMethodEntity
import com.jagauang.app.data.local.TransactionEntity
import com.jagauang.app.domain.Book
import com.jagauang.app.domain.Category
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.TransactionType
import java.time.LocalDate

fun BookEntity.toDomain() = Book(id = id, name = name)

fun PaymentMethodEntity.toDomain() = PaymentMethod(id = id, name = name)

fun CategoryEntity.toDomain() = Category(
    code = code,
    type = TransactionType.fromCode(type) ?: TransactionType.EXPENSE,
    name = name,
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    bookId = book_id,
    type = TransactionType.fromCode(type) ?: TransactionType.EXPENSE,
    amountIdr = amount_idr,
    day = LocalDate.ofEpochDay(transaction_day),
    timeMinutes = time_minutes,
    categoryCode = category_code,
    paymentMethodId = payment_method_id,
    transferSourceId = transfer_source_id,
    transferDestinationId = transfer_destination_id,
    note = note,
    attachmentPath = attachment_path,
    recurringCode = recurring_code,
    createdAtMs = created_at_ms,
    updatedAtMs = updated_at_ms,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    book_id = bookId,
    type = type.code,
    amount_idr = amountIdr,
    transaction_day = day.toEpochDay(),
    time_minutes = timeMinutes,
    category_code = categoryCode,
    payment_method_id = paymentMethodId,
    transfer_source_id = transferSourceId,
    transfer_destination_id = transferDestinationId,
    note = note,
    attachment_path = attachmentPath,
    recurring_code = recurringCode,
    created_at_ms = createdAtMs,
    updated_at_ms = updatedAtMs,
)
