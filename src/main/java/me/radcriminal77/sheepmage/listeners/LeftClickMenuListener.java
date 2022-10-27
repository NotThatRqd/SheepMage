package me.radcriminal77.sheepmage.listeners;

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

public class LeftClickMenuListener implements Listener {

    private final SheepMage plugin;

    public LeftClickMenuListener(SheepMage plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info(this.getClass().getSimpleName() + " registered");
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {

        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().equals("Sheepwand Left-Click Menu")) return;

        e.setCancelled(true);

        final Player p = (Player) e.getWhoClicked();
        final Material item = e.getCurrentItem().getType();

        final ItemStack mainHand = p.getInventory().getItemInMainHand();
        final ItemStack offHand = p.getInventory().getItemInOffHand();

        final ItemStack wand = getSheepWand(mainHand, offHand);
        if (wand == null) return; // this couldn't be true unless the player uses a glitch or hack

        ItemMeta meta = wand.getItemMeta();
        assert meta != null; // if the meta was null the isSheepWand check would have been false

        final PersistentDataContainer data = meta.getPersistentDataContainer();

        final Byte hasExplodeBlocks = data.get(new NamespacedKey(plugin, "hasExplodeBlocks"), PersistentDataType.BYTE);

        if (hasExplodeBlocks == null) {
            p.sendMessage("this wand did not have has explode blocks. it's probably a wand from an older version. just get a new wand");
            return;
        }

        switch (item) {
            case TNT -> {
                if (hasExplodeBlocks == 1) {
                    data.set(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE, (byte) 1);
                } else {

                    final EconomyResponse response = getEconomy().withdrawPlayer(p, 10);

                    if (response.transactionSuccess()) {
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "Bought ability to explode blocks");
                        data.set(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE, (byte) 1);
                        data.set(new NamespacedKey(plugin, "hasExplodeBlocks"), PersistentDataType.BYTE, (byte) 1);
                    } else {
                        p.sendMessage(response.errorMessage);
                    }

                }
            }
            case BARRIER -> data.set(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE, (byte) 0);
            default -> {
                return;
            }
        }

        meta = updateWandLore(meta);
        wand.setItemMeta(meta);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        Bukkit.getScheduler().runTask(plugin, p::closeInventory);

    }

}
