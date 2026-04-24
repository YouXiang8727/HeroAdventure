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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
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
import com.youxiang8727.heroadventure.model.HeroClass.Rogue.CRIT_LIFE_STEAL
import com.youxiang8727.heroadventure.model.HeroStat
import com.youxiang8727.heroadventure.model.Monster
import com.youxiang8727.heroadventure.model.ShopItem
import com.youxiang8727.heroadventure.model.Weapon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BattleScreen(
    hero: Hero,
    monster: Monster,
    stageLevel: Int,
    onVictory: (Int) -> Unit,
    onDefeat: () -> Unit
) {
    var heroHp by remember { mutableStateOf(hero.currentHp) }
    var monsterHp by remember { mutableStateOf(monster.currentHp) }
    var battleLog by remember { mutableStateOf("遭遇 ${monster.type.monsterName}！") }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var isBattleOver by remember { mutableStateOf(false) }

    var shieldHp by remember { mutableStateOf(0) }
    var isWarriorBuffActive by remember { mutableStateOf(false) }
    var isMageNextDoubleActive by remember { mutableStateOf(false) }
    var isRogueCritBuffActive by remember { mutableStateOf(false) }

    var isPassiveTriggered by remember { mutableStateOf(false) }
    val passiveScale by animateFloatAsState(
        targetValue = if (isPassiveTriggered) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val monsterShakeOffset = remember { Animatable(0f) }
    val heroShakeOffset = remember { Animatable(0f) }
    
    val scope = rememberCoroutineScope()

    val currentAttack = hero.totalAttack
    val currentCrit = hero.totalCritRate
    val currentBlock = hero.totalBlockRate
    val currentMaxHp = hero.totalMaxHp
    val passiveBonus = hero.attackPassiveBonus

    LaunchedEffect(hero.currentHp) {
        heroHp = hero.currentHp
    }

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
                EntityDisplay(
                    name = monster.type.monsterName,
                    currentHp = monsterHp,
                    maxHp = monster.maxHp,
                    attack = monster.attack,
                    mainColor = Color(0xFFFF4D4D),
                    isMonster = true
                )
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
                    Box(contentAlignment = Alignment.Center) {
                        EntityDisplay(
                            name = hero.heroClass.className,
                            currentHp = heroHp,
                            maxHp = currentMaxHp,
                            attack = hero.baseWithEquipAttack,
                            passiveBonus = passiveBonus,
                            mainColor = Color(0xFF4DFF88),
                            isMonster = false
                        )
                        if (shieldHp > 0) {
                            Surface(
                                modifier = Modifier.offset(y = 20.dp),
                                color = Color(0xFF4DFFFF).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("🛡️ $shieldHp", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                                color = if (isPassiveTriggered || passiveBonus > 0) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(
                                    1.dp, 
                                    if (isPassiveTriggered || passiveBonus > 0) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "⚡ 被動：${hero.heroClass.passiveName}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPassiveTriggered || passiveBonus > 0) Color(0xFFFFD700) else Color(0xFF4DFFFF)
                                )
                            }
                        }

                        ActiveSkillTextButton(hero, isPlayerTurn && !isBattleOver) {
                            scope.launch {
                                isPlayerTurn = false
                                val skill = hero.heroClass.activeSkill
                                battleLog = "🌟 施放：${skill.name}！"
                                hero.resetEnergy()
                                delay(600)

                                when(val hClass = hero.heroClass) {
                                    is HeroClass.Warrior -> {
                                        val sacrifice = (heroHp * 0.15).toInt()
                                        heroHp = (heroHp - sacrifice).coerceAtLeast(1)
                                        isWarriorBuffActive = true
                                        // 戰士主動技現在提供 10% 額外格擋邏輯
                                        battleLog = "🩸 獻祭生命，力量與守護湧現！"
                                    }
                                    is HeroClass.Mage -> {
                                        isMageNextDoubleActive = true
                                        battleLog = "🌀 魔力超載，下回合必發動連擊！"
                                    }
                                    is HeroClass.Rogue -> {
                                        isRogueCritBuffActive = true
                                        battleLog = "🗡️ 鎖定死角，致命一擊！"
                                    }
                                    is HeroClass.Paladin -> {
                                        val damage = (currentAttack + currentMaxHp * currentBlock).toInt()
                                        launch { shake(monsterShakeOffset) }
                                        monsterHp = (monsterHp - damage).coerceAtLeast(0)
                                        val healAmount = (currentMaxHp * hClass.HEAL_PERCENT_ON_VICTORY).toInt()
                                        heroHp = (heroHp + healAmount).coerceAtMost(currentMaxHp)
                                        battleLog = "✨ 聖光制裁！造成 $damage 傷害並觸發聖光治癒恢復 $healAmount 生命"
                                    }
                                    is HeroClass.Archer -> {
                                        battleLog = "🏹 疾風連射！"
                                        repeat(4) { // 弓箭手主動技現在是 4 連射
                                            delay(300)
                                            if (monsterHp > 0) {
                                                val damage = (currentAttack * 0.6).toInt()
                                                launch { shake(monsterShakeOffset) }
                                                monsterHp = (monsterHp - damage).coerceAtLeast(0)
                                                battleLog = "💥 快速射擊！造成 $damage 傷害"
                                            }
                                        }
                                    }
                                }
                                
                                delay(600)
                                if (monsterHp <= 0) {
                                    isBattleOver = true
                                    battleLog = "🏆 勝利！擊敗強敵！"
                                    delay(1000)
                                    hero.currentHp = heroHp
                                    onVictory(monster.goldDrop)
                                } else {
                                    isPlayerTurn = true
                                    hero.currentHp = heroHp
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBadge(HeroStat.CritRate(currentCrit))
                        StatBadge(HeroStat.BlockRate(currentBlock))
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
                            heroHp = hero.currentHp
                            battleLog = "✨ 使用了 ${it.name}"
                        }
                        InventoryRow("⚔️ 武器", weapons, isPlayerTurn && !isBattleOver) {
                            hero.useItem(it)
                            battleLog = "🛡️ 裝備了 ${it.name}"
                        }
                        InventoryRow("🛡️ 防具", armors, isPlayerTurn && !isBattleOver) {
                            hero.useItem(it)
                            heroHp = hero.currentHp
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

                            suspend fun triggerPassiveEffect() {
                                isPassiveTriggered = true
                                delay(300)
                                isPassiveTriggered = false
                            }

                            suspend fun performAttack(isPassive: Boolean = false, damageMultiplier: Double = 1.0, critChanceBonus: Double = 0.0): Boolean {
                                val finalCritRate = (currentCrit + critChanceBonus).coerceIn(0.0, 1.0)
                                val isCrit = Random.nextDouble() < finalCritRate
                                val hClass = hero.heroClass
                                val critMult = if (hClass is HeroClass.Rogue) {
                                    if (isCrit) launch { triggerPassiveEffect() }
                                    hClass.CRIT_MULTIPLIER 
                                } else 2.0
                                
                                var damage = if (isCrit) (currentAttack * critMult).toInt() else currentAttack
                                damage = (damage * damageMultiplier).toInt()

                                if (isWarriorBuffActive) {
                                    damage = (damage * 1.5).toInt()
                                    isWarriorBuffActive = false
                                }
                                
                                launch { shake(monsterShakeOffset) }
                                monsterHp = (monsterHp - damage).coerceAtLeast(0)
                                battleLog = if (isCrit) "🔥 CRITICAL HIT! 造成 $damage 傷害" else "💥 擊中！造成 $damage 傷害"
                                
                                if (isRogueCritBuffActive && isCrit) {
                                    val healAmt = (damage * CRIT_LIFE_STEAL).toInt()
                                    heroHp = (heroHp + healAmt).coerceAtMost(currentMaxHp)
                                    battleLog += " (吸血 +$healAmt)"
                                }

                                delay(400)
                                return monsterHp <= 0
                            }

                            val critBonus = if (isRogueCritBuffActive) 0.5 else 0.0
                            if (performAttack(critChanceBonus = critBonus)) {
                                isBattleOver = true
                                battleLog = "🏆 勝利！擊敗強敵！"
                                delay(1000)
                                hero.currentHp = heroHp
                                onVictory(monster.goldDrop)
                                return@launch
                            }
                            isRogueCritBuffActive = false

                            val hClass = hero.heroClass
                            val canDouble = (hClass is HeroClass.Mage && Random.nextDouble() < hClass.DOUBLE_ATTACK_CHANCE) || isMageNextDoubleActive
                            if (canDouble) {
                                if (!isMageNextDoubleActive) launch { triggerPassiveEffect() }
                                battleLog = "✨ 法術回響！再次施法！"
                                isMageNextDoubleActive = false
                                delay(400)
                                if (performAttack(isPassive = true)) {
                                    isBattleOver = true
                                    battleLog = "🏆 勝利！擊敗強敵！"
                                    delay(1000)
                                    hero.currentHp = heroHp
                                    onVictory(monster.goldDrop)
                                    return@launch
                                }
                            }

                            delay(300)

                            if (hClass is HeroClass.Archer && Random.nextDouble() < hClass.DODGE_CHANCE) {
                                launch { triggerPassiveEffect() }
                                battleLog = "🏹 精準閃避！避開了反擊！"
                                delay(600)
                            } else {
                                // 戰士主動技額外格擋邏輯
                                val warriorBonusBlock = if (hero.heroClass is HeroClass.Warrior && isWarriorBuffActive) 0.1 else 0.0
                                val isBlocked = Random.nextDouble() < (currentBlock + warriorBonusBlock)

                                val rawMonsterDamage = monster.attack
                                var monsterDamage = if (isBlocked) (rawMonsterDamage / 2) else rawMonsterDamage
                                
                                if (shieldHp > 0) {
                                    val absorbed = monsterDamage.coerceAtMost(shieldHp)
                                    shieldHp -= absorbed
                                    monsterDamage -= absorbed
                                    battleLog = "🛡️ 護盾吸收了傷害！"
                                    delay(300)
                                }

                                if (monsterDamage > 0) {
                                    launch { shake(heroShakeOffset) }
                                    heroHp = (heroHp - monsterDamage).coerceAtLeast(0)
                                    battleLog = if (isBlocked) "🛡️ BLOCK! 僅受 $monsterDamage 傷害" else "⚠️ 受到 $monsterDamage 傷害"
                                }
                                
                                if (heroHp <= 0) {
                                    isBattleOver = true
                                    battleLog = "💀 你倒下了..."
                                    delay(1000)
                                    onDefeat()
                                    return@launch
                                }
                            }
                            isPlayerTurn = true
                            hero.currentHp = heroHp
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
fun ActiveSkillTextButton(hero: Hero, isEnabled: Boolean, onClick: () -> Unit) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⚡ 能量消耗: ${skill.energyRequired}", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
