package com.youxiang8727.heroadventure.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.pow
import kotlin.random.Random

/**
 * 怪物能力/特性介面
 */
sealed interface MonsterAbility {
    val name: String
    val description: String
}

data class LifeSteal(val rate: Double) : MonsterAbility {
    override val name: String = "吸血"
    override val description: String = "攻擊時恢復傷害 ${(rate * 100).toInt()}% 的生命值"
}

data class Thorns(val rate: Double) : MonsterAbility {
    override val name: String = "反傷"
    override val description: String = "受到攻擊時，反彈傷害 ${(rate * 100).toInt()}% 的數值"
}

data class Regen(val rate: Double) : MonsterAbility {
    override val name: String = "再生"
    override val description: String = "每回合恢復最大生命值 ${(rate * 100).toInt()}% 的生命值"
}

data class Berserk(val threshold: Double = 0.5, val multiplier: Double = 1.5) : MonsterAbility {
    override val name: String = "狂暴"
    override val description: String = "生命值低於 ${(threshold * 100).toInt()}% 時，攻擊力提升 ${(multiplier * 100).toInt()}%"
}

enum class MonsterType(
    val monsterName: String,
    val baseHp: Int,
    val baseAttack: Int,
    val goldDropRange: IntRange,
    val abilities: List<MonsterAbility> = emptyList()
) {
    SLIME("史萊姆", 40, 6, 8..15, listOf(Regen(0.03))),
    GOBLIN("哥布林", 65, 12, 15..25),
    SKELETON("骷髏兵", 90, 16, 25..40, listOf(Thorns(0.1))),
    ORC("獸人勇士", 150, 24, 45..70, listOf(LifeSteal(0.12))),
    DRAGON("幼龍", 350, 38, 150..300, listOf(LifeSteal(0.15), Regen(0.04), Berserk(0.4, 1.4))),
    CHIMERA("奇美拉", 600, 50, 500..800, listOf(Regen(0.05), Thorns(0.15), LifeSteal(0.1), Berserk(0.5, 1.8))),
    DITTO_BASIC("初階變形蟲", 100, 10, 30..50),
    DITTO_ADVANCED("高階變形蟲", 200, 20, 60..100, listOf(Regen(0.05))),
    DITTO_ELITE("菁英變形蟲", 400, 40, 150..250, listOf(Regen(0.08), Thorns(0.1)))
}

class Monster(
    val type: MonsterType,
    val maxHp: Int,
    initialHp: Int,
    val baseAttack: Int,
    val goldDrop: Int,
    val abilities: List<MonsterAbility> = emptyList()
) {
    var currentHp by mutableStateOf(initialHp)

    /** 
     * 動態計算攻擊力，若具備「狂暴」且血量低於門檻則提升 
     */
    val attack: Int get() {
        val berserk = abilities.filterIsInstance<Berserk>().firstOrNull()
        return if (berserk != null && currentHp <= maxHp * berserk.threshold) {
            (baseAttack * berserk.multiplier).toInt()
        } else {
            baseAttack
        }
    }

    /**
     * 是否處於狂暴狀態
     */
    val isBerserking: Boolean get() {
        val berserk = abilities.filterIsInstance<Berserk>().firstOrNull()
        return berserk != null && currentHp <= maxHp * berserk.threshold
    }

    fun heal(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
    }

    companion object {
        fun createRandom(level: Int, hero: Hero): Monster {
            // 首先判斷是否為變形蟲 (極低機率 3%)
            val isDitto = Random.nextDouble() < 0.03
            
            val type = if (isDitto) {
                val dittoRand = Random.nextDouble()
                when {
                    dittoRand < 0.48 -> MonsterType.DITTO_BASIC
                    dittoRand < 0.48 + 0.37 -> MonsterType.DITTO_ADVANCED
                    else -> MonsterType.DITTO_ELITE
                }
            } else {
                // 一般怪物的關卡分佈邏輯
                when {
                    level < 5 -> {
                        if (Random.nextDouble() < 0.7) MonsterType.SLIME else MonsterType.GOBLIN
                    }
                    level < 10 -> {
                        listOf(MonsterType.GOBLIN, MonsterType.SKELETON, MonsterType.ORC).random()
                    }
                    level < 20 -> {
                        val rand = Random.nextDouble()
                        when {
                            rand < 0.1 -> MonsterType.DRAGON
                            rand < 0.4 -> MonsterType.ORC
                            else -> MonsterType.SKELETON
                        }
                    }
                    level < 35 -> {
                        val rand = Random.nextDouble()
                        when {
                            rand < 0.3 -> MonsterType.DRAGON
                            rand < 0.6 -> MonsterType.ORC
                            else -> MonsterType.SKELETON
                        }
                    }
                    else -> {
                        val rand = Random.nextDouble()
                        when {
                            rand < 0.2 -> MonsterType.CHIMERA
                            rand < 0.5 -> MonsterType.DRAGON
                            else -> listOf(MonsterType.ORC, MonsterType.SKELETON).random()
                        }
                    }
                }
            }
            
            // 成長曲線調整
            val difficultyMultiplier = if (level <= 30) {
                1.0 + (level - 1) * 0.12
            } else {
                val valAt30 = 1.0 + (30 - 1) * 0.12
                valAt30 + (level - 30) * 0.40
            }

            val variance = Random.nextDouble(0.95, 1.05)
            
            var hp = (type.baseHp * difficultyMultiplier * variance).toInt()
            var atk = (type.baseAttack * difficultyMultiplier * variance).toInt()

            // 根據怪物類型決定是否套用變形蟲的數值複製邏輯
            when (type) {
                MonsterType.DITTO_BASIC -> {
                    hp = (hero.totalMaxHp * 0.7).toInt()
                    atk = (hero.totalAttack * 0.7).toInt()
                }
                MonsterType.DITTO_ADVANCED -> {
                    hp = (hero.totalMaxHp * 1.0).toInt()
                    atk = (hero.totalAttack * 1.0).toInt()
                }
                MonsterType.DITTO_ELITE -> {
                    hp = (hero.totalMaxHp * 1.2).toInt()
                    atk = (hero.totalAttack * 1.2).toInt()
                }
                else -> {}
            }

            val gold = (type.goldDropRange.random() * difficultyMultiplier).toInt()
            
            return Monster(type, hp, hp, atk, gold, type.abilities)
        }
    }
}
