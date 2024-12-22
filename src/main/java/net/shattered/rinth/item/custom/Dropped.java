package net.shattered.rinth.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface Dropped {
    boolean onStopUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks);
}
