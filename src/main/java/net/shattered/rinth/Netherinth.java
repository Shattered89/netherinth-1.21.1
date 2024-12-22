package net.shattered.rinth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.shattered.rinth.block.ModBlocks;
import net.shattered.rinth.component.ModDataComponentTypes;
import net.shattered.rinth.event.RecallDetector;
import net.shattered.rinth.event.TridentRecallManager;
import net.shattered.rinth.item.ModItemGroups;
import net.shattered.rinth.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netherinth implements ModInitializer {
	public static final String MOD_ID = "rinth";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier of(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModItemGroups.registerModItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		TridentRecallManager.register();
		RecallDetector.register();

		ModDataComponentTypes.registerModDataComponentTypes();

		FuelRegistry.INSTANCE.add(ModItems.STARLIGHT_ASHES, 2400);

		// Register server tick event for trident recall
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				TridentRecallManager.tick(world);
			}
		});
	}
}