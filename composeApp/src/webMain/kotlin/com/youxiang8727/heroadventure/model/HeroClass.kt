package com.youxiang8727.heroadventure.model

sealed class HeroClass(
    val className: String,
    val baseHp: Int,
    val hpGrowth: Int,
    val baseAttack: Int,
    val attackGrowth: Int,
    val critRate: Double = 0.0,
    val blockRate: Double = 0.0,
    val passiveName: String,
    val passiveDescription: String,
    val activeSkill: ActiveSkill,
    val startingWeapon: String,
    val startingArmor: String? = null
) {
    data object Warrior : HeroClass(
        "戰士", 280, 35, 22, 6, 
        critRate = 0.08, blockRate = 0.15,
        passiveName = "破釜沈舟",
        passiveDescription = "生命越低攻擊越高(每失去1%生命+0.8%攻擊)；且每次造成傷害恢復8%血量。",
        activeSkill = ActiveSkill(
            name = "血祭衝擊",
            description = "消耗 10% 目前生命，本回合獲得 20% 額外格擋，且下次攻擊傷害提升 100%",
            energyRequired = 2
        ),
        startingWeapon = "生鏽長劍 (+5 ATK)"
    ) {
        const val ATTACK_BONUS_PER_LOST_HP = 0.8
        const val LIFE_STEAL_PERCENT = 0.08
        const val ACTIVE_SKILL_ATTACK_BUFFER = 2.0
    }

    data object Mage : HeroClass(
        "法師", 150, 25, 30, 8, 
        critRate = 0.10, blockRate = 0.0,
        passiveName = "法術回響",
        passiveDescription = "攻擊時有 25% 的機率連續發動兩次攻擊。",
        activeSkill = ActiveSkill(
            name = "魔力超載",
            description = "下一次攻擊必定觸發連擊，且傷害提升。",
            energyRequired = 3
        ),
        startingWeapon = "學徒法杖 (+8 ATK)"
    ) {
        const val DOUBLE_ATTACK_CHANCE = 0.25
    }

    data object Rogue : HeroClass(
        "刺客", 180, 25, 24, 6, 
        critRate = 0.25, blockRate = 0.05,
        passiveName = "致命打擊",
        passiveDescription = "暴擊傷害倍率由 2 倍提升至 3 倍。",
        activeSkill = ActiveSkill(
            name = "尋隙一擊",
            description = "本回合暴擊率提升 50%，若暴擊則吸血 30%。",
            energyRequired = 3
        ),
        startingWeapon = "新手匕首 (+4 ATK)"
    ) {
        const val CRIT_MULTIPLIER = 3.0
        const val CRIT_LIFE_STEAL = 0.3
    }

    data object Paladin : HeroClass(
        "聖騎士", 320, 45, 14, 4, 
        critRate = 0.02, blockRate = 0.25,
        passiveName = "聖光治癒",
        passiveDescription = "每場戰鬥勝利後，恢復 10% 最大生命值。",
        activeSkill = ActiveSkill(
            name = "聖光制裁",
            description = "造成 [攻擊力 + 最大生命 × 格擋率] 的神聖傷害，並立即觸發一次『聖光治癒』效果。",
            energyRequired = 5
        ),
        startingWeapon = "木盾",
        startingArmor = "舊皮甲 (+30 HP)"
    ) {
        const val HEAL_PERCENT_ON_VICTORY = 0.10
    }

    data object Archer : HeroClass(
        "弓箭手", 160, 22, 26, 7, 
        critRate = 0.18, blockRate = 0.08,
        passiveName = "精準閃避",
        passiveDescription = "攻擊時有 15% 的機率閃避敵人的反擊傷害。",
        activeSkill = ActiveSkill(
            name = "疾風連射",
            description = "立即射擊 4 次，且閃避反擊機率大幅提升。",
            energyRequired = 3
        ),
        startingWeapon = "獵人短劍 (+6 ATK)"
    ) {
        const val DODGE_CHANCE = 0.15
    }

    companion object {
        val entries = listOf(Warrior, Mage, Rogue, Paladin, Archer)
    }

    fun getHpAtLevel(level: Int): Int = baseHp + (level - 1) * hpGrowth
    fun getAttackAtLevel(level: Int): Int = baseAttack + (level - 1) * attackGrowth
}

data class ActiveSkill(
    val name: String,
    val description: String,
    val energyRequired: Int
)
