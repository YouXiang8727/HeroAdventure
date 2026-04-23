package com.youxiang8727.heroadventure.model

import androidx.compose.ui.graphics.Color

/**
 * 英雄屬性的統一管理類別
 */
sealed class HeroStat(
    val icon: String,
    val label: String,
    val color: Color
) {
    abstract val formattedValue: String

    class CritRate(value: Double) : HeroStat("🎯", "爆擊率", Color(0xFFFFD700)) {
        override val formattedValue = "${(value * 100).toInt()}%"
    }

    class BlockRate(value: Double) : HeroStat("🛡️", "格擋率", Color(0xFF4DFFFF)) {
        override val formattedValue = "${(value * 100).toInt()}%"
    }

    class Gold(value: Int) : HeroStat("💰", "金幣", Color(0xFFFFA500)) {
        override val formattedValue = value.toString()
    }
}
