package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.bakedlibs.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockBreakEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ToolUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The {@link BlockListener} is responsible for listening to the {@link BlockPlaceEvent}
 * and {@link BlockBreakEvent}.
 *
 * @author TheBusyBiscuit
 * @author Linox
 * @author Patbox
 * @see BlockPlaceHandler
 * @see BlockBreakHandler
 * @see ToolUseHandler
 */
public class BlockListener implements Listener {

    public BlockListener(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlaceExisting(BlockPlaceEvent e) {
        Block block = e.getBlock();
        var loc = block.getLocation();

        // Fixes #2636 - This will solve the "ghost blocks" issue
        if (e.getBlockReplacedState().getType().isAir()) {
            var blockData = StorageCacheUtils.getBlock(loc);
            if (blockData != null && blockData.isPendingRemove()) {
                e.setCancelled(true);
                return;
            }

            SlimefunItem sfItem = StorageCacheUtils.getSfItem(loc);
            if (sfItem != null) {
                for (ItemStack item : sfItem.getDrops()) {
                    if (item != null && !item.getType().isAir()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), item);
                    }
                }

                Slimefun.getDatabaseManager().getBlockDataController().removeBlock(loc);

                if (SlimefunItem.getByItem(e.getItemInHand()) != null) {
                    // Due to the delay of #clearBlockInfo, new sf block info will also be cleared. Set cancelled.
                    e.setCancelled(true);
                }
            }
        } else if (StorageCacheUtils.hasBlock(loc)) {
            // If there is no air (e.g. grass) then don't let the block be placed
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(item);

        // TODO: Protection manager is null in testing environment.
        if (!Slimefun.instance().isUnitTest()) {
            Slimefun.getProtectionManager().logAction(e.getPlayer(), e.getBlock(), Interaction.BREAK_BLOCK);
        }

        if (sfItem != null && !(sfItem instanceof NotPlaceable)) {
            if (!sfItem.canUse(e.getPlayer(), true)) {
                e.setCancelled(true);
            } else {
                var placeEvent = new SlimefunBlockPlaceEvent(e.getPlayer(), item, e.getBlock(), sfItem);
                Bukkit.getPluginManager().callEvent(placeEvent);

                Slimefun.getDatabaseManager().getBlockDataController().createBlock(e.getBlock().getLocation(), sfItem.getId());
                sfItem.callItemHandler(BlockPlaceHandler.class, handler -> handler.onPlayerPlace(e));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        // Simply ignore any events that were faked by other plugins
        if (Slimefun.getIntegrations().isEventFaked(e)) {
            return;
        }

        // Also ignore custom blocks which were placed by other plugins
        if (Slimefun.getIntegrations().isCustomBlock(e.getBlock())) {
            return;
        }

        var item = e.getPlayer().getInventory().getItemInMainHand();
        var block = e.getBlock();
        var blockData = StorageCacheUtils.getBlock(block.getLocation());

        // If there is a Slimefun Block here, call our BreakEvent and, if cancelled, cancel this event and return
        if (blockData != null) {
            var sfItem = SlimefunItem.getById(blockData.getSfId());
            SlimefunBlockBreakEvent breakEvent = new SlimefunBlockBreakEvent(e.getPlayer(), item, e.getBlock(), sfItem);
            Bukkit.getPluginManager().callEvent(breakEvent);

            if (breakEvent.isCancelled()) {
                e.setCancelled(true);
                return;
            }
        }

        if (!e.isCancelled()) {
            int fortune = getBonusDropsWithFortune(item, e.getBlock());
            List<ItemStack> drops = new ArrayList<>();
            checkForSensitiveBlockAbove(e, item);

            if (!item.getType().isAir()) {
                callToolHandler(e, item, fortune, drops);
            }

            if (blockData == null || blockData.isPendingRemove()) {
                return;
            }

            blockData.setPendingRemove(true);

            if (!blockData.isDataLoaded()) {
                e.setDropItems(false);
                var type = block.getType();
                StorageCacheUtils.executeAfterLoad(blockData, () -> {
                    callBlockHandler(e, item, drops);
                    if (e.isCancelled()) {
                        block.setType(type);
                        blockData.setPendingRemove(false);
                        return;
                    }
                    e.setDropItems(true);
                    dropItems(e, drops);
                }, true);
                return;
            }

            callBlockHandler(e, item, drops);
            if (e.isCancelled()) {
                blockData.setPendingRemove(false);
            }
            dropItems(e, drops);
        }
    }

    @ParametersAreNonnullByDefault
    private void callToolHandler(BlockBreakEvent e, ItemStack item, int fortune, List<ItemStack> drops) {
        SlimefunItem tool = SlimefunItem.getByItem(item);

        if (tool != null) {
            if (tool.canUse(e.getPlayer(), true)) {
                tool.callItemHandler(ToolUseHandler.class, handler -> handler.onToolUse(e, item, fortune, drops));
            } else {
                e.setCancelled(true);
            }
        }
    }

    @ParametersAreNonnullByDefault
    private void callBlockHandler(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
        var loc = e.getBlock().getLocation();
        SlimefunItem sfItem = StorageCacheUtils.getSfItem(loc);

        if (sfItem != null && !sfItem.useVanillaBlockBreaking()) {
            sfItem.callItemHandler(BlockBreakHandler.class, handler -> handler.onPlayerBreak(e, item, drops));

            if (e.isCancelled()) {
                return;
            }

            drops.addAll(sfItem.getDrops());
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(loc);
        }
    }

    @ParametersAreNonnullByDefault
    private void dropItems(BlockBreakEvent e, List<ItemStack> drops) {
        if (!drops.isEmpty()) {
            // TODO: properly support loading inventories within unit tests
            if (!Slimefun.instance().isUnitTest()) {
                // Notify plugins like CoreProtect
                Slimefun.getProtectionManager().logAction(e.getPlayer(), e.getBlock(), Interaction.BREAK_BLOCK);
            }

            // Fixes #2560
            if (e.isDropItems()) {
                // Disable normal block drops
                e.setDropItems(false);

                for (ItemStack drop : drops) {
                    // Prevent null or air from being dropped
                    if (drop != null && drop.getType() != Material.AIR) {
                        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);
                    }
                }
            }
        }
    }

    /**
     * This method checks for a sensitive {@link Block}.
     * Sensitive {@link Block Blocks} are pressure plates or saplings, which should be broken
     * when the block beneath is broken as well.
     *
     * @param e    The {@link Player} who broke this {@link Block}
     * @param item The {@link Block} that was broken
     */
    @ParametersAreNonnullByDefault
    private void checkForSensitiveBlockAbove(BlockBreakEvent e, ItemStack item) {
        Block blockAbove = e.getBlock().getRelative(BlockFace.UP);

        if (SlimefunTag.SENSITIVE_MATERIALS.isTagged(blockAbove.getType())) {
            var loc = blockAbove.getLocation();
            var blockData = StorageCacheUtils.getBlock(loc);
            SlimefunItem sfItem = StorageCacheUtils.getSfItem(loc);

            if (sfItem != null && !sfItem.useVanillaBlockBreaking()) {
                /*
                 * We create a dummy here to pass onto the BlockBreakHandler.
                 * This will set the correct block context.
                 */
                BlockBreakEvent dummyEvent = new BlockBreakEvent(blockAbove, e.getPlayer());
                List<ItemStack> drops = new ArrayList<>();
                drops.addAll(sfItem.getDrops(e.getPlayer()));

                var controller = Slimefun.getDatabaseManager().getBlockDataController();
                if (blockData.isDataLoaded()) {
                    sfItem.callItemHandler(BlockBreakHandler.class, handler -> handler.onPlayerBreak(dummyEvent, item, drops));
                    controller.removeBlock(loc);
                    dropItems(dummyEvent, drops);
                } else {
                    blockData.setPendingRemove(true);
                    controller.loadBlockDataAsync(blockData, new IAsyncReadCallback<>() {
                        @Override
                        public boolean runOnMainThread() {
                            return true;
                        }

                        @Override
                        public void onResult(SlimefunBlockData result) {
                            sfItem.callItemHandler(BlockBreakHandler.class, handler -> handler.onPlayerBreak(dummyEvent, item, drops));
                            controller.removeBlock(loc);
                            dropItems(dummyEvent, drops);
                        }
                    });
                }
                blockAbove.setType(Material.AIR);
            }
        }
    }

    private int getBonusDropsWithFortune(@Nullable ItemStack item, @Nonnull Block b) {
        int amount = 1;

        if (item != null && !item.getType().isAir() && item.hasItemMeta()) {
            /*
             * Small performance optimization:
             * ItemStack#getEnchantmentLevel() calls ItemStack#getItemMeta(), so if
             * we are handling more than one Enchantment, we should access the ItemMeta
             * directly and reuse it.
             */
            ItemMeta meta = item.getItemMeta();
            int fortuneLevel = meta.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);

            if (fortuneLevel > 0 && !meta.hasEnchant(Enchantment.SILK_TOUCH)) {
                Random random = ThreadLocalRandom.current();

                amount = Math.max(1, random.nextInt(fortuneLevel + 2) - 1);
                amount = (b.getType() == Material.LAPIS_ORE ? 4 + random.nextInt(5) : 1) * (amount + 1);
            }
        }

        return amount;
    }
}