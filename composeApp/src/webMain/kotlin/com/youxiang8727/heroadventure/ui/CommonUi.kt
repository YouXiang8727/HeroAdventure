package com.youxiang8727.heroadventure.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youxiang8727.heroadventure.model.HeroStat
import com.youxiang8727.heroadventure.model.ShopItem

@Composable
fun StatBadge(stat: HeroStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = stat.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, stat.color.copy(alpha = 0.4f))
    ) {
        Text(
            text = "${stat.icon} ${stat.label}: ${stat.formattedValue}",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = stat.color
        )
    }
}

/**
 * 共用的實體顯示元件 (血條 + 名字 + 攻擊力 + 被動加成)
 */
@Composable
fun EntityDisplay(
    name: String,
    currentHp: Int,
    maxHp: Int,
    attack: Int,
    mainColor: Color,
    isMonster: Boolean,
    modifier: Modifier = Modifier,
    hpBarWidth: Dp = 200.dp,
    hpBarHeight: Dp = 18.dp,
    fontSize: TextUnit = 14.sp,
    passiveBonus: Int = 0 // 新增：被動加成數值
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                color = if (isMonster) Color(0xFFFF4D4D) else Color(0xFF4DFF88)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = Color.White.copy(alpha = 0.15f), 
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ATK: $attack",
                        fontSize = (fontSize.value * 0.75).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (passiveBonus > 0) {
                        Text(
                            text = " (+$passiveBonus)",
                            fontSize = (fontSize.value * 0.65).sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4DFF88) // 綠色代表被動加成
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(hpBarWidth)
                .height(hpBarHeight)
                .clip(RoundedCornerShape(hpBarHeight / 2))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, mainColor.copy(alpha = 0.5f), RoundedCornerShape(hpBarHeight / 2)),
            contentAlignment = Alignment.Center
        ) {
            val progress by animateFloatAsState(targetValue = if (maxHp > 0) currentHp.toFloat() / maxHp else 0f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(Brush.horizontalGradient(listOf(mainColor.copy(alpha = 0.6f), mainColor)))
            )

            Text(
                text = "$currentHp / $maxHp",
                fontSize = (hpBarHeight.value * 0.65).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = (hpBarHeight.value * 0.65).sp,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }
}

/**
 * 帶有 Tooltip 功能的裝備插槽
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSlot(
    label: String,
    item: ShopItem?,
    size: Dp = 32.dp,
    onUnequip: () -> Unit
) {
    val slotBox = @Composable {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size / 5))
                .background(if (item != null) item.rarity.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                .border(
                    1.dp,
                    if (item != null) item.rarity.color.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(size / 5)
                )
                .clickable(enabled = item != null) { onUnequip() },
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                Text(
                    item.name.take(2),
                    fontSize = (size.value * 0.35).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = item.rarity.color
                )
            } else {
                Text(
                    label,
                    fontSize = (size.value * 0.3).sp,
                    color = Color.White.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (item != null) {
        TooltipBox(
            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 4.dp),
            tooltip = {
                PlainTooltip(
                    containerColor = Color(0xFF24243E),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = item.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = item.rarity.color)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.2f))
                        Text("等級: ${item.level}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        Text("效果: ${item.description}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
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
