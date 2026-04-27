package com.youxiang8727.heroadventure.model

import androidx.compose.ui.graphics.Color

/**
 * 物品稀有度定義
 */
enum class Rarity(val label: String, val color: Color) {
    COMMON("普通", Color(0xFF9E9E9E)),
    RARE("稀有", Color(0xFF2196F3)),
    EPIC("史詩", Color(0xFF9C27B0)),
    LEGENDARY("傳說", Color(0xFFFF9800))
}

/**
 * 屬性類型
 */
enum class StatType(val label: String) {
    HP("生命"),
    ATTACK("攻擊"),
    CRIT_RATE("爆擊"),
    BLOCK_RATE("格擋")
}

/**
 * 屬性修正符，支援固定值或百分比
 */
data class StatModifier(
    val type: StatType,
    val value: Double,
    val isPercent: Boolean = false
) {
    override fun toString(): String {
        val sign = if (value >= 0) "+" else ""
        val displayValue = if (isPercent) "${(value * 100).toInt()}%" else value.toInt().toString()
        return "${type.label} $sign$displayValue"
    }
}

/**
 * 基礎物品類別
 */
sealed class ShopItem(
    val name: String,
    val price: Int,
    val rarity: Rarity = Rarity.COMMON,
    val level: Int = 1
) {
    abstract val description: String
}
