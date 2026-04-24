package com.youxiang8727.heroadventure.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

sealed class GameState {
    object CharacterSelection : GameState()
    data class Battle(
        val hero: Hero,
        val monster: Monster,
        val stageLevel: Int
    ) : GameState()
    data class Shop(
        val hero: Hero,
        val items: List<ShopItem>,
        val nextStageLevel: Int
    ) : GameState()
    data class GameOver(val finalStage: Int, val heroClass: HeroClass) : GameState()
}

class Hero(
    val heroClass: HeroClass,
    initialLevel: Int = 1,
    initialHp: Int,
    initialMaxHp: Int,
    initialAttack: Int,
    initialGold: Int = 0
) {
    var level by mutableStateOf(initialLevel)
    var currentHp by mutableStateOf(initialHp)
    var maxHp by mutableStateOf(initialMaxHp)
    var attack by mutableStateOf(initialAttack)
    var gold by mutableStateOf(initialGold)

    // 能量系統
    var energy by mutableStateOf(0)
    val maxEnergy: Int get() = heroClass.activeSkill.energyRequired

    // 戰鬥狀態
    var isWarriorBuffActive by mutableStateOf(false)
    var isMageNextDoubleActive by mutableStateOf(false)
    var isRogueCritBuffActive by mutableStateOf(false)
    var shieldHp by mutableStateOf(0)

    val inventory = mutableStateListOf<ShopItem>()
    
    var weapon by mutableStateOf<Weapon?>(null)
    var armor by mutableStateOf<Armor?>(null)

    val baseWithEquipAttack: Int get() = attack + (weapon?.attackBonus ?: 0)
    val attackPassiveBonus: Int get() = heroClass.getPassiveAttackBonus(baseWithEquipAttack, currentHp, totalMaxHp)
    val totalAttack: Int get() = baseWithEquipAttack + attackPassiveBonus
    val totalMaxHp: Int get() = maxHp + (armor?.hpBonus ?: 0)
    
    val totalCritRate: Double get() = heroClass.critRate
    val totalBlockRate: Double get() = heroClass.blockRate

    fun takeDamage(damage: Int) {
        val actualDamage = if (shieldHp > 0) {
            val absorbed = damage.coerceAtMost(shieldHp)
            shieldHp -= absorbed
            damage - absorbed
        } else damage

        if (actualDamage > 0) {
            currentHp = (currentHp - actualDamage).coerceAtLeast(0)
        }
    }

    /** 強制扣血 (無視護盾) */
    fun takeDamageRaw(damage: Int) {
        currentHp = (currentHp - damage).coerceAtLeast(1)
    }

    fun heal(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(totalMaxHp)
    }

    fun gainEnergy(amount: Int = 1) {
        energy = (energy + amount).coerceAtMost(maxEnergy)
    }

    fun resetEnergy() {
        energy = 0
    }

    fun onAfterAttack(damage: Int, isCrit: Boolean): Int {
        var totalHeal = heroClass.getLifestealAmount(damage, isCrit)
        
        if (heroClass is HeroClass.Rogue && isRogueCritBuffActive && isCrit) {
            totalHeal += (damage * 0.3).roundToInt()
        }
        
        if (totalHeal > 0) {
            heal(totalHeal)
        }
        return totalHeal
    }

    fun onVictory(goldEarned: Int) {
        gold += goldEarned
        levelUp()
        
        val healPercent = heroClass.getHealPercentOnVictory()
        if (healPercent > 0.0) {
            heal((totalMaxHp * healPercent).roundToInt())
        }
        clearBattleBuffs()
    }

    fun clearBattleBuffs() {
        isWarriorBuffActive = false
        isMageNextDoubleActive = false
        isRogueCritBuffActive = false
        shieldHp = 0
    }

    fun levelUp() {
        val oldMaxHp = totalMaxHp
        level++
        maxHp += heroClass.hpGrowth
        attack += heroClass.attackGrowth
        adjustHpProportionally(oldMaxHp)
    }

    private fun adjustHpProportionally(oldMaxHp: Int) {
        val newMaxHp = totalMaxHp
        if (currentHp >= oldMaxHp) {
            currentHp = newMaxHp
        } else if (oldMaxHp > 0) {
            val ratio = currentHp.toDouble() / oldMaxHp
            currentHp = (ratio * newMaxHp).roundToInt().coerceIn(1, newMaxHp)
        } else {
            currentHp = newMaxHp
        }
    }

    fun useItem(item: ShopItem) {
        val oldMaxHp = totalMaxHp
        when (item) {
            is HealthPotion -> {
                heal(item.healAmount)
                inventory.remove(item)
            }
            is StatShard -> {
                if (item.hpBonus > 0) {
                    maxHp += item.hpBonus
                    currentHp += item.hpBonus
                }
                if (item.attackBonus > 0) {
                    attack += item.attackBonus
                }
                inventory.remove(item)
            }
            is Equipment -> {
                equip(item)
                adjustHpProportionally(oldMaxHp)
            }
        }
    }

    private fun equip(equipment: Equipment) {
        when (equipment) {
            is Weapon -> {
                weapon?.let { inventory.add(it) }
                weapon = equipment
                inventory.remove(equipment)
            }
            is Armor -> {
                armor?.let { inventory.add(it) }
                armor = equipment
                inventory.remove(equipment)
            }
        }
    }
    
    fun unequipWeapon() {
        val oldMaxHp = totalMaxHp
        weapon?.let {
            inventory.add(it)
            weapon = null
            adjustHpProportionally(oldMaxHp)
        }
    }

    fun unequipArmor() {
        val oldMaxHp = totalMaxHp
        armor?.let {
            inventory.add(it)
            armor = null
            adjustHpProportionally(oldMaxHp)
        }
    }
}
