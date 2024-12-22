package net.shattered.rinth.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.shattered.rinth.Netherinth;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    private static KeyBinding recallKey;

    public static void registerKeyBinds() {
        recallKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rinth.recall",  // Translation key
                InputUtil.Type.MOUSE,  // Input type (MOUSE for right click)
                GLFW.GLFW_MOUSE_BUTTON_2,  // Right mouse button
                "category.rinth.keybinds"  // Category translation key
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (recallKey.wasPressed() && client.player != null && client.player.getMainHandStack().isEmpty()) {
                Netherinth.LOGGER.info("Recall key pressed with empty hand!");
                TridentRecallManager.handleRecallRequest(client.player);
            }
        });
    }

    public static boolean isRecallKeyPressed() {
        return recallKey.isPressed();
    }
}