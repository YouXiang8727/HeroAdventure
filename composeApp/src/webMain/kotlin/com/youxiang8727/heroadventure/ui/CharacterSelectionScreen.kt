package com.youxiang8727.heroadventure.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
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
                            initialGold = 50 
                        )
                        
                        hero.inventory.add(HealthPotion(healAmount = 50, price = 20))
                        hero.inventory.add(HealthPotion(healAmount = 50, price = 20))
                        
                        when(hClass) {
                            is HeroClass.Warrior -> hero.inventory.add(Sword("生鏽長劍", attack = 5, price = 30))
                            is HeroClass.Mage -> hero.inventory.add(Staff("學徒法杖", attack = 8, price = 35))
                            is HeroClass.Rogue -> hero.inventory.add(Sword("新手匕首", attack = 4, price = 25))
                            is HeroClass.Paladin -> hero.inventory.add(LightArmor("舊皮甲", hp = 30, price = 30))
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
                containerColor = Color(0xFF24243E),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .widthIn(max = 280.dp)
                ) {
                    Text(
                        text = heroClass.className,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFFFFD700)
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp), 
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    
                    DetailRow("📈 生命成長", "+${heroClass.hpGrowth}")
                    DetailRow("⚔️ 攻擊成長", "+${heroClass.attackGrowth}")
                    
                    if (heroClass.blockRate > 0) {
                        DetailRow("🛡️ 基礎格擋", "${(heroClass.blockRate * 100).toInt()}%")
                    } else if (heroClass.critRate > 0) {
                        DetailRow("💥 基礎暴擊", "${(heroClass.critRate * 100).toInt()}%")
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("🎁 初始物資", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFA500))
                    Column(modifier = Modifier.padding(start = 6.dp, top = 4.dp)) {
                        Text("• ${heroClass.startingWeapon}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.95f))
                        heroClass.startingArmor?.let {
                            Text("• $it", fontSize = 11.sp, color = Color.White.copy(alpha = 0.95f))
                        }
                        Text("• 生命藥水 x2", fontSize = 11.sp, color = Color.White.copy(alpha = 0.95f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "被動：${heroClass.passiveName}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF4DFFFF)
                            )
                            Text(
                                text = heroClass.passiveDescription,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "主動：${heroClass.activeSkill.name}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFFFF4D4D)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " 消耗能量: ${heroClass.activeSkill.energyRequired}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = heroClass.activeSkill.description,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
            .heightIn(min = 180.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) cardColor else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) cardColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp) 
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
                    fontSize = 28.sp
                )
            }

            Text(
                text = heroClass.className,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (isSelected) cardColor else Color.White
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatRow(Icons.Default.Favorite, "生命", "${heroClass.baseHp}", Color(0xFFFF4D4D))
                StatRow(Icons.Default.Star, "攻擊", "${heroClass.baseAttack}", Color(0xFFFFA500))
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
        Text(text = "$label: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}
