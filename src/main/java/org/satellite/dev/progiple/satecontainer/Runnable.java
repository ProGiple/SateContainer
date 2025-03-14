package org.satellite.dev.progiple.satecontainer;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.event.ContainerEvent;

import java.time.LocalTime;

public class Runnable extends BukkitRunnable {
    @Override
    public void run() {
        LocalTime now = LocalTime.now();
        LocalTime next = Config.getNextSpawnTime();
        if (now.getHour() == next.getHour() && now.getMinute() == next.getMinute()) {
            if (ContainerEvent.getEvent() == null) Bukkit.getScheduler().runTask(SateContainer.getPlugin(), ContainerEvent::new);
        }
    }
}
