package com.youxiang8727.heroadventure.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

enum class MonsterType(
    val monsterName: String,
    val baseHp: Int,
    val baseAttack: Int,
    val goldDropRange: IntRange
) {
    SLIME("史萊姆", 40, 6, 5..10),
    GOBLIN("哥布林", 65, 12, 10..20),
    ORC("獸人", 120, 18, 25..40),
    SKELETON("骷髏兵", 85, 15, 15..25),
    DRAGON("幼龍", 300, 35, 100..200)
}

class Monster(
    val type: MonsterType,
    val maxHp: Int,
    initialHp: Int,
    val attack: Int,
    val goldDrop: Int
) {
    var currentHp by mutableStateOf(initialHp)

    companion object {
        fun createRandom(level: Int): Monster {
            val type = when {
                level >= 10 -> MonsterType.DRAGON
                level >= 7 -> if (Random.nextBoolean()) MonsterType.ORC else MonsterType.DRAGON
                level >= 4 -> listOf(MonsterType.GOBLIN, MonsterType.ORC, MonsterType.SKELETON).random()
                else -> if (Random.nextBoolean()) MonsterType.SLIME else MonsterType.GOBLIN
            }
            
            // 隨難度提升數值
            val difficultyMultiplier = 1.0 + (level - 1) * 0.2
            val hp = (type.baseHp * difficultyMultiplier).toInt()
            val atk = (type.baseAttack * difficultyMultiplier).toInt()
            val gold = (type.goldDropRange.random() * difficultyMultiplier).toInt()
            
            return Monster(type, hp, hp, atk, gold)
        }
    }
}
