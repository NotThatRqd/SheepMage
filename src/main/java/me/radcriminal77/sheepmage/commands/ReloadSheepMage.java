package me.radcriminal77.sheepmage.commands;

import me.radcriminal77.sheepmage.SheepMage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadSheepMage implements CommandExecutor {

    private final SheepMage plugin;

    public ReloadSheepMage(SheepMage plugin) {
        this.plugin = plugin;
        plugin.getCommand("ReloadSheepMage").setExecutor(this);
        plugin.getLogger().info(this.getClass().getSimpleName() + " registered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        plugin.reloadConfig();

        sender.sendMessage(ChatColor.GREEN + "SheepMage config reloaded.");

        return true;
    }

}
