package com.youxiang8727.heroadventure.model

import androidx.compose.ui.graphics.Color

enum class Rarity(val label: String, val color: Color) {
    COMMON("普通", Color(0xFF9E9E9E)),
    RARE("稀有", Color(0xFF2196F3)),
    EPIC("史詩", Color(0xFF9C27B0)),
    LEGENDARY("傳說", Color(0xFFFF9800))
}

sealed class ShopItem(
    val name: String,
    val description: String,
    val price: Int,
    val rarity: Rarity = Rarity.COMMON,
    val level: Int = 1 // 新增等級屬性
)

sealed class Consumable(
    name: String,
    description: String,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : ShopItem(name, description, price, rarity, level)

class HealthPotion(
    name: String = "生命藥水",
    val healAmount: Int,
    price: Int,
    rarity: Rarity = Rarity.COMMON,
    level: Int = 1
) : Consumable(name, "回復 $healAmount 點生命值", price, rarity, level)

class StatShard(
    name: String,
    val hpBonus: Int = 0,
    val attackBonus: Int = 0,
    price: Int,
    rarity: Rarity,
    level: Int = 1
) : Consumable(name, if (hpBonus > 0) "永久最大生命 +$hpBonus" else "永久攻擊力 +$attackBonus", price, rarity, level)

sealed class Equipment(
    name: String,
    description: String,
    price: Int,
    rarity: Rarity,
    level: Int = 1
) : ShopItem(name, description, price, rarity, level)

sealed class Weapon(
    name: String,
    val attackBonus: Int,
    price: Int,
    rarity: Rarity,
    level: Int = 1
) : Equipment(name, "攻擊力 +$attackBonus", price, rarity, level)

class Sword(name: String, attack: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : Weapon(name, attack, price, rarity, level)
class Axe(name: String, attack: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : Weapon(name, attack, price, rarity, level)
class Staff(name: String, attack: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : Weapon(name, attack, price, rarity, level)

sealed class Armor(
    name: String,
    val hpBonus: Int,
    price: Int,
    rarity: Rarity,
    level: Int = 1
) : Equipment(name, "最大生命 +$hpBonus", price, rarity, level)

class LightArmor(name: String, hp: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : Armor(name, hp, price, rarity, level)
class HeavyArmor(name: String, hp: Int, price: Int, rarity: Rarity = Rarity.COMMON, level: Int = 1) : Armor(name, hp, price, rarity, level)
