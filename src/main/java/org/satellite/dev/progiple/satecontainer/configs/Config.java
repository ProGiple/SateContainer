package org.satellite.dev.progiple.satecontainer.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.Configuration.IConfig;
import org.novasparkle.lunaspring.Util.Utils;
import org.satellite.dev.progiple.satecontainer.SateContainer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class Config {
    private final IConfig config;
    private List<LocalTime> spawnTimers;
    static {
        config = new IConfig(SateContainer.getPlugin());
        loadTimes();
    }

    public void reload() {
        config.reload(SateContainer.getPlugin());
        loadTimes();
    }

    private void loadTimes() {
        spawnTimers = getList("settings.event.spawn_timers").stream().map(LocalTime::parse).collect(Collectors.toList());
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public double getDouble(String path) {
        return config.self().getDouble(path);
    }

    public ConfigurationSection getSection(String path) {
        return config.getSection(path);
    }

    public List<String> getList(String path) {
        return config.getStringList(path);
    }

    public List<Integer> getIntList(String path) {
        return config.getIntList(path);
    }

    @SuppressWarnings("deprecation")
    public void sendMessage(CommandSender sender, String id, String... replacements) {
        List<String> message = new ArrayList<>(config.getStringList(String.format("messages.%s", id)));
        if (message.isEmpty()) return;
        for (String line : message) {
            byte index = 0;
            for (String replacement : replacements) {
                line = line.replace("{" + index + "}", replacement);
                index++;
            }

            String newLine = Utils.color(line
                    .replace("ACTION_BAR", "")
                    .replace("TITLE", "")
                    .replace("SOUND", ""));
            if (sender instanceof Player &&
                    (line.startsWith("ACTION_BAR") || line.startsWith("SOUND") || line.startsWith("TITLE"))) {
                Player player = (Player) sender;
                if (line.startsWith("ACTION_BAR")) player.sendActionBar(newLine);
                else if (line.startsWith("SOUND")) player.playSound(player.getLocation(), Sound.valueOf(newLine), 1, 1);
                else {
                    String[] split = newLine.split("\\{S}");
                    if (split.length < 2) split = new String[]{split[0], ""};
                    player.sendTitle(split[0], split[1], 15, 20, 15);
                }
            }
            else sender.sendMessage(newLine);
        }
    }

    public LocalTime getNextSpawnTime() {
        LocalTime currentTime = LocalTime.now();
        for (LocalTime time : spawnTimers) {
            if (time.isAfter(currentTime)) return time;
        }
        return spawnTimers.get(0);
    }
}
