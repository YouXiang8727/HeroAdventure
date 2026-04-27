package com.youxiang8727.heroadventure.model

/**
 * 物品效果介面
 */
sealed interface ItemEffect {
    val description: String
    fun apply(hero: Hero)
}

/**
 * 恢復效果
 */
data class HealEffect(val amount: Int) : ItemEffect {
    override val description: String = "回復 $amount 點生命值"
    override fun apply(hero: Hero) {
        hero.heal(amount)
    }
}

/**
 * 永久屬性加成效果
 */
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
