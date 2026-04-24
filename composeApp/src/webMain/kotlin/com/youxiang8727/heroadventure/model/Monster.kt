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
    SKELETON("骷髏兵", 85, 15, 15..25),
    ORC("獸人", 120, 18, 25..40),
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
                level < 4 -> {
                    if (Random.nextDouble() < 0.7) MonsterType.SLIME else MonsterType.GOBLIN
                }
                level < 7 -> {
                    listOf(MonsterType.GOBLIN, MonsterType.SKELETON, MonsterType.ORC).random()
                }
                level < 10 -> {
                    val rand = Random.nextDouble()
                    when {
                        rand < 0.2 -> MonsterType.DRAGON
                        rand < 0.6 -> MonsterType.ORC
                        else -> MonsterType.SKELETON
                    }
                }
                else -> {
                    val rand = Random.nextDouble()
                    when {
                        rand < 0.6 -> MonsterType.DRAGON
                        rand < 0.9 -> MonsterType.ORC
                        else -> MonsterType.SKELETON
                    }
                }
            }
            
            // 難度係數成長
            val difficultyMultiplier = 1.0 + (level - 1) * 0.15
            // 個體差異波動
            val variance = Random.nextDouble(0.9, 1.1)
            
            val hp = (type.baseHp * difficultyMultiplier * variance).toInt()
            val atk = (type.baseAttack * difficultyMultiplier * variance).toInt()
            val gold = (type.goldDropRange.random() * difficultyMultiplier).toInt()
            
            return Monster(type, hp, hp, atk, gold)
        }
    }
}
