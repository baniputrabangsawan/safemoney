package com.jagauang.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SummaryCalculatorTest {

    private fun tx(id: String, type: TransactionType, amount: Long, day: LocalDate, category: String? = null) =
        Transaction(
            id = id, bookId = "book-rumah", type = type, amountIdr = amount, day = day, timeMinutes = 0,
            categoryCode = category,
            paymentMethodId = if (type == TransactionType.TRANSFER) null else "pm-cash",
            transferSourceId = if (type == TransactionType.TRANSFER) "pm-cash" else null,
            transferDestinationId = if (type == TransactionType.TRANSFER) "pm-bank" else null,
            note = null, attachmentPath = null, recurringCode = null,
            createdAtMs = 0L, updatedAtMs = 0L,
        )

    @Test
    fun rumahScenario_transferExcludedFromTotals() {
        val d = LocalDate.of(2026, 11, 16)
        val list = listOf(
            tx("1", TransactionType.INCOME, 100_000, d, "SALARY"),
            tx("2", TransactionType.EXPENSE, 30_000, d, "FOOD"),
            tx("3", TransactionType.TRANSFER, 20_000, d),
        )
        assertEquals(100_000L, SummaryCalculator.totalIncome(list, d, d))
        assertEquals(30_000L, SummaryCalculator.totalExpense(list, d, d))
        assertEquals(1, SummaryCalculator.incomeByCategory(list, d, d).size)
        assertEquals(1, SummaryCalculator.expenseByCategory(list, d, d).size)
    }

    @Test
    fun emptyBookIsZero() {
        assertEquals(0L, SummaryCalculator.totalIncome(emptyList(), null, null))
        assertEquals(0L, SummaryCalculator.totalExpense(emptyList(), null, null))
        assertEquals(0, SummaryCalculator.incomeByCategory(emptyList(), null, null).size)
    }

    @Test
    fun byCategoryAggregatesAndSorts() {
        val d = LocalDate.of(2026, 1, 1)
        val list = listOf(
            tx("1", TransactionType.EXPENSE, 100, d, "FOOD"),
            tx("2", TransactionType.EXPENSE, 200, d, "FOOD"),
            tx("3", TransactionType.EXPENSE, 300, d, "BILLS"),
        )
        val byCat = SummaryCalculator.expenseByCategory(list, d, d)
        assertEquals(2, byCat.size)
        assertEquals("FOOD" to 300L, byCat[0])
        assertEquals("BILLS" to 300L, byCat[1])
    }

    @Test
    fun periodRangeFilters() {
        val d10 = LocalDate.of(2026, 11, 10)
        val d20 = LocalDate.of(2026, 11, 20)
        val list = listOf(tx("1", TransactionType.INCOME, 100, d10), tx("2", TransactionType.INCOME, 200, d20))
        assertEquals(100L, SummaryCalculator.totalIncome(list, d10, d10))
        assertEquals(300L, SummaryCalculator.totalIncome(list, d10, d20))
    }
}
