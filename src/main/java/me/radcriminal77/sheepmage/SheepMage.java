package me.radcriminal77.sheepmage;

import me.radcriminal77.sheepmage.commands.ReloadSheepMage;
import me.radcriminal77.sheepmage.commands.SheepWandCommand;
import me.radcriminal77.sheepmage.listeners.LeftClickListener;
import me.radcriminal77.sheepmage.listeners.LeftClickMenuListener;
import me.radcriminal77.sheepmage.listeners.RightClickListener;
import me.radcriminal77.sheepmage.listeners.RightClickMenuListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

import static me.radcriminal77.sheepmage.DelayedTask.setDelayedTaskPluginInstance;
import static me.radcriminal77.sheepmage.UpdateWandLore.setUpdateLorePluginInstance;

public final class SheepMage extends JavaPlugin {

    private static Economy econ = null;

    @Override
    public void onEnable() {

        getConfig().options().copyDefaults(true);
        saveConfig();

        // Get economy plugin
        if (!setupEconomy()) {
            getLogger().severe("Disabling due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setDelayedTaskPluginInstance(this);
        setUpdateLorePluginInstance(this);

        new SheepWandCommand(this);
        new ReloadSheepMage(this);

        new RightClickListener(this);
        new LeftClickListener(this);

        new RightClickMenuListener(this);
        new LeftClickMenuListener(this);

        getLogger().info("SheepMage enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
        getLogger().info("SheepMage disabled!");
    }

    /**
     * @param i The ItemStack to check
     * @return If the ItemStack is a Sheep Wand
     */
    public static boolean isSheepWand(ItemStack i) {
        // only return true if the item is a blaze rod and the name is Sheep Wand in yellow
        return (i.getType().equals(Material.BLAZE_ROD)
                && i.getItemMeta() != null
                && i.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Sheep Wand"));
    }

    /**
     * @return which itemstack is the sheep wand, or null if neither
     */
    @Nullable
    public static ItemStack getSheepWand(ItemStack mainHand, ItemStack offHand) {

        if (isSheepWand(mainHand)) return mainHand;
        else if (isSheepWand(offHand)) return offHand;
        else return null;

    }

}
