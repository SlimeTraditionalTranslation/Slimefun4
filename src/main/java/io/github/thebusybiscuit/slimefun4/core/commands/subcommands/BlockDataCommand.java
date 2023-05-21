package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import net.guizhanss.slimefun4.utils.ChatUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 该指令可直接对 Slimefun 方块数据进行设置。
 *
 * @author ybw0014
 */
class BlockDataCommand extends SubCommand {
    @ParametersAreNonnullByDefault
    BlockDataCommand(Slimefun plugin, SlimefunCommand cmd) {
        super(plugin, cmd, "blockdata", false);
    }

    @Override
    protected String getDescription() {
        return "commands.blockdata.description";
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Slimefun.getLocalization().sendMessage(sender, "messages.only-players", true);
            return;
        }

        if (!sender.hasPermission("slimefun.command.blockdata")) {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
            return;
        }

        if (args.length < 3) {
            Slimefun.getLocalization().sendMessage(sender, "messages.usage", true,
                msg -> msg.replace("%usage%", "/sf blockdata get/set/remove <key> [value]")
            );
            return;
        }

        Block target = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        var blockData = StorageCacheUtils.getBlock(target.getLocation());

        if (target == null || target.getType().isAir() || blockData == null) {
            ChatUtils.sendMessage(player, "&c你需要看向一個 Slimefun 方塊才能執行該指令！");
            return;
        }

        String key = args[2];

        switch (args[1]) {
            case "get" -> {
                String value = blockData.getData(key);
                ChatUtils.sendMessage(player, "&a該方塊 &b%key% &a的值為：&e%value%",
                    msg -> msg.replace("%key%", key).replace("%value%", value)
                );
            }
            case "set" -> {
                if (args.length < 4) {
                    Slimefun.getLocalization().sendMessage(sender, "messages.usage", true,
                        msg -> msg.replace("%usage%", "/sf blockdata set <key> <value>")
                    );
                    return;
                }

                if (key.equalsIgnoreCase("id")) {
                    ChatUtils.sendMessage(player, "&c你不能修改方塊的 ID！");
                    return;
                }

                String value = args[2];

                blockData.setData(key, value);
                ChatUtils.sendMessage(player, "&a已設置該方塊 &b%key% &a的值為：&e%value%",
                    msg -> msg.replace("%key%", key).replace("%value%", value)
                );
            }
            case "remove" -> {
                if (key.equalsIgnoreCase("id")) {
                    ChatUtils.sendMessage(player, "&c你不能修改方塊的 ID！");
                    return;
                }

                blockData.removeData(key);
                ChatUtils.sendMessage(player, "&a已移除該方塊 &b%key% &a的值",
                    msg -> msg.replace("%key%", key)
                );
            }
            default -> {
                Slimefun.getLocalization().sendMessage(sender, "messages.usage", true,
                    msg -> msg.replace("%usage%", "/sf blockdata get/set/remove <key> [value]")
                );
            }
        }

    }
}
