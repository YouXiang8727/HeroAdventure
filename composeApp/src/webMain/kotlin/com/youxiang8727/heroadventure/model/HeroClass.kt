package com.youxiang8727.heroadventure.model

import kotlin.math.roundToInt

sealed class HeroClass(
    val className: String,
    val baseHp: Int,
    val hpGrowth: Int,
    val baseAttack: Int,
    val attackGrowth: Int,
    val critRate: Double = 0.0,
    val blockRate: Double = 0.0,
    val passiveName: String,
    val startingWeapon: String,
    val startingArmor: String? = null
) {
    /** 被動描述 (由子類別根據參數動態生成) */
    abstract val passiveDescription: String
    
    /** 主動技能 (由子類別根據參數動態生成) */
    abstract val activeSkill: ActiveSkill

    /** 獲取暴擊傷害倍率 (預設 2.0) */
    open fun getCritMultiplier(): Double = 2.0

    /** 計算被動攻擊力加成 */
    open fun getPassiveAttackBonus(baseAttack: Int, currentHp: Int, totalMaxHp: Int): Int = 0

    /** 計算造成傷害後的吸血/恢復量 */
    open fun getLifestealAmount(damage: Int, isCrit: Boolean): Int = 0

    /** 獲取戰鬥勝利後的恢復比例 */
    open fun getHealPercentOnVictory(): Double = 0.0

    /** 獲取基礎閃避機率 */
    open fun getDodgeChance(): Double = 0.0

    /** 獲取額外連擊機率 */
    open fun getDoubleAttackChance(): Double = 0.0

    /** 獲取主動技能帶來的傷害加成倍率 */
    open fun getSkillAtkMultiplier(): Double = 1.0

    /** 執行主動技能效果 (回傳戰鬥日誌) */
    abstract fun executeActiveSkill(hero: Hero, monster: Monster): String

    data object Warrior : HeroClass(
        className = "戰士", baseHp = 300, hpGrowth = 45, baseAttack = 22, attackGrowth = 6,
        critRate = 0.08, blockRate = 0.15,
        passiveName = "破釜沈舟",
        startingWeapon = "生鏽長劍 (+5 ATK)"
    ) {
        const val HP_ATK_BONUS_RATE = 0.8
        const val LIFESTEAL_RATE = 0.08
        const val SKILL_SACRIFICE_RATE = 0.10
        const val SKILL_BLOCK_BONUS = 0.20
        const val SKILL_ATK_BUFF = 2.0
        const val ENERGY = 2

        override val passiveDescription = "生命越低攻擊越高(每失去1%生命+${HP_ATK_BONUS_RATE}%攻擊)；且每次造成傷害恢復${(LIFESTEAL_RATE * 100).toInt()}%血量。"
        override val activeSkill = ActiveSkill(
            name = "血祭衝擊",
            description = "消耗 ${(SKILL_SACRIFICE_RATE * 100).toInt()}% 目前生命，本回合獲得 ${(SKILL_BLOCK_BONUS * 100).toInt()}% 額外格擋，且下次攻擊傷害提升 ${(SKILL_ATK_BUFF * 100).toInt()}% 並無視防禦。",
            energyRequired = ENERGY
        )

        override fun getPassiveAttackBonus(baseAttack: Int, currentHp: Int, totalMaxHp: Int): Int {
            val lostHpPercent = (totalMaxHp - currentHp).toDouble() / totalMaxHp.coerceAtLeast(1)
            return (baseAttack * (lostHpPercent * HP_ATK_BONUS_RATE)).roundToInt()
        }
        override fun getLifestealAmount(damage: Int, isCrit: Boolean): Int = (damage * LIFESTEAL_RATE).roundToInt()
        override fun getSkillAtkMultiplier(): Double = SKILL_ATK_BUFF
        
        override fun executeActiveSkill(hero: Hero, monster: Monster): String {
            val sacrifice = (hero.currentHp * SKILL_SACRIFICE_RATE).toInt()
            hero.takeDamageRaw(sacrifice)
            hero.isWarriorBuffActive = true
            return "🩸 獻祭生命，力量與守護湧現！"
        }
    }

    data object Mage : HeroClass(
        className = "法師", baseHp = 150, hpGrowth = 30, baseAttack = 28, attackGrowth = 7,
        critRate = 0.10, blockRate = 0.0,
        passiveName = "元素共鳴",
        startingWeapon = "學徒法杖 (+8 ATK)"
    ) {
        const val DOUBLE_ATTACK_CHANCE = 0.30
        const val SKILL_ATK_MULTIPLIER = 3.0
        const val ENERGY = 3

        override val passiveDescription = "攻擊時有 ${(DOUBLE_ATTACK_CHANCE * 100).toInt()}% 的機率連續發動兩次攻擊。"
        override val activeSkill = ActiveSkill(
            name = "魔力超載",
            description = "引導下一發法術，使其傷害提升至 ${SKILL_ATK_MULTIPLIER.toInt()} 倍，且下回合必定觸發連擊。",
            energyRequired = ENERGY
        )

        override fun getDoubleAttackChance(): Double = DOUBLE_ATTACK_CHANCE
        override fun getSkillAtkMultiplier(): Double = SKILL_ATK_MULTIPLIER

        override fun executeActiveSkill(hero: Hero, monster: Monster): String {
            hero.isMageNextDoubleActive = true
            hero.isWarriorBuffActive = true
            return "🌀 魔力高度壓縮！準備施放毀滅性的一擊！"
        }
    }

    data object Rogue : HeroClass(
        className = "刺客", baseHp = 180, hpGrowth = 25, baseAttack = 30, attackGrowth = 8,
        critRate = 0.25, blockRate = 0.05,
        passiveName = "暗影步",
        startingWeapon = "新手匕首 (+4 ATK)"
    ) {
        const val CRIT_MULT = 3.0
        const val BASE_DODGE_CHANCE = 0.15
        const val SKILL_CRIT_CHANCE_BONUS = 0.70
        const val SKILL_LIFE_STEAL_RATE = 0.50
        const val ENERGY = 3

        override val passiveDescription = "爆擊倍率為 ${CRIT_MULT.toInt()} 倍，且擁有 ${(BASE_DODGE_CHANCE * 100).toInt()}% 基礎閃避率。"
        override val activeSkill = ActiveSkill(
            name = "尋隙一擊",
            description = "本回合暴擊率提升 ${(SKILL_CRIT_CHANCE_BONUS * 100).toInt()}%，若暴擊則吸血 ${(SKILL_LIFE_STEAL_RATE * 100).toInt()}%。",
            energyRequired = ENERGY
        )

        override fun getCritMultiplier(): Double = CRIT_MULT
        override fun getDodgeChance(): Double = BASE_DODGE_CHANCE
        override fun executeActiveSkill(hero: Hero, monster: Monster): String {
            hero.isRogueCritBuffActive = true
            return "🗡️ 鎖定死角，致命一擊！"
        }
    }

    data object Paladin : HeroClass(
        className = "聖騎士", baseHp = 280, hpGrowth = 42, baseAttack = 14, attackGrowth = 4,
        critRate = 0.02, blockRate = 0.25,
        passiveName = "聖光治癒",
        startingWeapon = "木盾",
        startingArmor = "舊皮甲 (+30 HP)"
    ) {
        const val VICTORY_HEAL_RATE = 0.10
        const val ENERGY = 5

        override val passiveDescription = "每場戰鬥勝利後，恢復 ${(VICTORY_HEAL_RATE * 100).toInt()}% 最大生命值。"
        override val activeSkill = ActiveSkill(
            name = "聖光制裁",
            description = "造成 [攻擊力 + 最大生命 × 格擋率] 的神聖傷害，並立即觸發一次『聖光治癒』效果。",
            energyRequired = ENERGY
        )

        override fun getHealPercentOnVictory(): Double = VICTORY_HEAL_RATE
        override fun executeActiveSkill(hero: Hero, monster: Monster): String {
            val damage = (hero.totalAttack + hero.totalMaxHp * hero.totalBlockRate).toInt()
            monster.currentHp = (monster.currentHp - damage).coerceAtLeast(0)
            val healAmount = (hero.totalMaxHp * VICTORY_HEAL_RATE).toInt()
            hero.heal(healAmount)
            return "✨ 聖光制裁！造成 $damage 傷害並觸發聖光治癒恢復 $healAmount 生命"
        }
    }

    data object Archer : HeroClass(
        className = "弓箭手", baseHp = 200, hpGrowth = 28, baseAttack = 26, attackGrowth = 7, 
        critRate = 0.25, blockRate = 0.08,
        passiveName = "精準閃避",
        startingWeapon = "獵人短劍 (+6 ATK)"
    ) {
        const val DODGE_CHANCE = 0.20
        const val SKILL_SHOT_COUNT = 5
        const val SKILL_DAMAGE_RATE = 0.80
        const val ENERGY = 2

        override val passiveDescription = "擁有 ${(DODGE_CHANCE * 100).toInt()}% 的機率閃避反擊傷害。"
        override val activeSkill = ActiveSkill(
            name = "疾風連射",
            description = "立即射擊 $SKILL_SHOT_COUNT 次，每發造成 ${(SKILL_DAMAGE_RATE * 100).toInt()}% 傷害且無視反擊。",
            energyRequired = ENERGY
        )

        override fun getDodgeChance(): Double = DODGE_CHANCE
        override fun executeActiveSkill(hero: Hero, monster: Monster): String {
            return "🏹 疾風連射！"
        }
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
