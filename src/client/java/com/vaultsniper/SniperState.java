package com.vaultsniper;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;

public class SniperState {

    public static boolean enabled = false;
    public static Item targetItem = null;
    public static boolean useOminousVault = false;

    /** Countdown in ticks before allowing another auto-click. */
    public static int clickCooldown = 0;

    /** True for exactly one tick when the target item is detected, used for HUD flash. */
    public static boolean justDetected = false;
    public static int detectedFlashTicks = 0;

    public static final List<Item> NORMAL_VAULT_ITEMS = List.of(
            Items.WIND_CHARGE,
            Items.ARROW,
            Items.EMERALD,
            Items.GOLD_INGOT,
            Items.IRON_SWORD,
            Items.IRON_AXE,
            Items.IRON_CHESTPLATE,
            Items.SHIELD,
            Items.CROSSBOW,
            Items.SADDLE,
            Items.DIAMOND,
            Items.EXPERIENCE_BOTTLE,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_AXE,
            Items.DIAMOND_CHESTPLATE,
            Items.TRIDENT,
            Items.MACE,
            Items.OMINOUS_BOTTLE
    );

    public static final List<Item> OMINOUS_VAULT_ITEMS = List.of(
            Items.WIND_CHARGE,
            Items.ARROW,
            Items.DIAMOND,
            Items.EMERALD,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_AXE,
            Items.DIAMOND_CHESTPLATE,
            Items.IRON_SWORD,
            Items.IRON_CHESTPLATE,
            Items.CROSSBOW,
            Items.TRIDENT,
            Items.MACE,
            Items.HEAVY_CORE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.OMINOUS_BOTTLE,
            Items.EMERALD_BLOCK,
            Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE
    );
}