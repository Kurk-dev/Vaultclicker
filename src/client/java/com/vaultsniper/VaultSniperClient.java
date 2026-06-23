package com.vaultsniper;

import com.vaultsniper.mixin.VaultBlockEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.entity.vault.VaultSharedData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class VaultSniperClient implements ClientModInitializer {

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of(VaultSniperMod.MOD_ID, "main"));

    private static final KeyBinding OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.vaultsniper.open_menu", GLFW.GLFW_KEY_K, CATEGORY)
    );

    /** Scan radius in blocks around the player. */
    private static final int SCAN_RADIUS = 6;

    /** Interaction reach in blocks — auto-click only within this distance. */
    private static final double MAX_REACH = 5.0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        HudElementRegistry.addLast(
                Identifier.of(VaultSniperMod.MOD_ID, "status_overlay"),
                (context, tickCounter) -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player == null) return;
                    if (SniperState.targetItem == null && !SniperState.enabled) return;

                    int x = 4;
                    int y = mc.getWindow().getScaledHeight() - 32;

                    // Background
                    int bgColor = SniperState.enabled ? 0xCC0A2A0A : 0xCC1A1A2E;
                    if (SniperState.detectedFlashTicks > 0) bgColor = 0xCCAA8800;
                    context.fill(x, y, x + 160, y + 22, bgColor);
                    context.drawBorder(x, y, 160, 22, SniperState.enabled ? 0xFF55FF55 : 0xFF555577);

                    // Status dot
                    String dot = SniperState.enabled ? "§a●" : "§7●";
                    context.drawTextWithShadow(mc.textRenderer, dot, x + 4, y + 7, 0xFFFFFF);

                    // Target item icon + name
                    if (SniperState.targetItem != null) {
                        context.drawItem(new ItemStack(SniperState.targetItem), x + 14, y + 3);
                        String name = SniperState.targetItem.getName().getString();
                        String vaultTag = SniperState.useOminousVault ? "§c[☠]" : "§b[⚔]";
                        context.drawTextWithShadow(mc.textRenderer,
                                vaultTag + " §f" + name, x + 34, y + 7, 0xFFFFFF);
                    } else {
                        context.drawTextWithShadow(mc.textRenderer,
                                "§7No target — press §eK §7to configure", x + 8, y + 7, 0xFFFFFF);
                    }

                    if (SniperState.detectedFlashTicks > 0) {
                        context.drawCenteredTextWithShadow(mc.textRenderer,
                                "§e✦ TARGET DETECTED ✦",
                                mc.getWindow().getScaledWidth() / 2,
                                mc.getWindow().getScaledHeight() / 2 - 60,
                                0xFFFF55);
                    }
                }
        );
    }

    private void onClientTick(MinecraftClient mc) {
        // Open menu key
        while (OPEN_MENU_KEY.wasPressed()) {
            if (mc.currentScreen == null) {
                mc.setScreen(new SniperScreen());
            }
        }

        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Count down flash
        if (SniperState.detectedFlashTicks > 0) {
            SniperState.detectedFlashTicks--;
        }

        // Count down click cooldown
        if (SniperState.clickCooldown > 0) {
            SniperState.clickCooldown--;
            return;
        }

        if (!SniperState.enabled || SniperState.targetItem == null) return;

        // Verify player is holding the appropriate trial key
        ItemStack mainHand = mc.player.getMainHandStack();
        boolean holdingNormal = mainHand.isOf(Items.TRIAL_KEY);
        boolean holdingOminous = mainHand.isOf(Items.OMINOUS_TRIAL_KEY);

        if (!holdingNormal && !holdingOminous) return;
        if (SniperState.useOminousVault && !holdingOminous) return;
        if (!SniperState.useOminousVault && !holdingNormal) return;

        BlockPos playerPos = mc.player.getBlockPos();

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    BlockPos vaultPos = playerPos.add(dx, dy, dz);

                    // Check block type matches selected vault type
                    var block = mc.world.getBlockState(vaultPos).getBlock();
                    boolean isNormalVault = block == Blocks.VAULT;
                    boolean isOminousVault = block == Blocks.OMINOUS_VAULT;
                    if (!isNormalVault && !isOminousVault) continue;
                    if (SniperState.useOminousVault != isOminousVault) continue;

                    // Distance check
                    double dist = mc.player.squaredDistanceTo(
                            vaultPos.getX() + 0.5, vaultPos.getY() + 0.5, vaultPos.getZ() + 0.5);
                    if (dist > MAX_REACH * MAX_REACH) continue;

                    BlockEntity be = mc.world.getBlockEntity(vaultPos);
                    if (!(be instanceof VaultBlockEntity vault)) continue;

                    VaultSharedData sharedData = ((VaultBlockEntityAccessor) vault).vaultsniper$getSharedData();
                    ItemStack displayed = sharedData.getDisplayedItem();

                    if (displayed.isEmpty()) continue;
                    if (!displayed.isOf(SniperState.targetItem)) continue;

                    // Target detected — auto-click
                    SniperState.detectedFlashTicks = 15;

                    // Determine best face to hit: face toward player
                    Direction face = Direction.getFacing(
                            mc.player.getX() - (vaultPos.getX() + 0.5),
                            mc.player.getY() - (vaultPos.getY() + 0.5),
                            mc.player.getZ() - (vaultPos.getZ() + 0.5)
                    );

                    Vec3d hitVec = Vec3d.ofCenter(vaultPos).offset(face, 0.5);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, face, vaultPos, false);

                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

                    // 40-tick cooldown between clicks (2 seconds)
                    SniperState.clickCooldown = 40;
                    return;
                }
            }
        }
    }
}