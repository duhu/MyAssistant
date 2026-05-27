package com.myassistant.app.domain.model

data class Card(
    val id: String = "",
    val title: String,
    val category: InfoCategory,
    val summary: String,
    val details: String,
    val tags: List<String> = emptyList(),
    val screenshotPath: String? = null,
    val eventTime: Long? = null,
    val importance: Int = 3,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: CardStatus = CardStatus.NORMAL
)
