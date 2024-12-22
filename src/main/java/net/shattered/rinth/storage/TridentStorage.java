package net.shattered.rinth.storage;

import net.minecraft.entity.player.PlayerEntity;
import net.shattered.rinth.entity.CustomTridentEntity;
import net.shattered.rinth.Netherinth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TridentStorage {
    private static final Map<UUID, CustomTridentEntity> thrownTridents = new HashMap<>();

    public static void storeTrident(PlayerEntity player, CustomTridentEntity trident) {
        Netherinth.LOGGER.info("Storing trident for player: " + player.getName().getString());
        thrownTridents.put(player.getUuid(), trident);
        debugPrintStorage();
    }

    public static CustomTridentEntity getTrident(PlayerEntity player) {
        Netherinth.LOGGER.info("Attempting to get trident for player: " + player.getName().getString());
        CustomTridentEntity trident = thrownTridents.get(player.getUuid());
        if (trident != null && trident.isAlive()) {
            Netherinth.LOGGER.info("Found valid trident");
            return trident;
        }
        Netherinth.LOGGER.info("No valid trident found");
        thrownTridents.remove(player.getUuid());
        debugPrintStorage();
        return null;
    }

    public static void removeTrident(PlayerEntity player) {
        Netherinth.LOGGER.info("Removing trident for player: " + player.getName().getString());
        thrownTridents.remove(player.getUuid());
        debugPrintStorage();
    }

    public static boolean hasTrident(PlayerEntity player) {
        boolean has = thrownTridents.containsKey(player.getUuid());
        Netherinth.LOGGER.info("Checking if player " + player.getName().getString() + " has trident: " + has);
        return has;
    }

    private static void debugPrintStorage() {
        Netherinth.LOGGER.info("Current Trident Storage State:");
        thrownTridents.forEach((uuid, trident) -> {
            Netherinth.LOGGER.info("UUID: " + uuid + " -> Trident alive: " + (trident != null ? trident.isAlive() : "null"));
        });
    }
}