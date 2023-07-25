package io.github.thebusybiscuit.slimefun4.implementation.items.teleporter;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The {@link PersonalActivationPlate} is a teleporter activation plate
 * to which only the {@link Player} who placed it down has access.
 *
 * @author TheBusyBiscuit
 *
 * @see SharedActivationPlate
 *
 */
public class PersonalActivationPlate extends AbstractTeleporterPlate {

    @ParametersAreNonnullByDefault
    public PersonalActivationPlate(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemHandler(onPlace());
    }

    @Nonnull
    private BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                StorageCacheUtils.setData(e.getBlock().getLocation(), "owner", e.getPlayer().getUniqueId().toString());
            }
        };
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean hasAccess(Player p, Block b) {
        return p.getUniqueId().toString().equals(StorageCacheUtils.getData(b.getLocation(), "owner"));
    }

    @Override
    public boolean loadDataByDefault() {
        return true;
    }
}