package com.youxiang8727.heroadventure.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youxiang8727.heroadventure.model.Equipment
import com.youxiang8727.heroadventure.model.HealthPotion
import com.youxiang8727.heroadventure.model.Hero
import com.youxiang8727.heroadventure.model.HeroClass
import com.youxiang8727.heroadventure.model.LightArmor
import com.youxiang8727.heroadventure.model.Staff
import com.youxiang8727.heroadventure.model.Sword

@Composable
fun CharacterSelectionScreen(onCharacterSelected: (Hero) -> Unit) {
    var selectedClass by remember { mutableStateOf<HeroClass?>(null) }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "選擇你的勇者",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(vertical = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(HeroClass.entries) { heroClass ->
                    CharacterCardWithTooltip(
                        heroClass = heroClass,
                        isSelected = selectedClass == heroClass,
                        onClick = { selectedClass = heroClass }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedClass?.let { hClass ->
                        val hero = Hero(
                            heroClass = hClass,
                            initialHp = hClass.baseHp,
                            initialMaxHp = hClass.baseHp,
                            initialAttack = hClass.baseAttack,
                            initialGold = 50 // 平衡後的起始金幣
                        )
                        
                        hero.inventory.add(HealthPotion(healAmount = 50, price = 20))
                        hero.inventory.add(HealthPotion(healAmount = 50, price = 20))
                        
                        when(hClass) {
                            is HeroClass.Warrior -> hero.inventory.add(
                                Sword(
                                    "生鏽長劍",
                                    attack = 5,
                                    price = 30
                                )
                            )
                            is HeroClass.Mage -> hero.inventory.add(
                                Staff(
                                    "學徒法杖",
                                    attack = 8,
                                    price = 35
                                )
                            )
                            is HeroClass.Rogue -> hero.inventory.add(Sword("新手匕首", attack = 4, price = 25))
                            is HeroClass.Paladin -> hero.inventory.add(
                                LightArmor(
                                    "舊皮甲",
                                    hp = 30,
                                    price = 30
                                )
                            )
                            is HeroClass.Archer -> hero.inventory.add(Sword("獵人短劍", attack = 6, price = 30))
                        }
                        
                        val firstItem = hero.inventory.lastOrNull()
                        if (firstItem is Equipment) {
                            hero.useItem(firstItem)
                        }

                        onCharacterSelected(hero)
                    }
                },
                enabled = selectedClass != null,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560),
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(54.dp)
                    .then(
                        if (selectedClass != null) 
                            Modifier.shadow(8.dp, RoundedCornerShape(24.dp)) 
                        else Modifier
                    )
            ) {
                Text(
                    "踏 上 征 程",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (selectedClass != null) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCardWithTooltip(
    heroClass: HeroClass,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 4.dp),
        tooltip = {
            PlainTooltip(
                containerColor = Color.Black.copy(alpha = 0.95f),
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp).widthIn(max = 260.dp)) {
                    Text(
                        text = heroClass.className,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFFFFD700)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.2f))
                    
                    // 成長數值
                    DetailRow("📈 生命成長", "+${heroClass.hpGrowth}")
                    DetailRow("⚔️ 攻擊成長", "+${heroClass.attackGrowth}")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 初始物資區
                    Text("🎁 初始物資", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA500))
                    Column(modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                        Text("• ${heroClass.startingWeapon}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        heroClass.startingArmor?.let {
                            Text("• $it", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        Text("• 生命藥水 x2", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // 被動技能
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "被動：${heroClass.passiveName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF4DFFFF)
                            )
                            Text(
                                text = heroClass.passiveDescription,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 主動技能
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "主動：${heroClass.activeSkill.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF4D4D)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(消耗: ${heroClass.activeSkill.energyRequired}⚡)",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = heroClass.activeSkill.description,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        },
        state = rememberTooltipState(isPersistent = false)
    ) {
        CharacterCard(heroClass, isSelected, onClick)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun CharacterCard(
    heroClass: HeroClass,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardColor = when(heroClass) {
        is HeroClass.Warrior -> Color(0xFFE94560)
        is HeroClass.Mage -> Color(0xFF4DFFFF)
        is HeroClass.Rogue -> Color(0xFF4DFF88)
        is HeroClass.Paladin -> Color(0xFFFFD700)
        is HeroClass.Archer -> Color(0xFFA24DFF)
    }

    val emoji = when(heroClass) {
        is HeroClass.Warrior -> "⚔️"
        is HeroClass.Mage -> "🔮"
        is HeroClass.Rogue -> "🗡️"
        is HeroClass.Paladin -> "🛡️"
        is HeroClass.Archer -> "🏹"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // 縮小高度
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) cardColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) cardColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 置中顯示
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(cardColor.copy(alpha = 0.3f), Color.Transparent)
                        ), 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = heroClass.className,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (isSelected) cardColor else Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatRow(Icons.Default.Favorite, "生命", "${heroClass.baseHp}", Color(0xFFFF4D4D))
                StatRow(Icons.Default.Star, "攻擊", "${heroClass.baseAttack}", Color(0xFFFFA500))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val subStatIcon = if (heroClass.blockRate > 0) "🛡️" else "💥"
                    val subStatValue = if (heroClass.blockRate > 0) 
                        "${(heroClass.blockRate * 100).toInt()}%" else 
                        "${(heroClass.critRate * 100).toInt()}%"
                    
                    Text(text = subStatIcon, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (heroClass.blockRate > 0) "防禦: $subStatValue" else "暴擊: $subStatValue",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$label: ", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
