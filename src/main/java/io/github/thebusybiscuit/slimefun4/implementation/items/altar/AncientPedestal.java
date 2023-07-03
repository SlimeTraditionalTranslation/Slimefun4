package io.github.thebusybiscuit.slimefun4.implementation.items.altar;

import city.norain.slimefun4.pdc.datatypes.DataTypes;
import io.github.bakedlibs.dough.blocks.BlockPosition;
import io.github.bakedlibs.dough.common.ChatColors;
import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.bakedlibs.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSpawnReason;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockDispenseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.listeners.AncientAltarListener;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.AncientAltarTask;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * The {@link AncientPedestal} is a part of the {@link AncientAltar}.
 * You can place any {@link ItemStack} onto the {@link AncientPedestal} to provide it to
 * the altar as a crafting ingredient.
 * 
 * @author Redemption198
 * @author TheBusyBiscuit
 * @author StarWishsama
 *
 * @see AncientAltar
 * @see AncientAltarListener
 * @see AncientAltarTask
 *
 */
public class AncientPedestal extends SimpleSlimefunItem<BlockDispenseHandler> {

    public static final String ITEM_PREFIX = ChatColors.color("&dALTAR &3Probe - &e");

    private static final NamespacedKey ALTAR_DISPLAY_ITEM = new NamespacedKey(Slimefun.instance(), "ALTAR_DISPLAY_ITEM");

    private static final Map<BlockPosition, UUID> pedestalVirtualItemCache = new ConcurrentHashMap<>();

    @ParametersAreNonnullByDefault
    public AncientPedestal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);

        addItemHandler(onBreak());
    }

    private @Nonnull BlockBreakHandler onBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                Optional<Item> entity = getPlacedItem(b);

                if (entity.isPresent()) {
                    Item stack = entity.get();

                    if (stack.isValid()) {
                        stack.removeMetadata("no_pickup", Slimefun.instance());
                        b.getWorld().dropItem(b.getLocation(), getOriginalItemStack(stack));
                        removeVirtualItem(b.getLocation(), stack);
                    }
                }
            }
        };
    }

    @Override
    public @Nonnull BlockDispenseHandler getItemHandler() {
        return (e, d, block, machine) -> e.setCancelled(true);
    }

    public @Nonnull Optional<Item> getPlacedItem(@Nonnull Block pedestal) {
        UUID itemUUID = pedestalVirtualItemCache.get(new BlockPosition(pedestal));

        if (itemUUID != null) {
            Entity cache = Bukkit.getEntity(itemUUID);
            if (cache instanceof Item) {
                return Optional.of((Item) cache);
            }
        }

        // If cache was deleted, use old method to find nearby possible display item entity.
        Location l = pedestal.getLocation().clone().add(0.5, 1.2, 0.5);

        for (Entity n : l.getWorld().getNearbyEntities(l, 0.5, 0.5, 0.5, this::testItem)) {
            if (n instanceof Item && n.isValid()) {
                Optional<Item> item = Optional.of((Item) n);

                startItemWatcher(pedestal.getLocation(), (Item) n);
                return item;
            }
        }

        return Optional.empty();
    }

    private boolean testItem(@Nullable Entity n) {
        if (n instanceof Item item && n.isValid()) {
            ItemMeta meta = item.getItemStack().getItemMeta();

            return meta.hasDisplayName() && meta.getDisplayName().startsWith(ITEM_PREFIX);
        } else {
            return false;
        }
    }

    public @Nonnull ItemStack getOriginalItemStack(@Nonnull Item item) {
        ItemStack stack = item.getItemStack().clone();
        var im = stack.getItemMeta();

        if (im != null) {
            var originalItem = im.getPersistentDataContainer().get(ALTAR_DISPLAY_ITEM, DataTypes.ITEMSTACK);

            if (originalItem != null) {
                return originalItem;
            } else {
                String customName = item.getCustomName();

                if (customName.equals(ItemUtils.getItemName(new ItemStack(stack.getType())))) {
                    im.setDisplayName(null);
                    stack.setItemMeta(im);
                } else {
                    im.setDisplayName(customName);
                    stack.setItemMeta(im);
                }
            }
        }

        return stack;
    }

    public void placeItem(@Nonnull Player p, @Nonnull Block b) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemStack displayItem = new CustomItemStack(hand, ITEM_PREFIX + System.nanoTime());
        ItemStack handCopy = hand.clone();
        handCopy.setAmount(1);

        var displayItemMeta = displayItem.getItemMeta();
        displayItemMeta.getPersistentDataContainer().set(ALTAR_DISPLAY_ITEM, DataTypes.ITEMSTACK, handCopy);
        displayItem.setItemMeta(displayItemMeta);
        displayItem.setAmount(1);

        // Get the display name of the original Item in the Player's hand
        String nametag = ItemUtils.getItemName(hand);

        if (p.getGameMode() != GameMode.CREATIVE) {
            ItemUtils.consumeItem(hand, false);
        }

        Location pedestalLocation = b.getLocation();
        Location spawnLocation = pedestalLocation.clone().add(0.5, 1.2, 0.5);
        Item entity = SlimefunUtils.spawnItem(spawnLocation, displayItem, ItemSpawnReason.ANCIENT_PEDESTAL_PLACE_ITEM);

        if (entity != null) {
            entity.setVelocity(new Vector(0, 0.1, 0));
            entity.setCustomNameVisible(true);
            entity.setCustomName(nametag);
            SlimefunUtils.markAsNoPickup(entity, "altar_item");
            p.playSound(pedestalLocation, Sound.ENTITY_ITEM_PICKUP, 0.3F, 0.3F);

            startItemWatcher(pedestalLocation, entity);
        }
    }

    /**
     * Remove virtual item upon pedestal
     *
     * @param pedestal ancient pedestal location
     * @param item virtual item
     */
    public void removeVirtualItem(@Nonnull Location pedestal, @Nonnull Entity item) {
        item.remove();
        if (Bukkit.getEntity(item.getUniqueId()) != null) {
            Bukkit.getEntity(item.getUniqueId()).remove();
        }
        pedestalVirtualItemCache.remove(new BlockPosition(pedestal));
    }

    public @Nonnull Map<BlockPosition, UUID> getVirtualItemCache() {
        return pedestalVirtualItemCache;
    }

    /**
     * Start a watcher to monitor the location of a virtual item
     *
     * @param pedestalLocation the location of pedestal
     * @param item virtual item
     */
    private void startItemWatcher(@Nonnull Location pedestalLocation, @Nonnull Item item) {
        pedestalVirtualItemCache.put(new BlockPosition(pedestalLocation), item.getUniqueId());
    }
}
