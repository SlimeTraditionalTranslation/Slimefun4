package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import io.github.bakedlibs.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

class TransformCommand extends SubCommand {
    TransformCommand(Slimefun plugin, SlimefunCommand cmd) {
        super(plugin, cmd, "transform", false);
    }

    private final Set<Player> noticedPlayer = new HashSet<>();

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 1 && args[1].equalsIgnoreCase("update")) {
                if (!noticedPlayer.contains(p)) {
                    Slimefun.getLocalization().sendMessage(sender, "messages.transform.warning", true);
                    noticedPlayer.add(p);
                    return;
                }

                Slimefun.getLocalization().sendMessage(sender, "messages.transform.in-progress", true);

                try {
                    p.getInventory().forEach(is -> {
                        if (is != null && is.getType() != Material.AIR) {
                            ItemMeta meta = is.getItemMeta();
                            if (meta != null) {
                                List<String> lore = meta.getLore();
                                if (lore != null && !lore.isEmpty()) {
                                    for (int i = 0; i < lore.size(); i++) {
                                        String oldColor = ChatColors.color("&r");
                                        String newColor = ChatColors.color("&f");
                                        if (lore.get(i).contains(oldColor)) {
                                            lore.set(i, lore.get(i).replace(oldColor, newColor));
                                        }
                                    }
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    Slimefun.logger().log(Level.WARNING, "在轉換物品時發生了錯誤", e);
                }

                noticedPlayer.remove(p);
                Slimefun.getLocalization().sendMessage(sender, "messages.transform.success", true);
            } else {
                ItemStack item = p.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (meta.getDisplayName().contains(ChatColors.color("&b已修復的生怪磚"))) {
                        ItemStack transform = item.clone();
                        List<String> lore = meta.getLore();
                        if (lore != null) {
                            for (int i = 0; i < lore.size(); i++) {
                                if (lore.get(i).contains("類型")) {
                                    lore.set(i, lore.get(i).replace("類型", "Type"));
                                }
                            }
                        }

                        meta.setLore(lore);
                        meta.setDisplayName(ChatColors.color("&bReinforced Spawner"));
                        transform.setItemMeta(meta);
                        p.getInventory().setItemInMainHand(transform);
                        Slimefun.getLocalization().sendMessage(sender, "messages.transform.success", true);
                    } else if (meta.getDisplayName().contains(ChatColors.color("&bReinforced Spawner"))) {
                        ItemStack transform = item.clone();
                        ItemMeta im = item.getItemMeta().clone();
                        List<String> lore = im.getLore();

                        if (lore != null) {
                            for (int i = 0; i < lore.size(); i++) {
                                if (lore.get(i).contains("Type")) {
                                    lore.set(i, lore.get(i).replace("Type", "類型"));
                                }
                            }
                        }

                        im.setLore(lore);
                        im.setDisplayName(ChatColors.color("&b已修復的生怪磚"));
                        transform.setItemMeta(im);
                        p.getInventory().setItemInMainHand(transform);
                        Slimefun.getLocalization().sendMessage(sender, "messages.transform.success", true);
                    } else {
                        Slimefun.getLocalization().sendMessage(sender, "messages.transform.wrong-type", true);
                    }
                }
            }
        } else {
            Slimefun.getLocalization().sendMessage(sender, "messages.only-players", true);
        }
    }
}
