package dev.nokhxyr.wherewasi;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import dev.nokhxyr.wherewasi.record.ActivityRecorder;

/**
 * Turns the local player's own block interactions into journal entries: left-click
 * feeds the break run, right-click either opens/uses an interactive block (chest,
 * furnace, door, workstation…) or feeds the place run. Aggregation and de-duplication
 * live in {@link ActivityRecorder}; this class only classifies each raw click.
 */
public final class InteractionEvents {

    private InteractionEvents() {
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() != Minecraft.getInstance().player) {
            return;
        }
        ActivityRecorder rec = ClientState.recorder();
        if (!rec.sessionActive()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        rec.onBlockBroken(idOf(state.getBlock()), pos.getX(), pos.getY(), pos.getZ(), dimOf(level));
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || event.getEntity() != player) {
            return;
        }
        ActivityRecorder rec = ClientState.recorder();
        if (!rec.sessionActive()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        String dim = dimOf(level);

        boolean menu = state.getMenuProvider(level, pos) != null;
        boolean interactive = menu || isToggle(block);
        if (interactive && !player.isShiftKeyDown()) {
            rec.onInteract(idOf(block), pos.getX(), pos.getY(), pos.getZ(), dim, menu);
            return;
        }

        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof BlockItem blockItem) {
            Direction face = event.getFace() == null ? Direction.UP : event.getFace();
            BlockPos placed = pos.relative(face);
            rec.onBlockPlaced(idOf(blockItem.getBlock()), placed.getX(), placed.getY(), placed.getZ(), dim);
        }
    }

    /** Interactive blocks that toggle/open without a container menu. */
    private static boolean isToggle(Block block) {
        return block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof FenceGateBlock
                || block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof BedBlock
                || block instanceof CraftingTableBlock;
    }

    private static String idOf(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static String dimOf(Level level) {
        return level.dimension().location().toString();
    }
}
