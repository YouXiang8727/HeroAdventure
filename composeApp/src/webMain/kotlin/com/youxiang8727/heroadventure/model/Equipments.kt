package com.youxiang8727.heroadventure.model

import kotlin.random.Random

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

/**
 * 武器類
 */
open class Weapon(
    name: String,
    stats: List<StatModifier>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Equipment(name, stats, price, rarity, level) {
    val attackBonus: Int get() = getBonus(StatType.ATTACK).toInt()
}

/**
 * 防具類
 */
open class Armor(
    name: String,
    stats: List<StatModifier>,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Equipment(name, stats, price, rarity, level) {
    val hpBonus: Int get() = getBonus(StatType.HP).toInt()
}

// --- 具體裝備 ---

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
