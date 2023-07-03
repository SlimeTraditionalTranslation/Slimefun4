package io.github.thebusybiscuit.slimefun4.implementation.listeners.crafting;

import city.norain.slimefun4.utils.InventoryUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.LinkedList;
import javax.annotation.Nonnull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This {@link Listener} prevents any {@link SlimefunItem} from being used in a
 * smithing table.
 *
 * @author Sefiraat
 */
public class SmithingTableListener implements SlimefunCraftingListener {

    public SmithingTableListener(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmith(InventoryClickEvent e) {
        if (e.getInventory().getType() == InventoryType.SMITHING && e.getWhoClicked() instanceof Player p) {
            ItemStack materialItem = e.getInventory().getContents()[1];

            // Checks if the item in the Material/Netherite slot is allowed to be used.
            if (isUnallowed(materialItem)) {
                e.setResult(Result.DENY);
                Slimefun.getLocalization().sendMessage(p, "smithing_table.not-working", true);
            }
        }
    }

    // Check
    @EventHandler(ignoreCancelled = true)
    public void onPrepareSmith(PrepareSmithingEvent e) {
        if (!e.getViewers().isEmpty()) {
            var viewers = new LinkedList<>(e.getViewers());
            var materialItem = e.getInventory().getContents()[2];

            // Checks if the item in the Material/Netherite slot is allowed to be used.
            if (isUnallowed(materialItem)) {
                e.setResult(new ItemStack(Material.AIR));
                InventoryUtil.closeInventory(e.getInventory());
                viewers.forEach(p -> Slimefun.getLocalization().sendMessage(p, "smithing_table.not-working", true));
            }
        }
    }
}
