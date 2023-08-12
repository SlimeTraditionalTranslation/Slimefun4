package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * The debug command will allow server owners to get information for us developers.
 * We can put debug messages in the code and they can trigger it for us to see what exactly is going on.
 *
 * @author WalshyDev
 */
public class DebugCommand extends SubCommand {

    protected DebugCommand(@Nonnull Slimefun plugin, @Nonnull SlimefunCommand cmd) {
        super(plugin, cmd, "debug", true);
    }

    @Override
    protected @Nonnull String getDescription() {
        return "commands.debug.description";
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!sender.hasPermission("slimefun.command.debug")) {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
            return;
        }

        if (args.length == 1) {
            String currentCase = String.join(", ", Debug.getTestCase());
            if (!currentCase.isEmpty()) {
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.current", true, msg -> msg.replace("%test_case%", currentCase));
            } else {
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.none-running", true);
            }
            return;
        }

        String test = args[1];

        switch (test.toLowerCase()) {
            case "disable", "off" -> {
                Debug.disableTestCase();
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.disabled");
            }
            default -> {
                Debug.addTestCase(test);
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.running", msg -> msg.replace("%test%", test));
            }
        }
    }
}
