package me.radcriminal77.sheepmage;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class UpdateWandLore {

    private static SheepMage plugin;

    public static void setUpdateLorePluginInstance(SheepMage instance) {
        plugin = instance;
    }

    public static ItemMeta updateWandLore(ItemMeta meta) {

        ItemMeta newMeta = meta;
        ArrayList<String> lore = new ArrayList<>();

        final PersistentDataContainer data = newMeta.getPersistentDataContainer();

        Byte canExplodeBlocks = data.get(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE);
        if (canExplodeBlocks == null || canExplodeBlocks == 0) {
            lore.add(ChatColor.GRAY + "Cannot Explode Blocks");
        } else {
            lore.add(ChatColor.GRAY + "Can Explode Blocks");
        }

        Cooldown cooldown;
        try {
            cooldown = Cooldown.valueOf(data.get(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING));
        } catch (IllegalArgumentException ex) {
            cooldown = Cooldown.MEDIUM;
        }

        switch (cooldown) {
            case FAST -> lore.add(ChatColor.GRAY + "Fast cooldown");
            case MEDIUM -> lore.add(ChatColor.GRAY + "Medium cooldown");
            case SLOW -> lore.add(ChatColor.GRAY + "Slow cooldown");
        }

        newMeta.setLore(lore);

        return newMeta;

    }
}
