package dev.nokhxyr.wherewasi.record;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;

import dev.nokhxyr.wherewasi.WhereWasI;

/**
 * The one place WhereWasI reaches into a private vanilla field: the client's
 * advancement progress map, which has no public getter. NeoForge runs on Mojang
 * mappings at runtime, so the field is literally {@code progress} in both dev and
 * production. Access is guarded — if a future MC version renames it, advancement
 * tracking disables itself with a single log line instead of crashing.
 */
final class ClientReflect {

    private static Field progressField;
    private static boolean unavailable;

    private ClientReflect() {
    }

    @SuppressWarnings("unchecked")
    static Map<AdvancementHolder, AdvancementProgress> advancementProgress(ClientAdvancements advancements) {
        if (unavailable || advancements == null) {
            return null;
        }
        try {
            if (progressField == null) {
                progressField = ClientAdvancements.class.getDeclaredField("progress");
                progressField.setAccessible(true);
            }
            return (Map<AdvancementHolder, AdvancementProgress>) progressField.get(advancements);
        } catch (Throwable t) {
            unavailable = true;
            WhereWasI.LOGGER.warn("WhereWasI: advancement tracking unavailable (field access failed); disabling it.", t);
            return null;
        }
    }
}
