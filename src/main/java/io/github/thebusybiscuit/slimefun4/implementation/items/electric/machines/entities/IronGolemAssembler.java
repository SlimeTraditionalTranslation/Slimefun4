package io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.entities;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.IronGolem;
import org.bukkit.inventory.ItemStack;

import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;

/**
 * The {@link IronGolemAssembler} is an electrical machine that can automatically spawn
 * a {@link IronGolem} if the required ingredients have been provided.
 * 
 * @author TheBusyBiscuit
 * 
 * @see WitherAssembler
 *
 */
public class IronGolemAssembler extends AbstractEntityAssembler<IronGolem> {

    @ParametersAreNonnullByDefault
    public IronGolemAssembler(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int getCapacity() {
        return 4096;
    }

    @Override
    public int getEnergyConsumption() {
        return 2048;
    }

    @Override
    public ItemStack getHead() {
        return new ItemStack(Material.CARVED_PUMPKIN);
    }

    @Override
    public Material getHeadBorder() {
        return Material.ORANGE_STAINED_GLASS_PANE;
    }

    @Override
    public ItemStack getBody() {
        return new ItemStack(Material.IRON_BLOCK, 4);
    }

    @Override
    public Material getBodyBorder() {
        return Material.WHITE_STAINED_GLASS_PANE;
    }

    @Override
    protected void constructMenu(BlockMenuPreset preset) {
        preset.addItem(1, new CustomItemStack(getHead(), "&7雕刻過的南瓜槽", "", "&f可以放入「雕刻過的南瓜」"), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(7, new CustomItemStack(getBody(), "&7鐵方塊槽", "", "&f可以放入「鐵方塊」"), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(13, new CustomItemStack(Material.CLOCK, "&7冷卻時間：&b30 秒", "", "&f這台機器需要半分鐘來暖機", "&f所以請給它點時間吧！"), ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public IronGolem spawnEntity(Location l) {
        SoundEffect.IRON_GOLEM_ASSEMBLER_ASSEMBLE_SOUND.playAt(l, SoundCategory.BLOCKS);
        return l.getWorld().spawn(l, IronGolem.class);
    }
}
