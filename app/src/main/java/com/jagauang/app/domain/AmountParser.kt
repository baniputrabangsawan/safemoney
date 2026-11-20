package com.jagauang.app.domain

/**
 * Parses whole-rupiah amounts written either as plain digits ("125000")
 * or with Indonesian thousand grouping ("125.000"). Rejects signs, fractions
 * (comma/decimal), and malformed grouping ("12.50", "1.25.000", "1250.000").
 */
object AmountParser {
    const val MIN_AMOUNT = 1L
    const val MAX_AMOUNT = 999_999_999_999L

    fun parse(input: String): Long? {
        val s = input.trim()
        if (s.isEmpty()) return null
        if (s.first() == '.' || s.last() == '.') return null
        if (s.contains("..")) return null
        if (s.any { !it.isDigit() && it != '.' }) return null

        val parts = s.split('.')
        if (parts.size > 1) {
            if (parts[0].length !in 1..3) return null
            for (i in 1 until parts.size) if (parts[i].length != 3) return null
        }

        val value = parts.joinToString("").toLongOrNull() ?: return null
        return value.takeIf { it in MIN_AMOUNT..MAX_AMOUNT }
    }
}
