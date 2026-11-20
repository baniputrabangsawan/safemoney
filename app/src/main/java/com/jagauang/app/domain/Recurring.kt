package com.jagauang.app.domain

enum class Recurring(val code: String, val label: String) {
    NONE("NONE", "Tidak berulang"),
    DAILY("DAILY", "Harian"),
    WEEKLY("WEEKLY", "Mingguan"),
    MONTHLY("MONTHLY", "Bulanan");

    companion object {
        fun fromCode(code: String?): Recurring = entries.firstOrNull { it.code == code } ?: NONE
    }
}
