package com.clocken.smash.mixin;

import com.clocken.smash.Smash;
import com.clocken.smash.SmashUtil;
import com.evandev.treeliable.common.config.ModConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private static final ResourceLocation SMASH_ICON = Smash.resource("textures/gui/smash_icon.png");

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void smash$renderSmashIcon(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;

            try {
                if (ModConfig.get().showChoppingIndicator && SmashUtil.blockCanBeSmashed(blockHit.getBlockPos())) {
                    int screenWidth = guiGraphics.guiWidth();
                    int screenHeight = guiGraphics.guiHeight();

                    int x = (screenWidth / 2) + ModConfig.get().choppingIndicatorXOffset;
                    int y = (screenHeight / 2) + ModConfig.get().choppingIndicatorYOffset;

                    RenderSystem.enableBlend();

                    RenderSystem.blendFuncSeparate(
                            GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                            GlStateManager.SourceFactor.ONE,
                            GlStateManager.DestFactor.ZERO
                    );

                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(SMASH_ICON, x, y, 0, 0, 16, 16, 16, 16);

                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableBlend();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
