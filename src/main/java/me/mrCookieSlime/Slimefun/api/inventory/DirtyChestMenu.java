package me.mrCookieSlime.Slimefun.api.inventory;

import city.norain.slimefun4.utils.StringUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.DataUtils;
import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.core.debug.TestCase;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.bakedlibs.dough.inventory.InvUtils;
import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.bakedlibs.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;

// This class will be deprecated, relocated and rewritten in a future version.
public class DirtyChestMenu extends ChestMenu {

    protected final BlockMenuPreset preset;
    protected int changes = 1;

    public DirtyChestMenu(@Nonnull BlockMenuPreset preset) {
        super(preset.getTitle());

        this.preset = preset;
    }

    /**
     * This method checks whether this {@link DirtyChestMenu} is currently viewed by a {@link Player}.
     *
     * @return Whether anyone is currently viewing this {@link Inventory}
     */
    public boolean hasViewer() {
        Inventory inv = toInventory();
        return inv != null && !inv.getViewers().isEmpty();
    }

    public void markDirty() {
        changes++;
    }

    public boolean isDirty() {
        return changes > 0;
    }

    public int getUnsavedChanges() {
        return changes;
    }

    @Nonnull
    public BlockMenuPreset getPreset() {
        return preset;
    }

    public boolean canOpen(Block b, Player p) {
        return preset.canOpen(b, p);
    }

    @Override
    public void open(Player... players) {
        super.open(players);

        // The Inventory will likely be modified soon
        markDirty();
    }

    public void close() {
        for (HumanEntity human : new ArrayList<>(toInventory().getViewers())) {
            human.closeInventory();
        }
    }

    public boolean fits(@Nonnull ItemStack item, int... slots) {
        Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - start check fits | item {} | slots {}", StringUtil.itemStackToString(item), Arrays.stream(slots).mapToObj(String::valueOf).collect(Collectors.joining(",")));

        var isSfItem = SlimefunItem.getByItem(item) != null;
        var wrapper = ItemStackWrapper.wrap(item);
        var remain = item.getAmount();

        for (int slot : slots) {
            // A small optimization for empty slots
            var slotItem = getItemInSlot(slot);
            if (slotItem == null || slotItem.getType().isAir()) {
                return true;
            }

            Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - Now checking item | Slot {} | Item {}", slot, StringUtil.itemStackToString(slotItem));

            if (isSfItem) {
                Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - Check slimefun item fits");

                if (!slotItem.hasItemMeta() || item.getType() != slotItem.getType() || !SlimefunUtils.isItemSimilar(slotItem, wrapper, true, false)) {
                    continue;
                }

                var slotRemain = slotItem.getMaxStackSize() - slotItem.getAmount();

                Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - current slot remain: {}", slotRemain);

                remain -= slotRemain;

                Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - remaining amount: {}", remain);

                if (remain <= 0) {
                    Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - check fits result (no remain): false");
                    return true;
                }
            }
        }

        boolean result = false;

        if (!isSfItem) {
            result = InvUtils.fits(toInventory(), wrapper, slots);
        }

        Debug.log(TestCase.UTILS, "DirtyChestMenu#fits - check fits result: {}", result);

        return result;
    }

    /**
     * Adds given {@link ItemStack} to any of the given inventory slots.
     * Items will be added to the inventory slots based on their order in the function argument.
     * Items will be added either to any empty inventory slots or any partially filled slots, in which case
     * as many items as can fit will be added to that specific spot.
     *
     * @param item  {@link ItemStack} to be added to the inventory
     * @param slots Numbers of slots to add the {@link ItemStack} to
     * @return {@link ItemStack} with any items that did not fit into the inventory
     * or null when everything had fit
     */
    @Nullable
    public ItemStack pushItem(ItemStack item, int... slots) {
        if (item == null || item.getType() == Material.AIR) {
            throw new IllegalArgumentException("Cannot push null or AIR");
        }

        ItemStackWrapper wrapper = null;
        int amount = item.getAmount();

        for (int slot : slots) {
            if (amount <= 0) {
                break;
            }

            ItemStack stack = getItemInSlot(slot);

            if (stack == null) {
                replaceExistingItem(slot, item);
                return null;
            } else {
                int maxStackSize = Math.min(stack.getMaxStackSize(), toInventory().getMaxStackSize());
                if (stack.getAmount() < maxStackSize) {
                    if (wrapper == null) {
                        wrapper = ItemStackWrapper.wrap(item);
                    }

                    if (SlimefunItem.getByItem(item) != null) {
                        // Patch: use sf item check
                        if (!SlimefunUtils.isItemSimilar(stack, wrapper, true, false)) {
                            continue;
                        }
                    } else {
                        // Use original check
                        if (!ItemUtils.canStack(wrapper, stack)) {
                            continue;
                        }
                    }

                    amount -= (maxStackSize - stack.getAmount());
                    stack.setAmount(Math.min(stack.getAmount() + item.getAmount(), maxStackSize));
                    item.setAmount(amount);
                }
            }
        }

        if (amount > 0) {
            return new CustomItemStack(item, amount);
        } else {
            return null;
        }
    }

    public void consumeItem(int slot) {
        consumeItem(slot, 1);
    }

    public void consumeItem(int slot, int amount) {
        consumeItem(slot, amount, false);
    }

    public void consumeItem(int slot, int amount, boolean replaceConsumables) {
        ItemUtils.consumeItem(getItemInSlot(slot), amount, replaceConsumables);
        markDirty();
    }

    @Override
    public void replaceExistingItem(int slot, ItemStack item) {
        replaceExistingItem(slot, item, true);
    }

    public void replaceExistingItem(int slot, ItemStack item, boolean event) {
        if (event) {
            ItemStack previous = getItemInSlot(slot);
            item = preset.onItemStackChange(this, slot, previous, item);
        }

        super.replaceExistingItem(slot, item);
        markDirty();
    }

}
