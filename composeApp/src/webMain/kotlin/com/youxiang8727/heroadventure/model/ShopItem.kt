package com.youxiang8727.heroadventure.model

import androidx.compose.ui.graphics.Color

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
 * 屬性修正符
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
 * 物品效果介面 (用於消耗品)
 */
sealed interface ItemEffect {
    val description: String
    fun apply(hero: Hero)
}

data class HealEffect(val amount: Int) : ItemEffect {
    override val description: String = "回復 $amount 點生命值"
    override fun apply(hero: Hero) {
        hero.heal(amount)
    }
}

data class PermanentStatEffect(val modifier: StatModifier) : ItemEffect {
    override val description: String = "永久 ${modifier}"
    override fun apply(hero: Hero) {
        when (modifier.type) {
            StatType.HP -> {
                hero.maxHp += modifier.value.toInt()
                hero.currentHp += modifier.value.toInt()
            }
            StatType.ATTACK -> hero.attack += modifier.value.toInt()
            StatType.CRIT_RATE -> hero.bonusCritRate += modifier.value
            StatType.BLOCK_RATE -> hero.bonusBlockRate += modifier.value
        }
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

/**
 * 消耗品 (使用後消失)
 */
open class Consumable(
    name: String,
    val effects: List<ItemEffect>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : ShopItem(name, price, rarity, level) {
    override val description: String get() = effects.joinToString("，") { it.description }
}

// --- 具體消耗品類別 (維持向後兼容) ---

class HealthPotion(
    name: String = "生命藥水",
    val healAmount: Int,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Consumable(name, listOf(HealEffect(healAmount)), price, rarity, level)

class StatShard(
    name: String,
    val hpBonus: Int = 0,
    val attackBonus: Int = 0,
    price: Int,
    rarity: Rarity,
    level: Int = 1
) : Consumable(
    name,
    mutableListOf<ItemEffect>().apply {
        if (hpBonus != 0) add(PermanentStatEffect(StatModifier(StatType.HP, hpBonus.toDouble())))
        if (attackBonus != 0) add(PermanentStatEffect(StatModifier(StatType.ATTACK, attackBonus.toDouble())))
    },
    price, rarity, level
)

/**
 * 裝備基類
 */
sealed class Equipment(
    name: String,
    val stats: List<StatModifier>,
    price: Int,
    rarity: Rarity,
    level: Int = 1
) : ShopItem(name, price, rarity, level) {
    override val description: String get() = stats.joinToString("，") { it.toString() }
    
    fun getBonus(type: StatType): Double = stats.filter { it.type == type }.sumOf { it.value }

    // 鍛造或特殊標籤可存放於此
    val metadata = mutableMapOf<String, Any>()
}

open class Weapon(
    name: String,
    stats: List<StatModifier>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Equipment(name, stats, price, rarity, level) {
    // 向後兼容 attackBonus 屬性
    val attackBonus: Int get() = getBonus(StatType.ATTACK).toInt()
}

open class Armor(
    name: String,
    stats: List<StatModifier>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Equipment(name, stats, price, rarity, level) {
    // 向後兼容 hpBonus 屬性
    val hpBonus: Int get() = getBonus(StatType.HP).toInt()
}

// --- 具體裝備類別 (維持向後兼容) ---

class Sword(name: String, attack: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : 
    Weapon(name, listOf(StatModifier(StatType.ATTACK, attack.toDouble())), price, rarity, level)

class Axe(name: String, attack: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : 
    Weapon(name, listOf(StatModifier(StatType.ATTACK, attack.toDouble())), price, rarity, level)

class Staff(name: String, attack: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : 
    Weapon(name, listOf(StatModifier(StatType.ATTACK, attack.toDouble())), price, rarity, level)

class LightArmor(name: String, hp: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : 
    Armor(name, listOf(StatModifier(StatType.HP, hp.toDouble())), price, rarity, level)

class HeavyArmor(name: String, hp: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : 
    Armor(name, listOf(StatModifier(StatType.HP, hp.toDouble())), price, rarity, level)
