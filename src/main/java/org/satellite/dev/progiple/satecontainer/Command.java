package org.satellite.dev.progiple.satecontainer;

import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.configs.ItemsConfig;
import org.satellite.dev.progiple.satecontainer.configs.OffsetsConfig;
import org.satellite.dev.progiple.satecontainer.event.ContainerEvent;
import org.satellite.dev.progiple.satecontainer.event.ContainerManager;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length >= 1) {
            switch (strings[0]) {
                default -> {
                    if (commandSender.hasPermission("satecontainer.admin")) {
                        if (strings[0].equalsIgnoreCase("reload")) {
                            Config.reload();
                            ItemsConfig.reload();
                            OffsetsConfig.reload();
                            Config.sendMessage(commandSender, "reload_plugin");
                        }
                        else if (strings[0].equalsIgnoreCase("start")) {
                            if (ContainerManager.getEvent() != null) Config.sendMessage(commandSender, "is_started_now");
                            else new ContainerEvent();
                        }
                        else if (strings[0].equalsIgnoreCase("stop")) {
                            if (ContainerManager.getEvent() == null) Config.sendMessage(commandSender, "no_running_event");
                            else ContainerManager.end();
                        }
                        else if (strings[0].equalsIgnoreCase("tp")) {
                            if (ContainerManager.getEvent() == null) Config.sendMessage(commandSender, "no_running_event");
                            else if (commandSender instanceof Player) {
                                Player player = (Player) commandSender;
                                player.teleport(ContainerManager.getEvent().getLocation().clone().add(0.5, 1.5, 0.5));
                            }
                        }
                    }
                    else Config.sendMessage(commandSender, "noPermission", "satecontainer.admin");
                }
                case "time", "delay", "timer" -> {
                    if (commandSender.hasPermission("satecontainer.delay")) {
                        LocalTime nextTime = Config.getNextSpawnTime();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                        String string = getString(LocalTime.now(), nextTime);

                        Config.sendMessage(commandSender, "delay", nextTime.format(formatter), string);
                    }
                    else Config.sendMessage(commandSender, "noPermission", "satecontainer.delay");
                }
                case "compass", "check", "coordinates", "coords" -> {
                    if (commandSender.hasPermission("satecontainer.compass")) {
                        ContainerEvent event = ContainerManager.getEvent();
                        if (event == null || event.getLocation() == null) {
                            Config.sendMessage(commandSender, "no_running_event");
                            return true;
                        }

                        Location location = event.getLocation();
                        Config.sendMessage(commandSender, "check_coordinates",
                                event.getContainer().getName(),
                                String.valueOf(location.getBlockX()),
                                String.valueOf(location.getBlockY()),
                                String.valueOf(location.getBlockZ()),
                                location.getWorld().getName());
                    }
                    else Config.sendMessage(commandSender, "noPermission", "satecontainer.compass");
                }
            }
        }
        else Config.sendMessage(commandSender, "usage");
        return true;
    }

    private static @NotNull String getString(LocalTime nowTime, LocalTime nextTime) {
        long chrono = nowTime.until(nextTime, ChronoUnit.MINUTES);

        String string = String.format("%s:%s", (int) (chrono / 60), chrono % 60);
        if (chrono < 0) {
            // 20:00 -> 3:30 = 7:30
            int hours = (24 - nowTime.getHour()) + nextTime.getHour();
            int minutes = (60 - nowTime.getMinute()) + nextTime.getMinute();
            string = String.format("%s:%s", hours < 10 ? "0" + hours : hours,
                    minutes < 10 ? "0" + minutes : minutes);
        }
        return string;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return strings.length == 1 ? List.of("reload", "start", "stop", "check", "compass", "coords", "time", "delay", "tp") : List.of();
    }
}
