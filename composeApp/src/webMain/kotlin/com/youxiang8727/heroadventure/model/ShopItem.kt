package com.youxiang8727.heroadventure.model

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

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

// --- 具體消耗品類別 ---

class HealthPotion(
    name: String = "生命藥水",
    val healAmount: Int,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Consumable(name, listOf(HealEffect(healAmount)), price, rarity, level) {
    companion object {
        fun createRandom(level: Int, rarity: Rarity = Rarity.COMMON): HealthPotion {
            val baseHeal = 40 + level * 8
            val basePrice = 40 + level * 6
            
            val (healMult, priceMult) = when (rarity) {
                Rarity.COMMON -> 1.0 to 1.0
                Rarity.RARE -> 2.5 to 2.2
                Rarity.EPIC -> 6.0 to 4.5
                Rarity.LEGENDARY -> 15.0 to 8.5
            }
            
            return HealthPotion(
                name = if (rarity == Rarity.COMMON) "普通生命藥水" else "${rarity.label}生命藥水",
                healAmount = (baseHeal * healMult).toInt(),
                price = (basePrice * priceMult).toInt(),
                rarity = rarity,
                level = level
            )
        }
    }
}

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
) {
    companion object {
        fun createRandom(level: Int): StatShard {
            val isAttackShard = Random.nextBoolean()
            val rarity = when (Random.nextDouble()) {
                in 0.0..0.7 -> Rarity.RARE
                in 0.7..0.92 -> Rarity.EPIC
                else -> Rarity.LEGENDARY
            }
            
            val multiplier = when (rarity) {
                Rarity.RARE -> 1.0
                Rarity.EPIC -> 2.5
                Rarity.LEGENDARY -> 6.0
                else -> 1.0
            }

            return if (isAttackShard) {
                val attackBonus = (4 * multiplier).toInt().coerceAtLeast(1)
                StatShard(
                    name = "${rarity.label}攻擊碎片",
                    attackBonus = attackBonus,
                    price = (120 * multiplier + level * 10).toInt(),
                    rarity = rarity,
                    level = level
                )
            } else {
                val hpBonus = (20 * multiplier).toInt().coerceAtLeast(5)
                StatShard(
                    name = "${rarity.label}生命碎片",
                    hpBonus = hpBonus,
                    price = (100 * multiplier + level * 8).toInt(),
                    rarity = rarity,
                    level = level
                )
            }
        }
    }
}

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

    val metadata = mutableMapOf<String, Any>()
    
    companion object {
        fun createRandom(level: Int): Equipment {
            val rarity = when (Random.nextDouble()) {
                in 0.0..0.5 -> Rarity.COMMON
                in 0.5..0.8 -> Rarity.RARE
                in 0.8..0.95 -> Rarity.EPIC
                else -> Rarity.LEGENDARY
            }
            
            val multiplier = when (rarity) {
                Rarity.COMMON -> 1.0
                Rarity.RARE -> 2.0
                Rarity.EPIC -> 4.0
                Rarity.LEGENDARY -> 8.0
            }
            
            val isWeapon = Random.nextBoolean()
            return if (isWeapon) {
                val weaponType = Random.nextInt(3)
                val baseAttack = (12 + level * 3) * multiplier
                val price = (60 + level * 8) * multiplier
                
                when (weaponType) {
                    0 -> Sword("${rarity.label}長劍", baseAttack.toInt(), price.toInt(), rarity, level)
                    1 -> Axe("${rarity.label}戰斧", (baseAttack * 1.2).toInt(), (price * 1.1).toInt(), rarity, level)
                    else -> Staff("${rarity.label}法杖", (baseAttack * 0.9).toInt(), (price * 0.9).toInt(), rarity, level)
                }
            } else {
                val armorType = Random.nextInt(2)
                val baseHp = (45 + level * 10) * multiplier
                val price = (55 + level * 7) * multiplier
                
                when (armorType) {
                    0 -> LightArmor("${rarity.label}皮甲", baseHp.toInt(), price.toInt(), rarity, level)
                    else -> HeavyArmor("${rarity.label}板甲", (baseHp * 1.5).toInt(), (price * 1.3).toInt(), rarity, level)
                }
            }
        }
    }
}

open class Weapon(
    name: String,
    stats: List<StatModifier>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Equipment(name, stats, price, rarity, level) {
    val attackBonus: Int get() = getBonus(StatType.ATTACK).toInt()
}

open class Armor(
    name: String,
    stats: List<StatModifier>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Equipment(name, stats, price, rarity, level) {
    val hpBonus: Int get() = getBonus(StatType.HP).toInt()
}

// --- 具體裝備類別 ---

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
