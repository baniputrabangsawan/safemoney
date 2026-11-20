package com.jagauang.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PeriodTest {

    @Test
    fun monthlyRange() {
        val (s, e) = Period(PeriodType.MONTHLY, LocalDate.of(2026, 11, 16)).range()
        assertEquals(LocalDate.of(2026, 11, 1), s)
        assertEquals(LocalDate.of(2026, 11, 30), e)
    }

    @Test
    fun dailyRange() {
        val (s, e) = Period(PeriodType.DAILY, LocalDate.of(2026, 11, 16)).range()
        assertEquals(LocalDate.of(2026, 11, 16), s)
        assertEquals(LocalDate.of(2026, 11, 16), e)
    }

    @Test
    fun yearlyRange() {
        val (s, e) = Period(PeriodType.YEARLY, LocalDate.of(2026, 5, 1)).range()
        assertEquals(LocalDate.of(2026, 1, 1), s)
        assertEquals(LocalDate.of(2026, 12, 31), e)
    }

    @Test
    fun weeklyRangeMondayToSunday() {
        val (s, e) = Period(PeriodType.WEEKLY, LocalDate.of(2026, 11, 18)).range() // Wednesday
        assertEquals(LocalDate.of(2026, 11, 16), s) // Monday
        assertEquals(LocalDate.of(2026, 11, 22), e) // Sunday
    }

    @Test
    fun allRangeIsUnbounded() {
        val (s, e) = Period(PeriodType.ALL, LocalDate.now()).range()
        assertNull(s)
        assertNull(e)
    }

    @Test
    fun shiftMonthly() {
        assertEquals(LocalDate.of(2026, 12, 16), Period(PeriodType.MONTHLY, LocalDate.of(2026, 11, 16)).shift(1).anchor)
    }
}
