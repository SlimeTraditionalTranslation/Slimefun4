package city.norain.slimefun4.utils;

import java.util.Collections;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public class InventoryUtil {
    /**
     * Close inventory for all viewers.
     *
     * @param inventory {@link Inventory}
     */
    public static void closeInventory(Inventory inventory) {
        if (inventory != null) {
            Collections.unmodifiableList(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        }
    }
}
