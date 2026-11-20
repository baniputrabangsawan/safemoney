package com.jagauang.app.ui

import com.jagauang.app.domain.Category
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.TransactionType
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idLocale = Locale("in", "ID")
private val number = NumberFormat.getNumberInstance(idLocale).apply { maximumFractionDigits = 0 }
private val dayFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", idLocale)
private val shortFmt = DateTimeFormatter.ofPattern("d MMM", idLocale)

fun formatRupiah(amount: Long): String =
    if (amount < 0) "-Rp" + number.format(-amount) else "Rp" + number.format(amount)

fun formatDay(day: LocalDate): String = day.format(dayFmt)
fun formatDayShort(day: LocalDate): String = day.format(shortFmt)
fun formatTime(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

fun categoryName(categories: List<Category>, code: String?): String =
    categories.firstOrNull { it.code == code }?.name ?: "Tanpa kategori"

fun paymentMethodName(methods: List<PaymentMethod>, id: String?): String =
    methods.firstOrNull { it.id == id }?.name ?: ""

fun Transaction.displayTitle(categories: List<Category>, methods: List<PaymentMethod>): String = when (type) {
    TransactionType.TRANSFER -> "Transfer ${paymentMethodName(methods, transferSourceId)} → ${paymentMethodName(methods, transferDestinationId)}"
    else -> categoryName(categories, categoryCode)
}
