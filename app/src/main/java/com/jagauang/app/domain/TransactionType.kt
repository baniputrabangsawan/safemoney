package com.jagauang.app.domain

enum class TransactionType(val code: String) {
    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    TRANSFER("TRANSFER");

    companion object {
        fun fromCode(code: String): TransactionType? = entries.firstOrNull { it.code == code }
    }
}
