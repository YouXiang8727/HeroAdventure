# Hero Adventure 開發規範與架構說明 (Coding Style & Architecture)

本文件定義專案的編碼風格、架構設計模式以及開發者準則。

## 1. 架構設計 (Architecture)

專案採用簡單的 **Model-View-Intent (MVI)** 概念變體，專為 Compose Multiplatform 設計：

### 核心組件：
*   **Model (`com.youxiang8727.heroadventure.model`)**：
    *   包含所有領域邏輯 (Domain Logic)。
    *   `GameState`：密封類別 (Sealed Class)，定義遊戲的不同螢幕狀態（選角、戰鬥、商店、結束）。
    *   `Hero` 與 `Monster`：核心實體，內部使用 Compose `mutableStateOf` 以實現自動 UI 更新。
*   **UI (`com.youxiang8727.heroadventure.ui`)**：
    *   純 Compose 函數。
    *   遵循「狀態下行，事件上行」原則。
*   **App.kt (Controller/Router)**：
    *   作為遊戲的主狀態機 (Main State Machine)。
    *   處理狀態切換與全域邏輯（如：生成下一個關卡的怪物）。

## 2. 編碼風格 (Coding Style)

### Kotlin 慣用法：
*   **Sealed Classes**：優先使用密封類別來處理狀態（如 `GameState`）與類別定義（如 `HeroStat`, `HeroClass`）。
*   **Extension Functions**：針對簡單的數值轉換或工具邏輯，使用擴充函數保持程式碼整潔。
*   **Immutable Properties**：優先使用 `val`。僅在 Compose 需要雙向綁定或屬性會隨時間變化時使用 `var by mutableStateOf`。

### 命名規範：
*   **類別/介面**：大駝峰 (PascalCase)，如 `BattleScreen`。
*   **變數/函數**：小駝峰 (camelCase)，如 `calculateDamage`。
*   **常量**：全大寫蛇形命名 (SCREAMING_SNAKE_CASE)，如 `HP_ATK_BONUS_RATE`。

## 3. UI 開發規範 (Compose Guidelines)

*   **狀態提升 (State Hoisting)**：將狀態儘可能提升到頂層，讓子元件保持 Stateless 以提高可測試性。
*   **硬編碼數值**：UI 間距與顏色應盡量抽象化（目前專案正逐步將硬編碼數值移至資源檔案）。
*   **動畫**：戰鬥效果應使用 `Animatable` 或 `animate*AsState` 以確保流暢性。

## 4. 遊戲平衡調整準則 (Balancing)

*   **數值解耦**：所有技能數值（倍率、機率）應定義在 `HeroClass` 的子類別中作為 `const val`，禁止在 UI 層直接寫死計算公式。
*   **文檔同步**：修改程式碼中的技能或怪物數值時，必須同步更新 `docs/` 資料夾下對應的設計手冊。
