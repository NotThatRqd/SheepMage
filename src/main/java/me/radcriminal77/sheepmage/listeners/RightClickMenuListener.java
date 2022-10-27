package me.radcriminal77.sheepmage.listeners;

import me.radcriminal77.sheepmage.Cooldown;
import me.radcriminal77.sheepmage.SheepMage;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static me.radcriminal77.sheepmage.SheepMage.getEconomy;
import static me.radcriminal77.sheepmage.SheepMage.getSheepWand;
import static me.radcriminal77.sheepmage.UpdateWandLore.updateWandLore;

public class RightClickMenuListener implements Listener {

    private final SheepMage plugin;

    public RightClickMenuListener(SheepMage plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info(this.getClass().getSimpleName() + " registered");
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {

        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().equals("Sheepwand Right-Click Menu")) return;

        e.setCancelled(true);

        final Player p = (Player) e.getWhoClicked();
        final Material item = e.getCurrentItem().getType();

        final ItemStack mainHand = p.getInventory().getItemInMainHand();
        final ItemStack offHand = p.getInventory().getItemInOffHand();

        final ItemStack wand = getSheepWand(mainHand, offHand);
        assert wand != null; // this couldn't be true unless the player uses a glitch or hack

        ItemMeta meta = wand.getItemMeta();
        assert meta != null; // if the meta was null the isSheepWand check would have been false

        final PersistentDataContainer data = meta.getPersistentDataContainer();

//        final Cooldown priorCooldown = Cooldown.valueOf(data.get(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING));

        switch (item) {
            case RED_WOOL -> {
                data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING, Cooldown.SLOW.name());
            }
            case YELLOW_WOOL -> {
                final Byte hasMediumCooldown = data.get(new NamespacedKey(plugin, "hasMediumCooldown"), PersistentDataType.BYTE);
                if (hasMediumCooldown != null && hasMediumCooldown == 1) {
                    data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING, Cooldown.MEDIUM.name());
                } else {
                    buyCooldown(p, 10.0, Cooldown.MEDIUM, data);
                    Bukkit.getScheduler().runTask(plugin, p::closeInventory);
                }
            }
            case GREEN_WOOL -> {
                final Byte hasFastCooldown = data.get(new NamespacedKey(plugin, "hasFastCooldown"), PersistentDataType.BYTE);
                if (hasFastCooldown != null && hasFastCooldown == 1) {
                    data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING, Cooldown.FAST.name());
                } else {
                    buyCooldown(p, 20.0, Cooldown.FAST, data);
                    Bukkit.getScheduler().runTask(plugin, p::closeInventory);
                }
            }
            default -> {
                return;
            }
        }

        meta = updateWandLore(meta);
        wand.setItemMeta(meta);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        Bukkit.getScheduler().runTask(plugin, p::closeInventory);

    }

    private void buyCooldown(Player p, double amount, Cooldown cooldown, PersistentDataContainer data) {

        final EconomyResponse response = getEconomy().withdrawPlayer(p, amount); // attempt to buy for the amount
        if (response.transactionSuccess()) {
            // send player a message so they know it worked
            p.sendMessage(ChatColor.LIGHT_PURPLE + "You bought " + cooldown.name() + " for " + getEconomy().format(amount));

            // set the wand's cooldown to the newly bought one
            data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING, cooldown.name());

            // make it so it remembers you bought it
            data.set(new NamespacedKey(plugin, "has" + cooldown.name() + "Cooldown"), PersistentDataType.BYTE, (byte) 1);
        } else {
            // send the player why it didn't work
            p.sendMessage(response.errorMessage);
        }

    }

}
