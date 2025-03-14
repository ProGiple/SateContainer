package org.satellite.dev.progiple.satecontainer.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.Configuration.Configuration;
import org.satellite.dev.progiple.satecontainer.SateContainer;

import java.io.File;

@UtilityClass
public class ItemsConfig {
    private final Configuration config;
    static {
        config = new Configuration(new File(SateContainer.getPlugin().getDataFolder(), "items.yml"));
    }

    public void reload() {
        config.reload();
    }

    public ConfigurationSection getSection(String path) {
        return config.getSection(path);
    }
}
