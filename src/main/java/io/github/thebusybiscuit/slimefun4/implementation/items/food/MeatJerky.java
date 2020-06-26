package io.github.thebusybiscuit.slimefun4.implementation.items.food;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SimpleSlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.ItemConsumptionHandler;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;

/**
 * {@link MeatJerky} is just a piece of meat that gives some extra saturation.
 * {@link MeatJerky} is available for all meat variants.
 * 
 * @author TheBusyBiscuit
 * 
 * @see MonsterJerky
 *
 */
public class MeatJerky extends SimpleSlimefunItem<ItemConsumptionHandler> {

    private final ItemSetting<Integer> saturation = new ItemSetting<>("saturation-level", 6);

    public MeatJerky(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        addItemSetting(saturation);
    }

    @Override
    public ItemConsumptionHandler getItemHandler() {
        return (e, p, item) -> p.setSaturation(p.getSaturation() + saturation.getValue());
    }

}
