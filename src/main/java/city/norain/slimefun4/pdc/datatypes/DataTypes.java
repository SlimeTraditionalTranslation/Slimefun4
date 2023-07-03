package city.norain.slimefun4.pdc.datatypes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DataTypes {
    public static PersistentDataType<byte[], ItemStack> ITEMSTACK = new CustomPersistentDataType<>(ItemStack.class);
}
