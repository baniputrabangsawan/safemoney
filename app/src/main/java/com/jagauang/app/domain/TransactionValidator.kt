package com.jagauang.app.domain

import java.time.LocalDate

object TransactionValidator {
    val MIN_DATE: LocalDate = LocalDate.of(1900, 1, 1)
    const val MAX_NOTE_LENGTH = 200
    const val MAX_TIME_MINUTES = 23 * 60 + 59
    const val MAX_NAME_LENGTH = 40

    fun isDateInRange(day: LocalDate, today: LocalDate): Boolean =
        !day.isBefore(MIN_DATE) && !day.isAfter(today)

    fun isTimeValid(minutes: Int): Boolean = minutes in 0..MAX_TIME_MINUTES

    fun normalizeNote(raw: String): String? =
        raw.trim().takeIf { it.isNotEmpty() }?.take(MAX_NOTE_LENGTH)

    fun normalizeName(raw: String): String? =
        raw.trim().takeIf { it.isNotEmpty() }?.take(MAX_NAME_LENGTH)

    fun isTransferValid(sourceId: String?, destinationId: String?): Boolean =
        sourceId != null && destinationId != null && sourceId != destinationId
}
