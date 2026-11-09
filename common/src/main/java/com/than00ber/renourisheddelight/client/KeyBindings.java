package com.than00ber.renourisheddelight.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    public static final KeyMapping VIEW_NUTRITION = new KeyMapping("key.renourisheddelight.view_nutrition", InputConstants.KEY_LSHIFT, "key.categories.renourisheddelight");

    public static boolean isDown(KeyMapping mapping) {
        if (!mapping.isUnbound()) {
            InputConstants.Key key = InputConstants.getKey(mapping.saveString());
            long window = Minecraft.getInstance().getWindow().getWindow();

            return switch (key.getType()) {
                case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
                case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
                case SCANCODE -> false;
            };
        }
        return false;
    }
}
