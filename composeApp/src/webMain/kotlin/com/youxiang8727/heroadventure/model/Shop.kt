package com.youxiang8727.heroadventure.model

import kotlin.random.Random

/**
 * 商店實體，負責處理商店的貨架生成邏輯
 */
object Shop {
    /**
     * 根據當前等級生成隨機商品
     */
    fun generateItems(level: Int): List<ShopItem> {
        val items = mutableListOf<ShopItem>()
        
        // 1. 生成藥水
        // 必定出現一瓶普通藥水
        items.add(HealthPotion.createRandom(level, Rarity.COMMON))
        
        // 隨機出現更高階藥水
        if (Random.nextDouble() < 0.65) {
            val rarity = when (Random.nextDouble()) {
                in 0.0..0.6 -> Rarity.RARE
                in 0.6..0.9 -> Rarity.EPIC
                else -> Rarity.LEGENDARY
            }
            items.add(HealthPotion.createRandom(level, rarity))
        }

        // 2. 隨機生成屬性碎片
        if (Random.nextDouble() < 0.7) {
            items.add(StatShard.createRandom(level))
        }
        
        // 3. 隨機生成裝備
        val equipmentCount = Random.nextInt(2, 5)
        repeat(equipmentCount) {
            items.add(Equipment.createRandom(level))
        }

        return items
    }
}
