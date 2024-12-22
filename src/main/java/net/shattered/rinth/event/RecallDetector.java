package net.shattered.rinth.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shattered.rinth.Netherinth;

public class RecallDetector {
    public static void register() {
        Netherinth.LOGGER.info("Registering RecallDetector");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            Netherinth.LOGGER.info("==== UseItemCallback Debug ====");
            Netherinth.LOGGER.info("Player: " + player.getName().getString());
            Netherinth.LOGGER.info("Hand: " + hand.toString());
            Netherinth.LOGGER.info("World isClient: " + world.isClient);

            ItemStack heldItem = player.getStackInHand(hand);
            Netherinth.LOGGER.info("Held item: " + heldItem);
            Netherinth.LOGGER.info("Is hand empty: " + heldItem.isEmpty());
            Netherinth.LOGGER.info("Is using item: " + player.isUsingItem());
            Netherinth.LOGGER.info("Active hand: " + player.getActiveHand());
            Netherinth.LOGGER.info("==============================");

            if (heldItem.isEmpty()) {
                Netherinth.LOGGER.info("Hand is empty, should trigger recall!");
            }

            return TypedActionResult.pass(heldItem);
        });
    }
}