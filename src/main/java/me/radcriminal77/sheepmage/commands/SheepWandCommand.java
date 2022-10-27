package me.radcriminal77.sheepmage.commands;

import me.radcriminal77.sheepmage.Cooldown;
import me.radcriminal77.sheepmage.SheepMage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static me.radcriminal77.sheepmage.UpdateWandLore.updateWandLore;

public class SheepWandCommand implements CommandExecutor {

    private final SheepMage plugin;

    public SheepWandCommand(SheepMage plugin) {
        this.plugin = plugin;
        plugin.getCommand("SheepWand").setExecutor(this);
        plugin.getLogger().info(this.getClass().getSimpleName() + " registered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        final Player toGive;

        if (!(sender instanceof Player)) { // sender is not player
            if (args.length == 0) {
                sender.sendMessage("No args.");
                return true;
            }

            toGive = Bukkit.getPlayerExact(args[0]);
            if (toGive == null) {
                sender.sendMessage("Invalid player.");
                return true;
            }
        } else { // sender is player
            if (args.length == 0) {
                toGive = (Player) sender;
            } else {
                toGive = Bukkit.getPlayerExact(args[0]);
                if (toGive == null) {
                    sender.sendMessage("Invalid player.");
                    return true;
                }
            }
        }

        final ItemStack sheepWand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta itemMeta = sheepWand.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Sheep Wand");

        final PersistentDataContainer data = itemMeta.getPersistentDataContainer();

        data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING, Cooldown.SLOW.name());

        data.set(new NamespacedKey(plugin, "hasSlowCooldown"), PersistentDataType.BYTE, (byte) 1);
        data.set(new NamespacedKey(plugin, "hasMediumCooldown"), PersistentDataType.BYTE, (byte) 0);
        data.set(new NamespacedKey(plugin, "hasFastCooldown"), PersistentDataType.BYTE, (byte) 0);

        data.set(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE, (byte) 0);
        data.set(new NamespacedKey(plugin, "hasExplodeBlocks"), PersistentDataType.BYTE, (byte) 0);

        itemMeta = updateWandLore(itemMeta);

        sheepWand.setItemMeta(itemMeta);

        toGive.getInventory().addItem(sheepWand);

        return true;
    }

}
