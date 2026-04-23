package com.youxiang8727.heroadventure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.youxiang8727.heroadventure.model.*
import com.youxiang8727.heroadventure.ui.BattleScreen
import com.youxiang8727.heroadventure.ui.CharacterSelectionScreen
import com.youxiang8727.heroadventure.ui.ShopScreen
import heroadventure.composeapp.generated.resources.JiuXiMingFanJian_2
import heroadventure.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font
import kotlin.random.Random

@Composable
fun App() {
    val myFontFamily = FontFamily(Font(Res.font.JiuXiMingFanJian_2))

    val defaultTypography = MaterialTheme.typography
    val typography = Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = myFontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = myFontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = myFontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = myFontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = myFontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = myFontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = myFontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = myFontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = myFontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = myFontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = myFontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = myFontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = myFontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = myFontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = myFontFamily)
    )

    // 定義深色主題配色
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFE94560),
        secondary = Color(0xFF0F3460),
        background = Color(0xFF1A1A2E),
        surface = Color(0xFF16213E),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = typography
    ) {
        var gameState by remember { mutableStateOf<GameState>(GameState.CharacterSelection) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = gameState) {
                is GameState.CharacterSelection -> {
                    CharacterSelectionScreen(onCharacterSelected = { hero ->
                        val firstMonster = Monster.createRandom(1)
                        gameState = GameState.Battle(hero, firstMonster, 1)
                    })
                }
                is GameState.Battle -> {
                    key(state.stageLevel) {
                        BattleScreen(
                            hero = state.hero,
                            monster = state.monster,
                            stageLevel = state.stageLevel,
                            onVictory = { gold ->
                                state.hero.gold += gold
                                state.hero.levelUp()
                                
                                val hClass = state.hero.heroClass
                                if (hClass is HeroClass.Paladin) {
                                    val healAmount = (state.hero.totalMaxHp * hClass.HEAL_PERCENT_ON_VICTORY).toInt()
                                    state.hero.heal(healAmount)
                                }
                                
                                val nextLevel = state.stageLevel + 1
                                
                                if (Random.nextDouble() < 0.25) {
                                    gameState = GameState.Shop(
                                        hero = state.hero,
                                        items = generateRandomShopItems(nextLevel),
                                        nextStageLevel = nextLevel
                                    )
                                } else {
                                    val nextMonster = Monster.createRandom(nextLevel)
                                    gameState = GameState.Battle(state.hero, nextMonster, nextLevel)
                                }
                            },
                            onDefeat = {
                                gameState = GameState.GameOver(state.stageLevel, state.hero.heroClass)
                            }
                        )
                    }
                }
                is GameState.Shop -> {
                    ShopScreen(
                        hero = state.hero,
                        shopItems = state.items,
                        onContinue = {
                            val nextMonster = Monster.createRandom(state.nextStageLevel)
                            gameState = GameState.Battle(state.hero, nextMonster, state.nextStageLevel)
                        }
                    )
                }
                is GameState.GameOver -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("遊戲結束", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("您的 ${state.heroClass.className} 最終到達了第 ${state.finalStage} 關", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { gameState = GameState.CharacterSelection }) {
                            Text("回到標題")
                        }
                    }
                }
            }
        }
    }
}

private fun generateRandomShopItems(level: Int): List<ShopItem> {
    val items = mutableListOf<ShopItem>()
    
    val basicHeal = 30 + level * 2
    items.add(HealthPotion(
        name = "普通生命藥水",
        healAmount = basicHeal,
        price = 20 + level,
        rarity = Rarity.COMMON
    ))
    
    if (Random.nextDouble() < 0.5) {
        val potionRarity = when (Random.nextDouble()) {
            in 0.0..0.6 -> Rarity.RARE
            in 0.6..0.9 -> Rarity.EPIC
            else -> Rarity.LEGENDARY
        }
        
        val potionHealMultiplier = when (potionRarity) {
            Rarity.COMMON -> 1.0
            Rarity.RARE -> 2.0
            Rarity.EPIC -> 4.0
            Rarity.LEGENDARY -> 8.0
        }
        
        val baseHeal = 20 + level * 2
        items.add(HealthPotion(
            name = "${potionRarity.label}生命藥水",
            healAmount = (baseHeal * potionHealMultiplier).toInt(),
            price = (15 * potionHealMultiplier + level).toInt(),
            rarity = potionRarity
        ))
    }

    if (Random.nextDouble() < 0.5) {
        val isAttackShard = Random.nextBoolean()
        val rarity = when (Random.nextDouble()) {
            in 0.0..0.7 -> Rarity.RARE
            in 0.7..0.95 -> Rarity.EPIC
            else -> Rarity.LEGENDARY
        }
        
        val multiplier = when (rarity) {
            Rarity.RARE -> 1.0
            Rarity.EPIC -> 2.0
            Rarity.LEGENDARY -> 4.0
            else -> 1.0
        }

        if (isAttackShard) {
            val attackBonus = (3 * multiplier).toInt().coerceAtLeast(1)
            items.add(
                StatShard(
                    name = "${rarity.label}攻擊碎片",
                    attackBonus = attackBonus,
                    price = (50 * multiplier + level * 5).toInt(),
                    rarity = rarity
                )
            )
        } else {
            val hpBonus = (15 * multiplier).toInt().coerceAtLeast(5)
            items.add(StatShard(
                name = "${rarity.label}生命碎片",
                hpBonus = hpBonus,
                price = (40 * multiplier + level * 4).toInt(),
                rarity = rarity
            ))
        }
    }
    
    val equipmentCount = Random.nextInt(2, 4)
    repeat(equipmentCount) {
        val rarity = when (Random.nextDouble()) {
            in 0.0..0.6 -> Rarity.COMMON
            in 0.6..0.85 -> Rarity.RARE
            in 0.85..0.97 -> Rarity.EPIC
            else -> Rarity.LEGENDARY
        }
        
        val multiplier = when (rarity) {
            Rarity.COMMON -> 1.0
            Rarity.RARE -> 1.5
            Rarity.EPIC -> 2.5
            Rarity.LEGENDARY -> 4.5
        }
        
        val isWeapon = Random.nextBoolean()
        if (isWeapon) {
            val weaponType = Random.nextInt(3)
            val baseAttack = (10 + level * 2) * multiplier
            val price = (40 + level * 5) * multiplier
            
            items.add(when (weaponType) {
                0 -> Sword("${rarity.label}長劍", baseAttack.toInt(), price.toInt(), rarity, level)
                1 -> Axe(
                    "${rarity.label}戰斧",
                    (baseAttack * 1.2).toInt(),
                    (price * 1.1).toInt(),
                    rarity,
                    level
                )
                else -> Staff(
                    "${rarity.label}法杖",
                    (baseAttack * 0.9).toInt(),
                    (price * 0.9).toInt(),
                    rarity,
                    level
                )
            })
        } else {
            val armorType = Random.nextInt(2)
            val baseHp = (40 + level * 8) * multiplier
            val price = (35 + level * 4) * multiplier
            
            items.add(when (armorType) {
                0 -> LightArmor("${rarity.label}皮甲", baseHp.toInt(), price.toInt(), rarity, level)
                else -> HeavyArmor(
                    "${rarity.label}板甲",
                    (baseHp * 1.5).toInt(),
                    (price * 1.3).toInt(),
                    rarity,
                    level
                )
            })
        }
    }

    return items
}
