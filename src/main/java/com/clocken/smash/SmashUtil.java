package com.clocken.smash;

import com.evandev.treeliable.client.Client;
import com.evandev.treeliable.client.settings.ClientChopSettings;
import com.evandev.treeliable.common.chop.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

public class SmashUtil {

    public static boolean canSmashWithTool(Player player, Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        ItemStack tool = player.getMainHandItem();

        return tool.getItem() instanceof PickaxeItem
                && blockState.is(Tags.Blocks.ORES)
                && (!blockState.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(blockState));
    }

    public static boolean blockCanBeSmashed(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        ClientChopSettings chopSettings = Client.getChopSettings();

        if (player == null || level == null) {
            return false;
        }

        return canSmashWithTool(player, level, pos)
                && ChopUtil.playerWantsToChop(minecraft.player, chopSettings);
    }

    public static List<BlockPos> getBlocksToBeSmashed(BlockPos initialPos, Player player, Level level) {
        List<BlockPos> positions = new ArrayList<>();

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    BlockPos pos = new BlockPos(initialPos.getX() + x, initialPos.getY() + y, initialPos.getZ() + z);

                    if(!pos.equals(initialPos) && SmashUtil.canSmashWithTool(player, level, pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    public static List<List<BlockPos>> getLayers(BlockPos initialPos, Player player, Level level) {
        return List.of(
                getLayer(initialPos, player, level, 0),
                getLayer(initialPos, player, level, 1),
                getLayer(initialPos, player, level, -1)
        );
    }

    public static List<BlockPos> getLayer(BlockPos initialPos, Player player, Level level, int y) {
        List<BlockPos> positions = new ArrayList<>();

        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                BlockPos pos = new BlockPos(initialPos.getX() + x, initialPos.getY() + y, initialPos.getZ() + z);

                if(!pos.equals(initialPos) && SmashUtil.canSmashWithTool(player, level, pos)) {
                    positions.add(pos);
                }
            }
        }
        return positions;
    }
}
