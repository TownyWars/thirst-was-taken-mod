package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public class MixinBottleItem {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean shouldModify;
    private int purity;

    @Inject(method = "turnBottleIntoItem", at = @At("HEAD"))
    public void setPurity(ItemStack source, Player player, ItemStack result, CallbackInfoReturnable<ItemStack> cir) {
        Level level = player.getLevel();
        BlockPos fluidPos = MathHelper.getPlayerPOVHitResult(level, player)
                .getBlockPos();
        Block block = level.getBlockState(fluidPos).getBlock();

        LOGGER.info("turnBottleIntoItem.setPurity " + block.getRegistryName().toString());

        shouldModify = (level.getFluidState(fluidPos).is(FluidTags.WATER) && level.getFluidState(fluidPos).isSource())
                || block.getRegistryName().toString().equals("puddles:puddle");
        if (shouldModify)
            purity = WaterPurity.getBlockPurity(level, fluidPos);
    }

    @ModifyArg(method = "turnBottleIntoItem", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack addPurity(ItemStack result) {
        if (shouldModify)
            WaterPurity.addPurity(result, purity);

        return result;
    }
}
