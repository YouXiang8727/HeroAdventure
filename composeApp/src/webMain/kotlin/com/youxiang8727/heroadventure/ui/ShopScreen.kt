package com.youxiang8727.heroadventure.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youxiang8727.heroadventure.model.Equipment
import com.youxiang8727.heroadventure.model.Hero
import com.youxiang8727.heroadventure.model.HeroStat
import com.youxiang8727.heroadventure.model.ShopItem
import com.youxiang8727.heroadventure.model.StatShard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    hero: Hero,
    shopItems: List<ShopItem>,
    onContinue: () -> Unit
) {
    var message by remember { mutableStateOf("歡迎來到神秘商店") }
    var currentGold by remember { mutableStateOf(hero.gold) }
    val boughtItems = remember { mutableStateMapOf<ShopItem, Boolean>() }

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 標題與金幣
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MYSTIC SHOP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
                
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💰", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "STAGE ${hero.level}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }

            // 1. 英雄狀態面板
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EntityDisplay(
                        name = hero.heroClass.className,
                        currentHp = hero.currentHp,
                        maxHp = hero.totalMaxHp,
                        attack = hero.totalAttack,
                        mainColor = Color(0xFF4DFF88),
                        isMonster = false,
                        hpBarWidth = 160.dp,
                        hpBarHeight = 16.dp,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    StatBadge(HeroStat.CritRate(hero.totalCritRate))
                    StatBadge(HeroStat.BlockRate(hero.totalBlockRate))
                    StatBadge(HeroStat.Gold(hero.gold))
                    Spacer(modifier = Modifier.weight(1f))
                    // 右側裝備
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MiniEquipSlot("武", hero.weapon) { hero.unequipWeapon() }
                        MiniEquipSlot("防", hero.armor) { hero.unequipArmor() }
                    }
                }
            }

            // 訊息列
            Text(
                text = message,
                fontSize = 11.sp,
                color = Color(0xFF4DFFFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth()
            )

            // 主要內容區
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
            ) {
                SectionTitle("🛒 商店貨架 (限購碎片)")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    items(shopItems) { item ->
                        val isBought = boughtItems[item] ?: false
                        ShopItemCard(
                            item = item,
                            isAvailable = !isBought && currentGold >= item.price,
                            buttonText = if (isBought) "已售罄" else "G: ${item.price}",
                            onAction = {
                                if (currentGold >= item.price) {
                                    hero.gold -= item.price
                                    currentGold = hero.gold
                                    if (item is StatShard) {
                                        hero.useItem(item)
                                        boughtItems[item] = true
                                        message = "永久提升屬性！"
                                    } else {
                                        hero.inventory.add(item)
                                        message = "獲得了 ${item.name}"
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                SectionTitle("🎒 背包")
                if (hero.inventory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                        Text("背包暫無物品", color = Color.White.copy(alpha = 0.2f), fontSize = 11.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                        items(hero.inventory) { item ->
                            val sellPrice = (item.price * 0.8).toInt()
                            Column {
                                ShopItemCard(
                                    item = item,
                                    isAvailable = true,
                                    buttonText = if (item is Equipment) "穿上" else "使用",
                                    onAction = {
                                        hero.useItem(item)
                                        message = "使用了 ${item.name}"
                                    }
                                )
                                Button(
                                    onClick = {
                                        hero.gold += sellPrice
                                        currentGold = hero.gold
                                        hero.inventory.remove(item)
                                        message = "售出獲得 $sellPrice G"
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(22.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("出售: $sellPrice G", fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(0.5f).height(40.dp).shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
            ) {
                Text("繼續冒險", fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniEquipSlot(label: String, item: ShopItem?, onUnequip: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val slotBox = @Composable {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (item != null) item.rarity.color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                    .border(
                        1.dp,
                        if (item != null) item.rarity.color.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = item != null) { onUnequip() },
                contentAlignment = Alignment.Center
            ) {
                if (item != null) Text(item.name.take(2), fontSize = 9.sp, fontWeight = FontWeight.Black, color = item.rarity.color)
                else Text(label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.1f))
            }
        }

        if (item != null) {
            TooltipBox(
                positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 4.dp),
                tooltip = {
                    PlainTooltip(
                        containerColor = Color.Black.copy(alpha = 0.9f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(text = item.name, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = item.rarity.color)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.2f))
                            Text("等級: ${item.level}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("效果: ${item.description}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                },
                state = rememberTooltipState(isPersistent = false)
            ) {
                slotBox()
            }
        } else {
            slotBox()
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isAvailable: Boolean,
    buttonText: String,
    onAction: () -> Unit
) {
    val isSoldOut = buttonText == "已售罄"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAvailable && !isSoldOut) item.rarity.color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.rarity.label,
                color = item.rarity.color,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = item.name, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White, textAlign = TextAlign.Center, maxLines = 1)
            Text(item.description, fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAction,
                enabled = isAvailable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSoldOut) Color.Gray else item.rarity.color.copy(alpha = 0.7f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.height(24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(buttonText, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
