package dev.nokhxyr.wherewasi.record;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;

import dev.nokhxyr.wherewasi.model.EventType;

/**
 * Client-side item-movement capture by diffing the player inventory each tick and
 * reading the context to classify the change: a gain/loss while a storage container
 * is open is a put/take, a gain in the open world is a pickup, and a loss in the open
 * world right after the drop key is a toss. Consumption / crafting / placing (a loss
 * with no drop key, or a change behind a workstation screen) is deliberately ignored.
 * The {@link ActivityRecorder} coalesces the stream into counted entries.
 */
public final class InventoryTransactionWatcher implements Sampler {

    private static final long DROP_WINDOW_MS = 400L;

    private final Map<String, Integer> prev = new HashMap<>();
    private boolean primed;
    private boolean dropWasDown;
    private long lastDropMs;

    @Override
    public void onSessionStart(RecorderContext ctx) {
        prev.clear();
        primed = false;
        dropWasDown = false;
        lastDropMs = 0L;
    }

    @Override
    public void tick(RecorderContext ctx) {
        Minecraft mc = ctx.mc();
        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }

        // Rising edge of the drop key, so an in-world loss can be told from a use.
        boolean dropDown = mc.options.keyDrop.isDown();
        if (dropDown && !dropWasDown) {
            lastDropMs = ctx.now();
        }
        dropWasDown = dropDown;

        Map<String, Integer> cur = snapshot(p);
        if (!primed) {
            prev.clear();
            prev.putAll(cur);
            primed = true;
            return;
        }

        boolean storage = isStorageMenu(p);
        boolean inWorld = mc.screen == null;
        boolean dropRecent = ctx.now() - lastDropMs <= DROP_WINDOW_MS;
        BlockPos pos = p.blockPosition();
        String dim = p.level().dimension().location().toString();

        Set<String> ids = new HashSet<>(prev.keySet());
        ids.addAll(cur.keySet());
        for (String id : ids) {
            int delta = cur.getOrDefault(id, 0) - prev.getOrDefault(id, 0);
            if (delta == 0) {
                continue;
            }
            EventType type = classify(delta > 0, storage, inWorld, dropRecent);
            if (type != null) {
                ctx.recorder().onItemTransaction(type, id, Math.abs(delta),
                        pos.getX(), pos.getY(), pos.getZ(), dim);
            }
        }
        prev.clear();
        prev.putAll(cur);
    }

    private static EventType classify(boolean gained, boolean storage, boolean inWorld, boolean dropRecent) {
        if (storage) {
            return gained ? EventType.STORAGE_TAKE : EventType.STORAGE_PUT;
        }
        if (inWorld) {
            if (gained) {
                return EventType.ITEM_PICKUP;
            }
            return dropRecent ? EventType.ITEM_DROP : null; // a non-drop loss (used/eaten/placed) is ignored
        }
        return null; // a workstation or the player's own inventory screen — not a transaction
    }

    /**
     * Storage = any external container the player has open that isn't a known vanilla
     * workstation. Rather than whitelisting chest/barrel/etc. (which misses modded
     * storage like Refined Storage, Mekanism or Sophisticated Storage), we accept every
     * non-inventory menu and only exclude the vanilla crafting/processing screens — so
     * a modded storage GUI is treated as storage automatically.
     */
    private static boolean isStorageMenu(LocalPlayer p) {
        AbstractContainerMenu menu = p.containerMenu;
        if (menu == null || menu == p.inventoryMenu) {
            return false; // no external container open
        }
        return !isWorkstation(menu);
    }

    private static boolean isWorkstation(AbstractContainerMenu menu) {
        return menu instanceof CraftingMenu
                || menu instanceof AbstractFurnaceMenu
                || menu instanceof AnvilMenu
                || menu instanceof GrindstoneMenu
                || menu instanceof LoomMenu
                || menu instanceof CartographyTableMenu
                || menu instanceof StonecutterMenu
                || menu instanceof SmithingMenu
                || menu instanceof EnchantmentMenu
                || menu instanceof BrewingStandMenu
                || menu instanceof BeaconMenu
                || menu instanceof MerchantMenu;
    }

    private static Map<String, Integer> snapshot(LocalPlayer p) {
        Map<String, Integer> counts = new HashMap<>();
        Inventory inv = p.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
        }
        return counts;
    }
}
