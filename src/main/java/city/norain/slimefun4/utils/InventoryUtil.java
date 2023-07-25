package city.norain.slimefun4.utils;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import java.util.LinkedList;

public class InventoryUtil {
    /**
     * Close inventory for all viewers.
     *
     * @param inventory {@link Inventory}
     */
    public static void closeInventory(Inventory inventory) {
        if (inventory != null) {
            new LinkedList<>(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        }
    }
}
