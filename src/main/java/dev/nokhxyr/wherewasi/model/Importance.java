package dev.nokhxyr.wherewasi.model;

import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Turns an event + its payload into an importance score. This is what makes the
 * briefing meaningful: the first diamond outranks the hundredth stack of cobble.
 * Kept intentionally simple — a base score per {@link EventType} plus a rarity
 * bonus derived from the item/block involved.
 */
public final class Importance {

    /** Items that are always noteworthy the first time, regardless of vanilla rarity tier. */
    private static final Set<ResourceLocation> HEADLINE = Set.of(
            rl("diamond"), rl("netherite_ingot"), rl("netherite_scrap"), rl("ancient_debris"),
            rl("elytra"), rl("nether_star"), rl("beacon"), rl("dragon_egg"), rl("dragon_head"),
            rl("totem_of_undying"), rl("enchanted_golden_apple"), rl("heart_of_the_sea"),
            rl("conduit"), rl("netherite_pickaxe"), rl("netherite_sword"), rl("netherite_chestplate"),
            rl("wither_skeleton_skull"), rl("trident"), rl("shulker_shell"), rl("echo_shard")
    );

    private Importance() {
    }

    public static int score(EventType type, Map<String, String> payload) {
        int base = type.baseImportance();
        return switch (type) {
            case FIRST_ACQUIRE -> base + itemBonus(payload.get("item"));
            case MINE_MILESTONE -> base + itemBonus(payload.get("block"));
            case BULK_ACQUIRE -> base + Math.min(3, parse(payload.get("count")) / 256);
            case ADVANCEMENT -> base + ("challenge".equals(payload.get("frame")) ? 3 : 0);
            default -> base;
        };
    }

    private static int itemBonus(String id) {
        ResourceLocation rl = id == null ? null : ResourceLocation.tryParse(id);
        if (rl == null) {
            return 0;
        }
        if (HEADLINE.contains(rl)) {
            return 6;
        }
        Item item = BuiltInRegistries.ITEM.get(rl);
        Rarity rarity = new ItemStack(item).getRarity();
        return switch (rarity) {
            case EPIC -> 5;
            case RARE -> 4;
            case UNCOMMON -> 2;
            default -> 0;
        };
    }

    private static int parse(String s) {
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
