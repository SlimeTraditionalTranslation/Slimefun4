package city.norain.slimefun4.utils;

import org.bukkit.inventory.ItemStack;

public class StringUtil {
    public static String itemStackToString(ItemStack item) {
        if (item == null) {
            return "null";
        }

        return String.format("ItemStack (type=%s, amount=%d)", item.getType(), item.getAmount());
    }
}
