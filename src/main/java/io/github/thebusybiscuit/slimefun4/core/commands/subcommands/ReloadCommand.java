package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * {@link ReloadCommand} is to handle reloading of slimefun configs
 *
 * @author StarWishsama
 */
public class ReloadCommand extends SubCommand {
    @ParametersAreNonnullByDefault
    ReloadCommand(Slimefun plugin, SlimefunCommand cmd) {
        super(plugin, cmd, "reload", false);
    }

    @Nonnull
    @Override
    protected String getDescription() {
        return "commands.reload.description";
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (sender.hasPermission("slimefun.command.reload") || sender instanceof ConsoleCommandSender) {
            if (Slimefun.getConfigManager().load(true)) {
                Slimefun.getLocalization().sendMessage(sender, "commands.reload.reload-success", true);
            } else {
                Slimefun.getLocalization().sendMessage(sender, "commands.reload.reload-failed", true);
            }
        } else {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
        }
    }
}
