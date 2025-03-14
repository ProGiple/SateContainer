package org.satellite.dev.progiple.satecontainer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.event.ContainerEvent;
import org.satellite.dev.progiple.satecontainer.event.ContainerManager;

import java.time.LocalTime;

public class Placeholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "satecontainer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ProGiple";
    }

    @Override
    public @NotNull String getVersion() {
        return "latest";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        ContainerEvent event = ContainerManager.getEvent();
        Location location = event == null ? null : event.getLocation();

        if (params.equalsIgnoreCase("x")) {
            return location == null ? null : String.valueOf(location.getBlockX());
        }
        else if (params.equalsIgnoreCase("y")) {
            return location == null ? null : String.valueOf(location.getBlockY());
        }
        else if (params.equalsIgnoreCase("z")) {
            return location == null ? null : String.valueOf(location.getBlockZ());
        }
        else if (params.equalsIgnoreCase("world")) {
            return location == null ? null : location.getWorld().getName();
        }
        else if (params.equalsIgnoreCase("region")) {
            return event == null ? null : event.getRegionId();
        }
        else if (params.equalsIgnoreCase("next")) {
            LocalTime time = Config.getNextSpawnTime();
            return String.format("%s:%s", time.getHour(), time.getMinute());
        }
        else if (params.equalsIgnoreCase("started")) {
            return event == null ? "no" : "yes";
        }
        return null;
    }
}
