package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import io.papermc.lib.PaperLib;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

/**
 * This is our class for the /sf versions subcommand.
 * 
 * @author TheBusyBiscuit
 * @author Walshy
 *
 */
class VersionsCommand extends SubCommand {

    /**
     * This is the Java version we recommend to use.
     * Bump as necessary and adjust the warning.
     */
    private static final int RECOMMENDED_JAVA_VERSION = 16;

    /**
     * This is the notice that will be displayed when an
     * older version of Java is detected.
     */
    private static final String JAVA_VERSION_NOTICE = "從 Minecraft 1.17 開始，將需要使用 Java 16+！";

    @ParametersAreNonnullByDefault
    VersionsCommand(Slimefun plugin, SlimefunCommand cmd) {
        super(plugin, cmd, "versions", false);
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (sender.hasPermission("slimefun.command.versions") || sender instanceof ConsoleCommandSender) {
            /*
             * After all these years... Spigot still displays as "CraftBukkit".
             * so we will just fix this inconsistency for them :)
             */
            String serverSoftware = PaperLib.isSpigot() && !PaperLib.isPaper() ? "Spigot" : Bukkit.getName();
            ComponentBuilder builder = new ComponentBuilder();

            // @formatter:off
            builder.append("此伺服器使用以下 Slimefun 配置：\n")
                    .color(ChatColor.GRAY)
                    .append(serverSoftware)
                    .color(ChatColor.GREEN)
                    .append(" " + Bukkit.getVersion() + '\n')
                    .color(ChatColor.DARK_GREEN)
                    .append("Slimefun ")
                    .color(ChatColor.GREEN)
                    .append(Slimefun.getVersion() + '\n')
                    .color(ChatColor.DARK_GREEN);
            // @formatter:on

            if (Slimefun.getMetricsService().getVersion() != null) {
                // @formatter:off
                builder.append("Metrics-模組 ")
                        .color(ChatColor.GREEN)
                        .append("#" + Slimefun.getMetricsService().getVersion() + '\n')
                        .color(ChatColor.DARK_GREEN);
                // @formatter:on
            }

            addJavaVersion(builder);

            if (Slimefun.getConfigManager().isBackwardsCompatible()) {
                // @formatter:off
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(
                        "向後相容會對效能產生極大的負面影響！\n" +
                                "我們建議你關閉此設定，除非你的伺服器\n" +
                                "仍有在流通舊版的 Slimefun 物品（來自 2019 夏季之前）。"
                )});
                // @formatter:on

                builder.append("\n向後相容已啟用！\n").color(ChatColor.RED).event(hoverEvent);
            }

            // Add notice to warn those smart people
            builder.append("\n由 StarWishsama 漢化，並由 xMikux 進行繁化，並以芒果綠茶的翻譯為基準").color(ChatColor.WHITE)
                    .append(
                            "\n請不要將此版本資訊截圖到官方的 Discord/GitHub 回報 Bug" +
                            "\n請優先到翻譯專案回報" + "\n"
                    ).color(ChatColor.RED);

            builder.append("\n").event((HoverEvent) null);
            addPluginVersions(builder);

            sender.spigot().sendMessage(builder.create());
        } else {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
        }
    }

    private void addJavaVersion(@Nonnull ComponentBuilder builder) {
        int version = NumberUtils.getJavaVersion();

        if (version < RECOMMENDED_JAVA_VERSION) {
            // @formatter:off
            builder.append("Java " + version).color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(
                            "你所使用的 Java 版本已過時！\n!" +
                                    "你應該使用 Java " + RECOMMENDED_JAVA_VERSION + " 或更高的版本。\n" +
                                    JAVA_VERSION_NOTICE
                    )}))
                    .append("\n")
                    .event((HoverEvent) null);
            // @formatter:on
        } else {
            builder.append("Java ").color(ChatColor.GREEN).append(version + "\n").color(ChatColor.DARK_GREEN);
        }
    }

    private void addPluginVersions(@Nonnull ComponentBuilder builder) {
        Collection<Plugin> addons = Slimefun.getInstalledAddons();

        if (addons.isEmpty()) {
            builder.append("未安裝任何附加").color(ChatColor.GRAY).italic(true);
            return;
        }

        builder.append("已安裝的附加：").color(ChatColor.GRAY).append("（" + addons.size() + "）").color(ChatColor.DARK_GRAY);

        for (Plugin plugin : addons) {
            String version = plugin.getDescription().getVersion();

            HoverEvent hoverEvent = null;
            ClickEvent clickEvent = null;
            ChatColor primaryColor;
            ChatColor secondaryColor;

            if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
                primaryColor = ChatColor.GREEN;
                secondaryColor = ChatColor.DARK_GREEN;
                String authors = String.join(", ", plugin.getDescription().getAuthors());

                if (plugin instanceof SlimefunAddon addon && addon.getBugTrackerURL() != null) {
                    // @formatter:off
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(new ComponentBuilder()
                            .append("作者：")
                            .append(authors)
                            .color(ChatColor.YELLOW)
                            .append("\n> 點擊開啟問題追蹤器")
                            .color(ChatColor.GOLD)
                            .create()
                    )});
                    // @formatter:on

                    clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, addon.getBugTrackerURL());
                } else {
                    // @formatter:off
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(new ComponentBuilder()
                            .append("作者：")
                            .append(authors)
                            .color(ChatColor.YELLOW)
                            .create()
                    )});
                    // @formatter:on
                }
            } else {
                primaryColor = ChatColor.RED;
                secondaryColor = ChatColor.DARK_RED;

                if (plugin instanceof SlimefunAddon addon && addon.getBugTrackerURL() != null) {
                    // @formatter:off
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(new ComponentBuilder()
                            .append("此插件已停用。\n檢查控制台是否有錯誤。")
                            .color(ChatColor.RED)
                            .append("\n> 點擊開啟問題追蹤器")
                            .color(ChatColor.DARK_RED)
                            .create()
                    )});
                    // @formatter:on

                    if (addon.getBugTrackerURL() != null) {
                        clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, addon.getBugTrackerURL());
                    }
                } else {
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("插件已停用。檢查控制台是否有錯誤並回報。")});
                }
            }

            // @formatter:off
            // We need to reset the hover event or it's added to all components
            builder.append("\n  " + plugin.getName())
                    .color(primaryColor)
                    .event(hoverEvent)
                    .event(clickEvent)
                    .append(" v" + version)
                    .color(secondaryColor)
                    .append("")
                    .event((ClickEvent) null)
                    .event((HoverEvent) null);
            // @formatter:on
        }
    }
}
