# SwordFix (Minecraft 1.12.2)

**Developer:** Bigens  
**Platform:** Minecraft 1.12.2 (Forge & CleanroomMC)  
**Supported Java:** Java 8 – 21  

---

## 📌 Overview

**SwordFix** is a lightweight, client-side coremod for Minecraft 1.12.2 that solves an annoying vanilla 1.9+ visual issue: **weapons and tools dipping down off-screen during attack cooldown or upon hitting targets**.

In vanilla 1.9–1.12.2, whenever an entity attacks:
1. Attack strength drops to 0, which by default causes the first-person hand renderer (`ItemRenderer`) to lower the weapon towards the bottom of the screen until cooldown finishes.
2. Item durability decreases by 1, which triggers a re-equip twitch.

**SwordFix** eliminates both of these visual dips while leaving combat balance, damage calculations, and attack speed 100% vanilla.

---

## ✨ Features

- **No Weapon Dipping:** Swords, axes, pickaxes, hoes, shovels, and modded weapons stay in front of you after attacking.
- **Vanilla Combat Preserved:** Damage scaling, attack cooldown timing, crosshair indicators, and sweep attacks remain completely unaffected.
- **Smooth Slot Switching:** Normal equip animations still play when switching hotbar slots or selecting a different item.
- **Universal Compatibility:** Works on both standard Minecraft Forge 1.12.2 and CleanroomMC (Java 8, 17, and 21).
- **Pure Client-Side:** Does not require server installation; safe to use on any multiplayer server.

---

## ⚙️ How It Works Technically

The mod operates as an ASM transformer that targets `net.minecraft.client.renderer.ItemRenderer`:
1. In `updateEquippedItem()` (`func_78441_a`), calls to `getCooledAttackStrength()` are redirected to return `1.0F` specifically for hand position calculations. The weapon model remains raised at all times.
2. Calls to `ForgeHooksClient.shouldCauseReequipAnimation()` are intercepted so that durability loss on hit does not trigger an equip progress reset when holding the same item in the same slot.

---

## 🚀 Installation

1. Download `SwordFix-1.12.2-1.0.0.jar`.
2. Place the jar into your `.minecraft/mods` folder.
3. Launch Minecraft 1.12.2 using Forge or CleanroomMC.

---

## 🔨 Building from Source

Run `build.bat` on Windows (requires JDK 8 or newer).
