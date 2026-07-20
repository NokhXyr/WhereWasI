package dev.nokhxyr.wherewasi;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Entry point. {@code dist = Dist.CLIENT} means this class — and therefore every
 * listener it registers — is only ever constructed on the physical client. Nothing
 * touches a dedicated server, so the jar is inert (but harmless) if installed there.
 * Combined with {@code displayTest = IGNORE_ALL_VERSION} in the mods.toml, the mod
 * connects to any server, vanilla or modded.
 */
@Mod(value = WhereWasI.MOD_ID, dist = Dist.CLIENT)
public final class WhereWasI {

    public static final String MOD_ID = "wherewasi";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WhereWasI(ModContainer container, IEventBus modBus) {
        container.registerConfig(ModConfig.Type.CLIENT, WhereWasIConfig.SPEC);

        // Mod bus: registration events.
        modBus.addListener(Keybinds::onRegisterKeyMappings);
        modBus.addListener(ClientEvents::onRegisterGuiLayers);

        // Game bus: the tick loop that drives capture, and session teardown.
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onLoggingOut);

        LOGGER.info("Where Was I? loaded — client-side play journal ready.");
    }
}
