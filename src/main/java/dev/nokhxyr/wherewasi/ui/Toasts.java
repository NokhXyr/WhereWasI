package dev.nokhxyr.wherewasi.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/**
 * Thin wrapper over the vanilla toast system. In 1.21.1 {@code SystemToastId} has
 * a public constructor, so we mint a fresh id per toast — that lets WhereWasI toasts
 * stack instead of replacing one another and avoids colliding with vanilla ids.
 */
public final class Toasts {

    private Toasts() {
    }

    public static void show(Component title, Component message) {
        Minecraft mc = Minecraft.getInstance();
        SystemToast.add(mc.getToasts(), new SystemToast.SystemToastId(), title, message);
    }
}
