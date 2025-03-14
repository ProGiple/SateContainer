package org.satellite.dev.progiple.satecontainer;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.novasparkle.lunaspring.Events.MenuHandler;
import org.novasparkle.lunaspring.LunaSpring;
import org.novasparkle.lunaspring.Util.Service.realized.NBTService;
import org.novasparkle.lunaspring.Util.Service.realized.RegionService;
import org.novasparkle.lunaspring.Util.Service.realized.WorldEditService;
import org.novasparkle.lunaspring.Util.managers.NBTManager;
import org.novasparkle.lunaspring.Util.managers.RegionManager;
import org.novasparkle.lunaspring.Util.managers.WorldEditManager;
import org.satellite.dev.progiple.satecontainer.event.listeners.BreakBlockHandler;
import org.satellite.dev.progiple.satecontainer.event.listeners.ClickOnBlockHandler;

import java.util.Objects;

public final class SateContainer extends JavaPlugin {
    @Getter private static SateContainer plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        saveResource("schems/offsets.yml", false);
        saveResource("items.yml", false);

        NBTService nbtService = new NBTService();
        LunaSpring.getServiceProvider().register(nbtService);
        NBTManager.init(nbtService);

        RegionService regionService = new RegionService();
        LunaSpring.getServiceProvider().register(regionService);
        RegionManager.init(regionService);

        WorldEditService worldEditService = new WorldEditService();
        LunaSpring.getServiceProvider().register(worldEditService);
        WorldEditManager.init(worldEditService);

        Vault.setupEconomy();

        this.reg(new MenuHandler());
        this.reg(new ClickOnBlockHandler());
        this.reg(new BreakBlockHandler());

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) new Placeholder().register();

        Command command = new Command();
        Objects.requireNonNull(getCommand("satecontainer")).setExecutor(command);
        Objects.requireNonNull(getCommand("satecontainer")).setTabCompleter(command);

        new Runnable().runTaskTimerAsynchronously(plugin, 0, 60 * 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void reg(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}
