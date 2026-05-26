package com.clocken.smash;

import com.evandev.treeliable.client.Client;
import com.evandev.treeliable.common.chop.ChopUtil;
import com.evandev.treeliable.common.chop.FellQueue;
import com.evandev.treeliable.common.config.ModConfig;
import com.evandev.treeliable.server.NeoForgeServer;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;

import java.util.*;

@Mod(Smash.MODID)
public class Smash
{
    public static final String MODID = "smash";
    public static final String MOD_NAME = "Treeliable: Smash the Ore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Smash(IEventBus modEventBus) {
        AutoConfig.register(SmashConfig.class, JanksonConfigSerializer::new);

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, screen) -> AutoConfig.getConfigScreen(SmashConfig.class, screen).get()
        );

        NeoForgeServer.init(modEventBus);
        LOGGER.info(MOD_NAME + " was loaded.");
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(Smash.MODID, path);
    }

    @EventBusSubscriber(modid = Smash.MODID)
    public static class SmashCommon {
        private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

        @SubscribeEvent
        public static void onBreakEvent(BlockEvent.BreakEvent event) {
            if (event.isCanceled()
                    || !(event.getLevel() instanceof ServerLevel level)
                    || !(event.getPlayer() instanceof ServerPlayer agent)
                    || !SmashConfig.get().enabled) {
                return;
            }

            BlockPos initialPos = event.getPos();
            BlockState state = level.getBlockState(initialPos);
            if(HARVESTED_BLOCKS.contains(initialPos)) return;

            if (SmashUtil.canSmashWithTool(agent, level, initialPos) && ChopUtil.playerWantsToChop(agent, Client.getChopSettings())) {
                Map<Integer, List<Runnable>> layeredActions = new TreeMap<>();

                int index = 0;
                if (SmashConfig.get().smashBehavior == SmashBehavior.BY_LAYER) {
                    for (List<BlockPos> positions : SmashUtil.getLayers(initialPos, agent, level)) {

                        index++;
                        layeredActions.put(
                                index,
                                List.of(() -> {
                                    for (BlockPos pos : positions) {
                                        if (HARVESTED_BLOCKS.add(pos)) {
                                            agent.gameMode.destroyBlock(pos);
                                            HARVESTED_BLOCKS.remove(pos);
                                        }
                                    }
                                    level.levelEvent(2001, initialPos, Block.getId(state));
                                })
                        );
                    }
                } else {
                    for(BlockPos pos : SmashUtil.getBlocksToBeSmashed(initialPos, agent, level)) {

                        index++;
                        layeredActions.put(
                                index,
                                List.of(() -> {
                                    HARVESTED_BLOCKS.add(pos);
                                    agent.gameMode.destroyBlock(pos);
                                    level.levelEvent(2001, pos, Block.getId(state));
                                    HARVESTED_BLOCKS.remove(pos);
                                })
                        );
                    }
                }

                if (ModConfig.get().delayFellingLayers) {
                    FellQueue.addTask(layeredActions, ModConfig.get().fellingLayerDelayTicks, ModConfig.get().exponentialFellingSpeedup);
                } else {
                    for (List<Runnable> layer : layeredActions.values()) {
                        for (Runnable action : layer) {
                            action.run();
                        }
                    }
                }
            }
        }
    }
}
