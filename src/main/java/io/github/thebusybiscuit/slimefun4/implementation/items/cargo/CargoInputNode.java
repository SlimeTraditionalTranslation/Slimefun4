package io.github.thebusybiscuit.slimefun4.implementation.items.cargo;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

public class CargoInputNode extends AbstractFilterNode {

    private static final int[] BORDER = { 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 17, 18, 22, 23, 26, 27, 31, 32, 33, 34, 35, 36, 40, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53 };

    private static final String ROUND_ROBIN_MODE = "round-robin";
    private static final String SMART_FILL_MODE = "smart-fill";

    @ParametersAreNonnullByDefault
    public CargoInputNode(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
    }

    @Override
    protected int[] getBorder() {
        return BORDER;
    }

    @Override
    protected void onPlace(BlockPlaceEvent e) {
        super.onPlace(e);

        var l = e.getBlock().getLocation();
        StorageCacheUtils.setData(l, ROUND_ROBIN_MODE, String.valueOf(false));
        StorageCacheUtils.setData(l, SMART_FILL_MODE, String.valueOf(false));
    }

    @Override
    protected void updateBlockMenu(BlockMenu menu, Block b) {
        super.updateBlockMenu(menu, b);

        var blockData = StorageCacheUtils.getBlock(b.getLocation());
        String roundRobinMode = blockData.getData(ROUND_ROBIN_MODE);
        if (roundRobinMode == null || roundRobinMode.equals(String.valueOf(false))) {
            menu.replaceExistingItem(24, new CustomItemStack(SlimefunUtils.getCustomHead(HeadTexture.ENERGY_REGULATOR.getTexture()), "&7循環模式：&4\u2718", "", "&e> 點擊以啟用循環模式", "&e（物品將在頻道上平均分配）"));
            menu.addMenuClickHandler(24, (p, slot, item, action) -> {
                StorageCacheUtils.setData(b.getLocation(), ROUND_ROBIN_MODE, String.valueOf(true));
                updateBlockMenu(menu, b);
                return false;
            });
        } else {
            menu.replaceExistingItem(24, new CustomItemStack(SlimefunUtils.getCustomHead(HeadTexture.ENERGY_REGULATOR.getTexture()), "&7循環模式：&2\u2714", "", "&e> 點擊以停用循環模式", "&e（物品將在頻道上平均分配）"));
            menu.addMenuClickHandler(24, (p, slot, item, action) -> {
                StorageCacheUtils.setData(b.getLocation(), ROUND_ROBIN_MODE, String.valueOf(false));
                updateBlockMenu(menu, b);
                return false;
            });
        }

            String smartFillNode = blockData.getData(SMART_FILL_MODE);

            // FIXME 需要改进翻译

            if (smartFillNode == null || smartFillNode.equals(String.valueOf(false))) {
                menu.replaceExistingItem(16, new CustomItemStack(Material.WRITABLE_BOOK, "&7「智慧填補」模式：&4\u2718", "", "&e> 點擊以啟用「智慧填補」模式", "", "&f在此模式下，物流節點將嘗試", "&f在物品欄中保留固定的物品數量。", "&f這不是完美的，並且仍然會繼續", "&f填補物流先前所運輸來的欄位。"));
                menu.addMenuClickHandler(16, (p, slot, item, action) -> {
                    StorageCacheUtils.setData(b.getLocation(), SMART_FILL_MODE, String.valueOf(true));
                    updateBlockMenu(menu, b);
                    return false;
                });
            } else {
                menu.replaceExistingItem(16, new CustomItemStack(Material.WRITTEN_BOOK, "&7「智慧填補」模式：&2\u2714", "", "&e> 點擊以停用「智慧填補」模式", "", "&f在此模式下，物流節點將嘗試", "&f在物品欄中保留固定的物品數量。", "&f這不是完美的，並且仍然會繼續", "&f填補物流先前所運輸來的欄位。"));
                menu.addMenuClickHandler(16, (p, slot, item, action) -> {
                    StorageCacheUtils.setData(b.getLocation(), SMART_FILL_MODE, String.valueOf(false));
                    updateBlockMenu(menu, b);
                    return false;
                });
            }
        }
    }
