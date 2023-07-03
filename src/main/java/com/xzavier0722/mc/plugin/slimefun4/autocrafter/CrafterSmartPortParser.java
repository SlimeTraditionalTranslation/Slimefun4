package com.xzavier0722.mc.plugin.slimefun4.autocrafter;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.implementation.items.autocrafters.AbstractAutoCrafter;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public class CrafterSmartPortParser implements CrafterInteractable{

    BlockMenu inv;

    public CrafterSmartPortParser(BlockMenu inv) {
        this.inv = inv;
    }

    @Override
    public boolean canOutput(ItemStack item) {
        ItemStackWrapper wrapper = ItemStackWrapper.wrap(item);

        int amountLeft = wrapper.getAmount();
        for (int slot : CrafterSmartPort.OUTPUT_SLOTS) {
            ItemStack itemInSlot = inv.getItemInSlot(slot);
            if (itemInSlot == null) {
                return true;
            }
            if (SlimefunUtils.isItemSimilar(itemInSlot, wrapper, true, false)) {
                int slotItemAmount = itemInSlot.getAmount();
                int maxAmount = itemInSlot.getMaxStackSize();
                if (slotItemAmount + amountLeft <= maxAmount) {
                    return true;
                }
                amountLeft -= maxAmount - slotItemAmount;
            }
        }
        return false;
    }

    @Override
    public boolean matchRecipe(AbstractAutoCrafter crafter, Collection<Predicate<ItemStack>> recipe, Map<Integer, Integer> itemQuantities) {
        for (Predicate<ItemStack> predicate : recipe) {
            // Check if any Item matches the Predicate
            if (!crafter.matchesAny(inv.toInventory(), itemQuantities, predicate)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getItemInSlot(slot);
    }

    @Override
    public boolean addItem(ItemStack item) {
        return inv.pushItem(item, CrafterSmartPort.OUTPUT_SLOTS) == null;
    }

    @Override
    public void setIngredientCount(Block b, int count) {
        count = Math.max(1, count);
        StorageCacheUtils.setData(b.getLocation(), "ingredientCount", String.valueOf(count));
        inv.getItemInSlot(6).setAmount(count);
    }
}
