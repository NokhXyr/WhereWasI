package dev.nokhxyr.wherewasi.storage;

import java.nio.file.Path;

import net.neoforged.fml.loading.FMLPaths;

/**
 * Resolves where journals live: {@code .minecraft/wherewasi/<worldId>/}.
 */
public final class JournalPaths {

    private JournalPaths() {
    }

    public static Path baseDir() {
        return FMLPaths.GAMEDIR.get().resolve("wherewasi");
    }

    public static Path worldDir(String worldId) {
        return baseDir().resolve(worldId);
    }
}
