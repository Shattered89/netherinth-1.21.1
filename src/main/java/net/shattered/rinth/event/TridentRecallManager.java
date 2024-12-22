package net.shattered.rinth.event;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shattered.rinth.Netherinth;
import net.shattered.rinth.entity.CustomTridentEntity;
import net.shattered.rinth.storage.TridentStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TridentRecallManager {
    private static final Map<UUID, CustomTridentEntity> activePulls = new HashMap<>();
    public static final Identifier RECALL_PACKET = Identifier.of(Netherinth.MOD_ID, "recall_trident");

    public static void register() {
        Netherinth.LOGGER.info("Registering TridentRecallManager");
    }

    public static void handleRecallRequest(PlayerEntity player) {
        UUID playerUUID = player.getUuid();

        if (!activePulls.containsKey(playerUUID) && TridentStorage.hasTrident(player)) {
            Netherinth.LOGGER.info("Attempting recall for player: " + player.getName().getString());
            CustomTridentEntity trident = TridentStorage.getTrident(player);
            if (trident != null) {
                activePulls.put(playerUUID, trident);
                trident.setNoClip(true);
                trident.playSound(net.minecraft.sound.SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
            }
        }
    }

    public static void tick(World world) {
        for (Map.Entry<UUID, CustomTridentEntity> entry : new HashMap<>(activePulls).entrySet()) {
            PlayerEntity player = world.getPlayerByUuid(entry.getKey());
            CustomTridentEntity trident = entry.getValue();

            if (player == null || !trident.isAlive()) {
                if (trident != null && trident.isAlive()) {
                    trident.setNoClip(false);
                }
                activePulls.remove(entry.getKey());
                continue;
            }

            handlePulling(player, trident);
        }
    }

    private static void handlePulling(PlayerEntity player, CustomTridentEntity trident) {
        net.minecraft.util.math.Vec3d vec3d = player.getEyePos().subtract(trident.getPos());
        trident.setPos(
                trident.getX(),
                trident.getY() + vec3d.y * 0.015 * 10.0,
                trident.getZ()
        );

        double pullStrength = 3.0;
        trident.setVelocity(
                trident.getVelocity().multiply(0.8).add(
                        vec3d.normalize().multiply(pullStrength)
                )
        );

        double distance = trident.distanceTo(player);
        if (distance < 2.0) {
            if (!trident.getWorld().isClient) {
                ItemStack stack = trident.getItemStack();
                if (stack != null && player.getInventory().insertStack(stack)) {
                    Netherinth.LOGGER.info("Trident picked up successfully");
                    trident.discard();
                    TridentStorage.removeTrident(player);
                    activePulls.remove(player.getUuid());
                }
            }
        }
    }
}