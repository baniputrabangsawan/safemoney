package com.jagauang.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {

    @Test
    fun plainDigits() = assertEquals(125000L, AmountParser.parse("125000"))

    @Test
    fun grouped() = assertEquals(125000L, AmountParser.parse("125.000"))

    @Test
    fun multiGroup() = assertEquals(1_250_000L, AmountParser.parse("1.250.000"))

    @Test
    fun whitespaceTrimmed() = assertEquals(5000L, AmountParser.parse(" 5.000 "))

    @Test
    fun oneAccepted() = assertEquals(1L, AmountParser.parse("1"))

    @Test
    fun maxAccepted() = assertEquals(999_999_999_999L, AmountParser.parse("999.999.999.999"))

    @Test
    fun zeroRejected() = assertNull(AmountParser.parse("0"))

    @Test
    fun emptyRejected() = assertNull(AmountParser.parse(""))

    @Test
    fun blankRejected() = assertNull(AmountParser.parse("   "))

    @Test
    fun signRejected() = assertNull(AmountParser.parse("-125"))

    @Test
    fun commaRejected() = assertNull(AmountParser.parse("125,5"))

    @Test
    fun decimalRejected() = assertNull(AmountParser.parse("125.5"))

    @Test
    fun leadingDotRejected() = assertNull(AmountParser.parse(".125"))

    @Test
    fun trailingDotRejected() = assertNull(AmountParser.parse("125."))

    @Test
    fun doubleDotRejected() = assertNull(AmountParser.parse("1..25"))

    @Test
    fun badGroupingRejected() = assertNull(AmountParser.parse("12.50"))

    @Test
    fun badGrouping2Rejected() = assertNull(AmountParser.parse("1250.000"))

    @Test
    fun tooLargeRejected() = assertNull(AmountParser.parse("1000000000000"))
}
