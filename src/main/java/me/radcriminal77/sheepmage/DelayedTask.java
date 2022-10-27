package me.radcriminal77.sheepmage;

import org.bukkit.Bukkit;

public class DelayedTask {

    private static SheepMage plugin;
    private int id = -1;

    public static void setDelayedTaskPluginInstance(SheepMage instance) {
        plugin = instance;
    }

    public DelayedTask(Runnable runnable) {
        this(runnable, 0);
    }

    public DelayedTask(Runnable runnable, long delay) {

        if (plugin.isEnabled()) {
            id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
        } else {
            runnable.run();
        }

    }

    public int getId() {
        return id;
    }

}
