package net.shattered.rinth.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.shattered.rinth.item.custom.CustomTridentItem;
import net.shattered.rinth.entity.CustomTridentEntity;
import net.shattered.rinth.Netherinth;
import net.shattered.rinth.event.TridentRecallManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (!player.getWorld().isClient && stack.getItem() instanceof CustomTridentItem) {
            CustomTridentEntity tridentEntity = CustomTridentEntity.createFromDrop(player.getWorld(), player, stack.copy());
            player.getWorld().spawnEntity(tridentEntity);
            player.getWorld().playSoundFromEntity(
                    null,
                    tridentEntity,
                    (SoundEvent) SoundEvents.ITEM_TRIDENT_THROW,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );
            stack.setCount(0);
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (player.getMainHandStack().isEmpty() && player.handSwinging) {
            Netherinth.LOGGER.info("Player is using empty hand");
            TridentRecallManager.handleRecallRequest(player);
        }
    }
}