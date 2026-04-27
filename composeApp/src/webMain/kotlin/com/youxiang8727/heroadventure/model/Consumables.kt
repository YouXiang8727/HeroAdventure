package com.youxiang8727.heroadventure.model

import kotlin.random.Random

/**
 * 消耗品基類 (使用後消失)
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

/**
 * 生命藥水
 */
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
                Rarity.RARE -> 3.0 to 2.2
                Rarity.EPIC -> 8.0 to 5.0
                Rarity.LEGENDARY -> 20.0 to 10.0
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

/**
 * 屬性碎片
 */
class StatShard(
    name: String,
    price: Int,
    rarity: Rarity,
    level: Int = 1,
    effects: List<ItemEffect>
) : Consumable(name, effects, price, rarity, level) {
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
                    price = (120 * multiplier + level * 10).toInt(),
                    rarity = rarity,
                    level = level,
                    effects = listOf(PermanentStatEffect(StatModifier(StatType.ATTACK, attackBonus.toDouble())))
                )
            } else {
                val hpBonus = (20 * multiplier).toInt().coerceAtLeast(5)
                StatShard(
                    name = "${rarity.label}生命碎片",
                    price = (100 * multiplier + level * 8).toInt(),
                    rarity = rarity,
                    level = level,
                    effects = listOf(PermanentStatEffect(StatModifier(StatType.HP, hpBonus.toDouble())))
                )
            }
        }
    }
}
