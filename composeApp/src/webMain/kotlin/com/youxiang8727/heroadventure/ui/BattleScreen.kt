package com.youxiang8727.heroadventure.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youxiang8727.heroadventure.model.Armor
import com.youxiang8727.heroadventure.model.Consumable
import com.youxiang8727.heroadventure.model.Hero
import com.youxiang8727.heroadventure.model.HeroClass
import com.youxiang8727.heroadventure.model.HeroStat
import com.youxiang8727.heroadventure.model.LifeSteal
import com.youxiang8727.heroadventure.model.Monster
import com.youxiang8727.heroadventure.model.Regen
import com.youxiang8727.heroadventure.model.ShopItem
import com.youxiang8727.heroadventure.model.Thorns
import com.youxiang8727.heroadventure.model.Weapon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleScreen(
    hero: Hero,
    monster: Monster,
    stageLevel: Int,
    onVictory: (Int) -> Unit,
    onDefeat: () -> Unit
) {
    var battleLog by remember { mutableStateOf("遭遇 ${monster.type.monsterName}！") }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var isBattleOver by remember { mutableStateOf(false) }

    var isPassiveTriggered by remember { mutableStateOf(false) }
    val passiveScale by animateFloatAsState(
        targetValue = if (isPassiveTriggered) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val monsterShakeOffset = remember { Animatable(0f) }
    val heroShakeOffset = remember { Animatable(0f) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
            ) {
                Text(
                    "STAGE $stageLevel",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .graphicsLayer(translationX = monsterShakeOffset.value),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EntityDisplay(
                        name = monster.type.monsterName,
                        currentHp = monster.currentHp,
                        maxHp = monster.maxHp,
                        attack = monster.attack,
                        mainColor = Color(0xFFFF4D4D),
                        isMonster = true,
                        isBerserking = monster.isBerserking
                    )
                    if (monster.abilities.isNotEmpty()) {
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            monster.abilities.forEach { ability ->
                                TooltipBox(
                                    positionProvider = rememberTooltipPositionProvider(),
                                    tooltip = {
                                        PlainTooltip(
                                            containerColor = Color(0xFF24243E),
                                            contentColor = Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp).widthIn(max = 200.dp)) {
                                                Text(ability.name, fontWeight = FontWeight.Bold, color = Color(0xFFFF4D4D), fontSize = 13.sp)
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.2f))
                                                Text(ability.description, fontSize = 12.sp, lineHeight = 16.sp)
                                            }
                                        }
                                    },
                                    state = rememberTooltipState()
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            ability.name,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF4D4D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                    AnimatedContent(targetState = battleLog) { text ->
                        Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.White)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .graphicsLayer(translationX = heroShakeOffset.value),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hero.shieldHp > 0) {
                        Surface(
                            color = Color(0xFF4DFFFF).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text("🛡️ ${hero.shieldHp}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    EntityDisplay(
                        name = hero.heroClass.className,
                        currentHp = hero.currentHp,
                        maxHp = hero.totalMaxHp,
                        attack = hero.baseWithEquipAttack,
                        passiveBonus = hero.attackPassiveBonus,
                        mainColor = Color(0xFF4DFF88),
                        isMonster = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TooltipBox(
                            positionProvider = rememberTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip(
                                    containerColor = Color(0xFF24243E),
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp).widthIn(max = 200.dp)) {
                                        Text("被動技能", fontWeight = FontWeight.Bold, color = Color(0xFF4DFFFF), fontSize = 13.sp)
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.2f))
                                        Text(hero.heroClass.passiveDescription, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            Surface(
                                modifier = Modifier.graphicsLayer(scaleX = passiveScale, scaleY = passiveScale),
                                color = if (isPassiveTriggered || hero.attackPassiveBonus > 0) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(
                                    1.dp, 
                                    if (isPassiveTriggered || hero.attackPassiveBonus > 0) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "⚡ 被動：${hero.heroClass.passiveName}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPassiveTriggered || hero.attackPassiveBonus > 0) Color(0xFFFFD700) else Color(0xFF4DFFFF)
                                )
                            }
                        }

                        ActiveSkillTextButton(hero, monster, isPlayerTurn && !isBattleOver) {
                            scope.launch {
                                isPlayerTurn = false
                                battleLog = "🌟 施放：${hero.heroClass.activeSkill.name}！"
                                delay(600)
                                
                                val log = hero.heroClass.executeActiveSkill(hero, monster)
                                battleLog = log
                                hero.resetEnergy()

                                if (hero.heroClass is HeroClass.Archer) {
                                    val shotCount = (HeroClass.Archer.MIN_SHOTS..HeroClass.Archer.MAX_SHOTS).random()
                                    repeat(shotCount) {
                                        delay(300)
                                        if (monster.currentHp > 0) {
                                            val damage = hero.heroClass.getSkillDamage(hero, monster)
                                            launch { shake(monsterShakeOffset) }
                                            monster.currentHp = (monster.currentHp - damage).coerceAtLeast(0)
                                            battleLog = "💥 快速射擊！造成 $damage 傷害"
                                        }
                                    }
                                }
                                
                                delay(600)
                                if (monster.currentHp <= 0) {
                                    isBattleOver = true
                                    battleLog = "🏆 勝利！擊敗強敵！"
                                    delay(1000)
                                    onVictory(monster.goldDrop)
                                } else {
                                    isPlayerTurn = true
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBadge(HeroStat.CritRate(hero.totalCritRate))
                        StatBadge(HeroStat.BlockRate(hero.totalBlockRate))
                        StatBadge(HeroStat.Gold(hero.gold))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().weight(2f),
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        EquipmentSlot("武器", hero.weapon, size = 44.dp) { hero.unequipWeapon() }
                        Spacer(modifier = Modifier.width(20.dp))
                        EquipmentSlot("防具", hero.armor, size = 44.dp) { hero.unequipArmor() }
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val consumables = hero.inventory.filterIsInstance<Consumable>()
                        val weapons = hero.inventory.filterIsInstance<Weapon>()
                        val armors = hero.inventory.filterIsInstance<Armor>()

                        InventoryRow("🧪 藥水", consumables, isPlayerTurn && !isBattleOver) {
                            hero.useItem(it)
                            battleLog = "✨ 使用了 ${it.name}"
                        }
                        InventoryRow("⚔️ 武器", weapons, isPlayerTurn && !isBattleOver) {
                            hero.useItem(it)
                            battleLog = "🛡️ 裝備了 ${it.name}"
                        }
                        InventoryRow("🛡️ 防具", armors, isPlayerTurn && !isBattleOver) {
                            hero.useItem(it)
                            battleLog = "🛡️ 裝備了 ${it.name}"
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (isPlayerTurn && !isBattleOver) {
                        scope.launch {
                            isPlayerTurn = false
                            hero.gainEnergy()
                            
                            // 怪物回合開始前的再生效果 (百分比形式)
                            monster.abilities.filterIsInstance<Regen>().forEach { regen ->
                                val healAmount = (monster.maxHp * regen.rate).toInt()
                                monster.heal(healAmount)
                                battleLog = "🌱 ${monster.type.monsterName} 再生了 $healAmount 生命"
                                delay(400)
                            }

                            suspend fun triggerPassiveEffect() {
                                isPassiveTriggered = true
                                delay(300)
                                isPassiveTriggered = false
                            }

                            suspend fun performAttack(isPassive: Boolean = false, damageMultiplier: Double = 1.0, critChanceBonus: Double = 0.0): Boolean {
                                val finalCritRate = (hero.totalCritRate + critChanceBonus).coerceIn(0.0, 1.0)
                                val isCrit = Random.nextDouble() < finalCritRate
                                val hClass = hero.heroClass
                                
                                val critMult = if (isCrit) hClass.getCritMultiplier() else 1.0
                                
                                var damage = (hero.totalAttack * critMult).toInt()
                                
                                if (hero.isWarriorBuffActive) {
                                    val skillMult = hClass.getSkillAtkMultiplier()
                                    damage = (damage * skillMult).toInt()
                                    hero.isWarriorBuffActive = false
                                }

                                damage = (damage * damageMultiplier).toInt()

                                if (hero.isRogueBleedActive) {
                                    val extraDamage = hero.heroClass.getSkillDamage(hero, monster)
                                    if (extraDamage > 0) {
                                        damage += extraDamage
                                    }
                                    hero.isRogueBleedActive = false
                                }
                                
                                launch { shake(monsterShakeOffset) }
                                monster.currentHp = (monster.currentHp - damage).coerceAtLeast(0)
                                battleLog = if (isCrit) "🔥 爆擊! 造成 $damage 傷害" else "💥 擊中！造成 $damage 傷害"
                                
                                // 觸發怪物反傷 (百分比形式)
                                monster.abilities.filterIsInstance<Thorns>().forEach { thorns ->
                                    if (monster.currentHp > 0) {
                                        delay(300)
                                        val reflectedDamage = (damage * thorns.rate).toInt()
                                        hero.takeDamage(reflectedDamage)
                                        battleLog = "🌵 受到反傷！失去 $reflectedDamage 生命"
                                        launch { shake(heroShakeOffset) }
                                    }
                                }

                                val healAmt = hero.onAfterAttack(damage, isCrit)
                                if (healAmt > 0) {
                                    battleLog += " (吸血 +$healAmt)"
                                }

                                delay(400)
                                return monster.currentHp <= 0
                            }

                            if (performAttack()) {
                                isBattleOver = true
                                battleLog = "🏆 勝利！擊敗強敵！"
                                delay(1000)
                                onVictory(monster.goldDrop)
                                return@launch
                            }

                            val hClass = hero.heroClass
                            val doubleChance = hClass.getDoubleAttackChance()
                            val canDouble = (Random.nextDouble() < doubleChance) || hero.isMageNextDoubleActive
                            if (canDouble) {
                                if (!hero.isMageNextDoubleActive) launch { triggerPassiveEffect() }
                                battleLog = "✨ 法術回響！再次施法！"
                                hero.isMageNextDoubleActive = false
                                delay(400)
                                if (performAttack(isPassive = true)) {
                                    isBattleOver = true
                                    battleLog = "🏆 勝利！擊敗強敵！"
                                    delay(1000)
                                    onVictory(monster.goldDrop)
                                    return@launch
                                }
                            }

                            delay(300)

                            val dodgeChance = hClass.getDodgeChance()
                            if (Random.nextDouble() < dodgeChance) {
                                launch { triggerPassiveEffect() }
                                battleLog = "🏹 精準閃避！避開了反擊！"
                                delay(600)
                            } else {
                                val isBlocked = Random.nextDouble() < hero.totalBlockRate
                                val warriorBonusBlock = if (hero.heroClass is HeroClass.Warrior && hero.isWarriorBuffActive) 0.20 else 0.0
                                val finalBlocked = isBlocked || (Random.nextDouble() < warriorBonusBlock)

                                val rawMonsterDamage = monster.attack
                                var monsterDamage = if (finalBlocked) (rawMonsterDamage / 2) else rawMonsterDamage
                                
                                if (monsterDamage > 0) {
                                    launch { shake(heroShakeOffset) }
                                    hero.takeDamage(monsterDamage)
                                    battleLog = if (finalBlocked) "🛡️ BLOCK! 僅受 $monsterDamage 傷害" else "⚠️ 受到 $monsterDamage 傷害"
                                    
                                    // 觸發怪物吸血
                                    monster.abilities.filterIsInstance<LifeSteal>().forEach { lifeSteal ->
                                        val healAmount = (monsterDamage * lifeSteal.rate).toInt()
                                        if (healAmount > 0) {
                                            delay(300)
                                            monster.heal(healAmount)
                                            battleLog = "💉 ${monster.type.monsterName} 吸血恢復了 $healAmount 生命"
                                        }
                                    }
                                }
                                
                                if (hero.currentHp <= 0) {
                                    isBattleOver = true
                                    battleLog = "💀 你倒下了..."
                                    delay(1000)
                                    onDefeat()
                                    return@launch
                                }
                            }
                            isPlayerTurn = true
                        }
                    }
                },
                enabled = isPlayerTurn && !isBattleOver,
                modifier = Modifier.fillMaxWidth(0.8f).height(46.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
            ) {
                Text("發 動 攻 擊", fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSkillTextButton(hero: Hero, monster: Monster, isEnabled: Boolean, onClick: () -> Unit) {
    val skill = hero.heroClass.activeSkill
    val isReady = hero.energy >= hero.maxEnergy
    
    val animatedProgress by animateFloatAsState(
        targetValue = hero.energy.toFloat() / hero.maxEnergy,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val activeColor = when(hero.heroClass) {
        is HeroClass.Warrior -> Color(0xFFE94560)
        is HeroClass.Mage -> Color(0xFF4DFFFF)
        is HeroClass.Rogue -> Color(0xFF4DFF88)
        is HeroClass.Paladin -> Color(0xFFFFD700)
        is HeroClass.Archer -> Color(0xFFA24DFF)
    }

    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                containerColor = Color(0xFF24243E),
                contentColor = Color.White,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp).widthIn(max = 220.dp)) {
                    Text(skill.name, fontWeight = FontWeight.ExtraBold, color = activeColor, fontSize = 16.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.2f))
                    Text(skill.description, fontSize = 13.sp, lineHeight = 18.sp)
                    
                    val skillDamage = hero.heroClass.getSkillDamage(hero, monster)
                    if (skillDamage > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val damageLabel = when (hero.heroClass) {
                            is HeroClass.Archer -> "預估傷害: $skillDamage × ${HeroClass.Archer.MIN_SHOTS}~${HeroClass.Archer.MAX_SHOTS} 次"
                            is HeroClass.Rogue -> "預估額外傷害: +$skillDamage"
                            else -> "預估傷害: $skillDamage"
                        }
                        Text(damageLabel, fontSize = 12.sp, color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(" 能量消耗: ${skill.energyRequired}", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        state = rememberTooltipState()
    ) {
        Surface(
            modifier = Modifier
                .clickable(enabled = isEnabled && isReady) { onClick() },
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(
                1.dp, 
                if (isReady) activeColor else Color.White.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = "🔥 技能：${skill.name}",
                modifier = Modifier
                    .drawBehind {
                        val progressWidth = size.width * animatedProgress
                        drawRect(
                            color = if (isReady) activeColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.2f),
                            size = Size(progressWidth, size.height)
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isReady) activeColor else Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryRow(title: String, items: List<ShopItem>, isEnabled: Boolean, onUse: (ShopItem) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(42.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.width(50.dp))
        if (items.isEmpty()) {
            Text("無", fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    TooltipBox(
                        positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 4.dp),
                        tooltip = { 
                            PlainTooltip(
                                containerColor = Color(0xFF24243E),
                                contentColor = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp).widthIn(max = 180.dp)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, color = item.rarity.color, fontSize = 14.sp)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.15f))
                                    Text(item.description, fontSize = 12.sp, lineHeight = 16.sp)
                                }
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        Surface(
                            modifier = Modifier.width(85.dp).clickable(enabled = isEnabled) { onUse(item) },
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.2.dp, item.rarity.color.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = item.name, 
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp), 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = item.rarity.color, 
                                textAlign = TextAlign.Center, 
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

suspend fun shake(animatable: Animatable<Float, AnimationVector1D>) {
    repeat(4) {
        animatable.animateTo(10f, tween(50, easing = LinearEasing))
        animatable.animateTo(-10f, tween(50, easing = LinearEasing))
    }
    animatable.animateTo(0f, tween(50, easing = LinearEasing))
}
