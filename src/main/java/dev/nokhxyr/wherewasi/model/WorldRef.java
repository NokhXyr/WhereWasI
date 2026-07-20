package dev.nokhxyr.wherewasi.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Identity of the world the player is currently in, used to keep journals apart.
 *
 * <p>{@link #id()} is a filesystem-safe key (the save folder name in singleplayer,
 * the server address in multiplayer); {@link #name()} is a human label.
 */
public record WorldRef(String id, String name) {

    public static WorldRef current(Minecraft mc) {
        IntegratedServer sp = mc.getSingleplayerServer();
        if (sp != null) {
            String folder;
            try {
                folder = sp.getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
            } catch (Exception e) {
                folder = "world";
            }
            if (folder == null || folder.isBlank() || folder.equals(".")) {
                folder = "world";
            }
            return new WorldRef("sp_" + sanitize(folder), folder);
        }

        ServerData sd = mc.getCurrentServer();
        if (sd != null && sd.ip != null && !sd.ip.isBlank()) {
            String label = (sd.name != null && !sd.name.isBlank()) ? sd.name : sd.ip;
            return new WorldRef("mp_" + sanitize(sd.ip), label);
        }

        return new WorldRef("unknown", "Unknown");
    }

    private static String sanitize(String s) {
        String cleaned = s.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.length() > 80 ? cleaned.substring(0, 80) : cleaned;
    }
}
