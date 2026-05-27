package com.myassistant.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val details: String,
    val tags: String,                    // JSON Array 字符串
    @ColumnInfo(name = "screenshot_path") val screenshotPath: String?,
    @ColumnInfo(name = "event_time") val eventTime: Long?,
    val importance: Int = 3,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val status: String = "NORMAL"        // NORMAL / PINNED / ARCHIVED
)

@Fts4(contentEntity = CardEntity::class)
@Entity(tableName = "cards_fts")
data class CardFtsEntity(
    val title: String,
    val summary: String,
    val details: String,
    val tags: String
)
