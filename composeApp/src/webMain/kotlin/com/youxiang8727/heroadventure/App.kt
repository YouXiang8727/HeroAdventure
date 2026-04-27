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
import com.youxiang8727.heroadventure.model.GameState
import com.youxiang8727.heroadventure.model.Monster
import com.youxiang8727.heroadventure.model.Shop
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
        
        var stagesWithoutShop by remember { mutableStateOf(0) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = gameState) {
                is GameState.CharacterSelection -> {
                    CharacterSelectionScreen(onCharacterSelected = { hero ->
                        val firstMonster = Monster.createRandom(1)
                        stagesWithoutShop = 0
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
                                state.hero.onVictory(gold)
                                
                                val nextLevel = state.stageLevel + 1
                                val shopChance = 0.20 + (stagesWithoutShop * 0.10)
                                
                                if (Random.nextDouble() < shopChance) {
                                    stagesWithoutShop = 0
                                    gameState = GameState.Shop(
                                        hero = state.hero,
                                        items = Shop.generateItems(nextLevel),
                                        nextStageLevel = nextLevel
                                    )
                                } else {
                                    stagesWithoutShop++
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
