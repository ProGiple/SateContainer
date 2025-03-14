package org.satellite.dev.progiple.satecontainer.event;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.Menus.MenuManager;
import org.novasparkle.lunaspring.Util.Utils;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.event.menu.CMenu;

import java.util.ArrayList;
import java.util.Objects;

public class Container {
    private Block block;
    private final Hologram hologram;
    @Getter private final String name;
    public Container(Location location, String name) {
        Block block = location.getBlock();
        block.setType(Objects.requireNonNull(Material.getMaterial(Config.getString("settings.event.block"))));
        this.block = block;
        this.name = name;
        this.hologram = DHAPI.createHologram(Utils.getRKey((byte) 24),
                location.clone().add(0.5, Config.getDouble("settings.hologramHeight"), 0.5));

        new ArrayList<>(Config.getList("settings.hologram")).forEach(line -> {
                    if (line.startsWith("Material.")) DHAPI.addHologramLine(this.hologram,
                            Objects.requireNonNull(Material.getMaterial(line.replace("Material.", ""))));
                    else DHAPI.addHologramLine(this.hologram, Utils.color(line.replace("{name}", this.name)));
        });
    }

    public void delete() {
        DHAPI.removeHologram(this.hologram.getName());
        if (this.block == null) return;
        this.block.setType(Material.AIR);
        this.block = null;
    }

    public void click(Player player) {
        if (this.block == null) return;

        if (CMenu.getOpened() == null) MenuManager.openInventory(player, new CMenu(player));
        else Config.sendMessage(player, "container_is_opened", CMenu.getOpened().getName());
    }
}
