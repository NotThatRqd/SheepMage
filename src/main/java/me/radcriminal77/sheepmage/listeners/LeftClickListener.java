package me.radcriminal77.sheepmage.listeners;

import me.radcriminal77.sheepmage.DelayedTask;
import me.radcriminal77.sheepmage.SheepMage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
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
import static me.radcriminal77.sheepmage.UpdateWandLore.updateWandLore;

public class LeftClickListener implements Listener {
    private final SheepMage plugin;

    private final Random RANDOM = new Random();
    private final int SIZE = DyeColor.values().length;

    public LeftClickListener(SheepMage plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info(this.getClass().getSimpleName() + " registered");
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent e) {

        final Action action = e.getAction();

        // return if action wasn't left-click
        if (!action.equals(Action.LEFT_CLICK_BLOCK) && !action.equals(Action.LEFT_CLICK_AIR)) return;

        final Player p = e.getPlayer();
        final ItemStack mainHand = p.getInventory().getItemInMainHand();
        final ItemStack offHand = p.getInventory().getItemInOffHand();

        final ItemStack wand = getSheepWand(mainHand, offHand);

        // return if left-click wasn't with a wand
        if (wand == null) return;

        e.setCancelled(true);

        // open gui if sneaking
        if (p.isSneaking()) {
            wandMenu(e, p, wand);
            return;
        }


        final ItemMeta meta = wand.getItemMeta();
        assert meta != null; // if the meta was null the isSheepWand check would have been false
        final PersistentDataContainer data = meta.getPersistentDataContainer();


        final double density = 0.1;  // smaller numbers make the particles denser

        // loop through a circle and spawn a particle at each point
        for (double i = 0; i < 2 * Math.PI; i += density) {
            final double x = Math.cos(i);
            final double z = Math.sin(i);

            Location l = p.getLocation().add(x, 1, z);

            // spawn your particle here
            Particle.DustTransition dustTransition = new Particle.DustTransition(Color.AQUA, Color.WHITE, 1.0f);
            p.spawnParticle(Particle.DUST_COLOR_TRANSITION, l, 10, dustTransition);
        }

        final Sheep sheep = (Sheep) p.getWorld().spawnEntity(p.getLocation(), EntityType.SHEEP);
        sheep.setInvulnerable(true);
        sheep.setColor(DyeColor.values()[RANDOM.nextInt(SIZE)]);
        sheep.setVelocity(p.getLocation().getDirection().multiply(5));

        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.2f);

        // give slowness for 1 second
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1, true, false, false));

        new DelayedTask(() -> {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.2f);
        }, 20/3);

        new DelayedTask(() -> {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.4f);
        }, (20/3)*2);

        new DelayedTask(() -> {

            final Location l = sheep.getLocation();

            createParticleLine(p.getLocation(), l, p);

            Byte canExplodeBlocks = data.get(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE);

            if (canExplodeBlocks == null) {
                canExplodeBlocks = 0;
                data.set(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE, (byte) 0);
                wand.setItemMeta(updateWandLore(meta));
                p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This wand did not have a valid can explode blocks, defaulting to false (0b)");
            }

            p.getWorld().createExplosion(l, 5.0f, false, canExplodeBlocks != 0);
            sheep.remove();
        }, 20L);

    }

    private void wandMenu(PlayerInteractEvent e, Player p, ItemStack wand) {

        Inventory gui = Bukkit.createInventory(p, 9, "Sheepwand Left-Click Menu");

        final Economy economy = getEconomy();

        assert wand.getItemMeta() != null; // it wouldn't be a wand if it was null
        final PersistentDataContainer data = wand.getItemMeta().getPersistentDataContainer();

        final ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "You have: " + economy.format(economy.getBalance(p)));

        final ItemStack toggle = new ItemStack(Material.TNT);
        final ItemMeta toggleMeta = toggle.getItemMeta();

        final Byte canExplodeBlocks = data.get(new NamespacedKey(plugin, "canExplodeBlocks"), PersistentDataType.BYTE);
        final Byte hasExplodeBlocks = data.get(new NamespacedKey(plugin, "hasExplodeBlocks"), PersistentDataType.BYTE);

        if (canExplodeBlocks == null || hasExplodeBlocks == null) {
            p.sendMessage("this wand did not have a can explode blocks. it's probably a wand from an old version. just get a new one");
            return;
        }

        if (canExplodeBlocks == 1) {
            toggle.setType(Material.BARRIER);
            toggleMeta.setDisplayName(ChatColor.RED + "Turn block breaking explosions off");
        } else {
            if (hasExplodeBlocks == 1) {
                toggleMeta.setDisplayName(ChatColor.GREEN + "Turn block breaking explosions on");
            } else {
                toggleMeta.setDisplayName(ChatColor.GREEN + "Buy block breaking explosions: 10 " + economy.currencyNamePlural());
            }
        }

        toggle.setItemMeta(toggleMeta);

        gui.setItem(4, toggle);
        p.openInventory(gui);

    }

    private void createParticleLine(Location fromLoc, Location toLoc, Player p) {

        // Vector from start to end:
        final Vector toTarget = toLoc.toVector().subtract(fromLoc.toVector());
        // Vector between particles:
        final Vector particleStep = toTarget.clone().normalize().multiply(0.3);
        // amount of planned particles:
        final int particleAmount = (int) (toTarget.length() / 0.3);
        // start with the first particle at start location:
        final Location nextParticleLoc = fromLoc.clone();

        // dust options
        final Particle.DustTransition dustTransition = new Particle.DustTransition(Color.WHITE, Color.GRAY, 1.0f);

        // for all planned particles:
        for (int i = 0; i < particleAmount; i++) {
            // send particle:
            p.spawnParticle(Particle.DUST_COLOR_TRANSITION, nextParticleLoc, 1, dustTransition);
            // move next particle location one step further towards the end location:
            nextParticleLoc.add(particleStep);
        }

    }
}
