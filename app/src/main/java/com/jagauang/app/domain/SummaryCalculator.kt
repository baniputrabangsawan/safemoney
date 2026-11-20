package com.jagauang.app.domain

import java.time.LocalDate

object SummaryCalculator {
    fun totalIncome(list: List<Transaction>, start: LocalDate?, end: LocalDate?): Long =
        list.filter { it.type == TransactionType.INCOME && inRange(it.day, start, end) }.sumOf { it.amountIdr }

    fun totalExpense(list: List<Transaction>, start: LocalDate?, end: LocalDate?): Long =
        list.filter { it.type == TransactionType.EXPENSE && inRange(it.day, start, end) }.sumOf { it.amountIdr }

    fun incomeByCategory(list: List<Transaction>, start: LocalDate?, end: LocalDate?): List<Pair<String, Long>> =
        group(list, start, end, TransactionType.INCOME)

    fun expenseByCategory(list: List<Transaction>, start: LocalDate?, end: LocalDate?): List<Pair<String, Long>> =
        group(list, start, end, TransactionType.EXPENSE)

    private fun group(list: List<Transaction>, start: LocalDate?, end: LocalDate?, type: TransactionType): List<Pair<String, Long>> =
        list.filter { it.type == type && inRange(it.day, start, end) }
            .groupBy { it.categoryCode ?: "" }
            .map { (code, ts) -> code to ts.sumOf { it.amountIdr } }
            .sortedByDescending { it.second }

    private fun inRange(day: LocalDate, start: LocalDate?, end: LocalDate?): Boolean =
        (start == null || !day.isBefore(start)) && (end == null || !day.isAfter(end))
}

data class LedgerSummary(
    val income: Long = 0L,
    val expense: Long = 0L,
    val incomeByCategory: List<Pair<String, Long>> = emptyList(),
    val expenseByCategory: List<Pair<String, Long>> = emptyList(),
)

fun summaryOf(list: List<Transaction>, start: LocalDate?, end: LocalDate?): LedgerSummary = LedgerSummary(
    income = SummaryCalculator.totalIncome(list, start, end),
    expense = SummaryCalculator.totalExpense(list, start, end),
    incomeByCategory = SummaryCalculator.incomeByCategory(list, start, end),
    expenseByCategory = SummaryCalculator.expenseByCategory(list, start, end),
)
