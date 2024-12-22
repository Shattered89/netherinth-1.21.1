package net.shattered.rinth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.util.Identifier;
import net.shattered.rinth.block.ModBlocks;
import net.shattered.rinth.component.ModDataComponentTypes;
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

		ModDataComponentTypes.registerModDataComponentTypes();

		FuelRegistry.INSTANCE.add(ModItems.STARLIGHT_ASHES, 2400);
	}
}