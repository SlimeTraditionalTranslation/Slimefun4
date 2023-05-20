package city.norain.slimefun4.utils;

import io.papermc.lib.PaperLib;
import javax.annotation.Nonnull;
import org.bukkit.plugin.Plugin;

/**
 * LangUtil
 * <p>
 * 将部分无法直接汉化的方法提取出来
 *
 * @author StarWishsama
 */
public class LangUtil {
    /**
     * 推荐你使用 Paper 服务端
     *
     * @param plugin
     */
    public static void suggestPaper(@Nonnull Plugin plugin) {
        if (PaperLib.isPaper()) {
            return;
        }
        final var benefitsProperty = "paperlib.shown-benefits";
        final var pluginName = plugin.getDescription().getName();
        final var logger = plugin.getLogger();
        logger.warning("====================================================");
        logger.warning(" " + pluginName + " 在 Paper 上會運作得更好");
        logger.warning(" 推薦你使用 Paper 運行" + pluginName + " ");
        if (System.getProperty(benefitsProperty) == null) {
            System.setProperty(benefitsProperty, "1");
            logger.warning("  ");
            logger.warning(" Paper 能提供顯著效能優化，且更安全");
            logger.warning(" 以及 Bug 修復和部分新功能");
            logger.warning(" 提升服主的伺服器體驗。");
            logger.warning("  ");
            logger.warning(" Paper 內建了 Timings v2。相比 v1 版本");
            logger.warning(" 能夠更顯著地診斷伺服器卡頓原因。");
            logger.warning("  ");
            logger.warning(" 你原有的插件在更換後大部分都能正常使用。");
            logger.warning(" 如果遇到問題，Paper 社群很樂意幫助你解決問題。");
            logger.warning("  ");
            logger.warning(" 加入 Paper 社群 @ https://papermc.io");
        }
        logger.warning("====================================================");
    }
}
