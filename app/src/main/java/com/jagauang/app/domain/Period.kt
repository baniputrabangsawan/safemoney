package com.jagauang.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class PeriodType(val label: String) {
    ALL("Semua"),
    DAILY("Harian"),
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan"),
    YEARLY("Tahunan"),
}

data class Period(val type: PeriodType, val anchor: LocalDate = LocalDate.now()) {
    fun range(): Pair<LocalDate?, LocalDate?> = when (type) {
        PeriodType.ALL -> null to null
        PeriodType.DAILY -> anchor to anchor
        PeriodType.WEEKLY -> anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .let { it to it.plusDays(6) }
        PeriodType.MONTHLY -> anchor.withDayOfMonth(1) to anchor.withDayOfMonth(anchor.lengthOfMonth())
        PeriodType.YEARLY -> LocalDate.of(anchor.year, 1, 1) to LocalDate.of(anchor.year, 12, 31)
    }

    fun shift(delta: Int): Period = when (type) {
        PeriodType.ALL -> this
        PeriodType.DAILY -> copy(anchor = anchor.plusDays(delta.toLong()))
        PeriodType.WEEKLY -> copy(anchor = anchor.plusWeeks(delta.toLong()))
        PeriodType.MONTHLY -> copy(anchor = anchor.plusMonths(delta.toLong()))
        PeriodType.YEARLY -> copy(anchor = anchor.plusYears(delta.toLong()))
    }
}

fun periodLabel(period: Period): String {
    val id = Locale("in", "ID")
    val (start, end) = period.range()
    return when (period.type) {
        PeriodType.ALL -> "Semua"
        PeriodType.DAILY -> start!!.format(DateTimeFormatter.ofPattern("d MMMM yyyy", id))
        PeriodType.WEEKLY -> "${start!!.format(DateTimeFormatter.ofPattern("d MMM", id))} – ${end!!.format(DateTimeFormatter.ofPattern("d MMM yyyy", id))}"
        PeriodType.MONTHLY -> start!!.format(DateTimeFormatter.ofPattern("MMMM yyyy", id))
        PeriodType.YEARLY -> start!!.year.toString()
    }
}
