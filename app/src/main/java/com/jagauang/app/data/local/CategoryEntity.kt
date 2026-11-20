package com.jagauang.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val code: String,
    val type: String,
    val name: String,
    val created_at_ms: Long,
)
