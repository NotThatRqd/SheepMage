package me.radcriminal77.sheepmage.listeners;

import me.radcriminal77.sheepmage.Cooldown;
import me.radcriminal77.sheepmage.DelayedTask;
import me.radcriminal77.sheepmage.SheepMage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static me.radcriminal77.sheepmage.SheepMage.getEconomy;
import static me.radcriminal77.sheepmage.SheepMage.getSheepWand;

public class RightClickListener implements Listener {

    private final SheepMage plugin;

    private final Random RANDOM = new Random();
    private final int SIZE = DyeColor.values().length;

    public RightClickListener(SheepMage plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info(this.getClass().getSimpleName() + " registered");
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {

        final Action action = e.getAction();

        // return if the action wasn't a right click
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        final Player p = e.getPlayer();
        final ItemStack mainHand = p.getInventory().getItemInMainHand();
        final ItemStack offHand = p.getInventory().getItemInOffHand();

        final ItemStack wand = getSheepWand(mainHand, offHand);

        // return if the player isn't holding the sheep wand
        if (wand == null) return;

        e.setCancelled(true);

        // open menu
        if (p.isSneaking()) {
            wandMenu(e, p, wand);
            return;
        }

        AtomicReference<Location> loc = new AtomicReference<>(p.getLocation());


        p.playSound(loc.get(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GRAY + "" + ChatColor.ITALIC +
                "You call upon an ancient force..."));

        final byte scaleX = 2;  // use these to tune the size of your circle
        final byte scaleZ = 2;
        final float density = 0.1f;  // smaller numbers make the particles denser

        // loop through -0.5, 0, and 0.5
        for (double i2 = -0.5; i2 <= 0.5; i2 += 0.5) {
            // loop through a circle and spawn a particle at each point
            for (double i = 0; i < 2 * Math.PI; i += density) {
                double x = Math.cos(i) * scaleX;
                double z = Math.sin(i) * scaleZ;

                Location l = p.getEyeLocation().add(x, i2, z);

                // spawn your particle here
                Particle.DustTransition dustTransition = new Particle.DustTransition(Color.AQUA, Color.LIME, 1.0f);
                p.spawnParticle(Particle.DUST_COLOR_TRANSITION, l, 10, dustTransition);
            }
        }

        ItemMeta meta = wand.getItemMeta();
        assert meta != null; // if it didn't have meta it would not have passed the isSheepWand check
        final PersistentDataContainer data = meta.getPersistentDataContainer();

        String cooldownString = data.get(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING);

        // if the wand doesn't have the cooldown key add it and defualt to medium
        if (cooldownString == null) {
            data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.STRING, Cooldown.MEDIUM.name());
            mainHand.setItemMeta(meta);
            cooldownString = Cooldown.MEDIUM.name();
            p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "No cooldown was on this wand, defaulting to medium.");
        }

        float cooldown;

        try {
            cooldown = switch (Cooldown.valueOf(cooldownString)) {
                case FAST -> 0.0f;
                case MEDIUM -> 1.5f;
                case SLOW -> 3.0f;
            };
        } catch (IllegalArgumentException ex) {
            p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This wand did not have a valid cooldown. This wand might be from an old version. Consider getting a new one using /sheepwand.");
            return;
        }

        if (cooldown == 0.0f) {
            flingPlayer(p);
            return;
        }

        new DelayedTask(() -> {
            loc.set(p.getLocation());

            p.playSound(loc.get(), Sound.ITEM_BOTTLE_EMPTY, 1.0f, 1.4f);
        }, (long) ((cooldown / 3) * 20)); // one third of the cooldown

        new DelayedTask(() -> {
            loc.set(p.getLocation());

            p.playSound(loc.get(), Sound.ITEM_BOTTLE_EMPTY, 1.0f, 1.8f);
        }, (long) ((cooldown / 3 * 2) * 20)); // 2 thirds of the cooldown

        new DelayedTask(() -> flingPlayer(p), (long) (cooldown * 20)); // the cooldown
    }

    private void flingPlayer(Player p) {

        final Location loc = p.getLocation();

        p.playSound(loc, Sound.ITEM_BOTTLE_EMPTY, 1.0f, 2.0f);

        final Sheep sheep = (Sheep) p.getWorld().spawnEntity(loc, EntityType.SHEEP);
        sheep.setInvulnerable(true);
        sheep.setColor(DyeColor.values()[RANDOM.nextInt(SIZE)]);

        new DelayedTask(sheep::remove, (long) (1.2 * 20));

        Vector vector;

        // if block under player is air
        if (loc.subtract(0.0, 1.0, 0.0).getBlock().isEmpty()) {
            vector = loc.getDirection().multiply(1.5f);
        } else {
            vector = new Vector(0.0f, 1.5f, 0.0f);
        }
        p.setVelocity(vector);

        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, true, false, false));

    }


    private void wandMenu(PlayerInteractEvent e, Player p, ItemStack wand) {

        Inventory gui = Bukkit.createInventory(p, 9, "Sheepwand Right-Click Menu");

        final Economy economy = getEconomy();

        assert wand.getItemMeta() != null; // it wouldn't be a wand if it was null
        final PersistentDataContainer data = wand.getItemMeta().getPersistentDataContainer();

        final ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "You have: " + economy.format(economy.getBalance(p)));


        final ItemStack slow = new ItemStack(Material.RED_WOOL);
        final ItemMeta slowMeta = slow.getItemMeta();
        slowMeta.setDisplayName(ChatColor.RED + "Toggle Slow");
        slowMeta.setLore(lore);
        slow.setItemMeta(slowMeta);


        final ItemStack medium = new ItemStack(Material.YELLOW_WOOL);
        final ItemMeta mediumMeta = medium.getItemMeta();

        final Byte hasMediumCooldown = data.get(new NamespacedKey(plugin, "has" + Cooldown.MEDIUM.name() + "Cooldown"), PersistentDataType.BYTE);
        if (hasMediumCooldown != null && hasMediumCooldown == 1) mediumMeta.setDisplayName(ChatColor.YELLOW + "Toggle Medium");
        else mediumMeta.setDisplayName(ChatColor.YELLOW + "Buy Medium: 10 " + economy.currencyNamePlural());

        mediumMeta.setLore(lore);
        medium.setItemMeta(mediumMeta);


        final ItemStack fast = new ItemStack(Material.GREEN_WOOL);
        final ItemMeta fastMeta = fast.getItemMeta();

        final Byte hasFastCooldown = data.get(new NamespacedKey(plugin, "has" + Cooldown.FAST.name() + "Cooldown"), PersistentDataType.BYTE);
        if (hasFastCooldown != null && hasFastCooldown == 1) fastMeta.setDisplayName(ChatColor.GREEN + "Toggle Fast");
        else fastMeta.setDisplayName(ChatColor.GREEN + "Buy Fast: 20 " + economy.currencyNamePlural());

        fastMeta.setLore(lore);
        fast.setItemMeta(fastMeta);

        gui.setItem(3, slow);
        gui.setItem(4, medium);
        gui.setItem(5, fast);
        p.openInventory(gui);

    }

}
