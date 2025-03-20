package org.satellite.dev.progiple.satecontainer.event;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.novasparkle.lunaspring.Menus.MenuManager;
import org.novasparkle.lunaspring.Util.Utils;
import org.novasparkle.lunaspring.Util.managers.RegionManager;
import org.novasparkle.lunaspring.Util.managers.WorldEditManager;
import org.satellite.dev.progiple.satecontainer.SateContainer;
import org.satellite.dev.progiple.satecontainer.configs.Config;
import org.satellite.dev.progiple.satecontainer.configs.OffsetsConfig;
import org.satellite.dev.progiple.satecontainer.event.menu.CMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ContainerEvent {
    private Container container = null;
    private Location location = null;
    private EditSession editSession = null;
    @Setter private Player menuViewer = null;
    @Setter private BukkitTask menuTask;

    private String regionId;
    private int taskId;
    public ContainerEvent() {
        if (ContainerManager.getEvent() != null) return;
        ConfigurationSection eventSection = Config.getSection("settings.event");

        int regionSize = eventSection.getInt("regionSize");
        World world = Bukkit.getWorld(Objects.requireNonNull(eventSection.getString("world")));

        this.initLocation(eventSection, world, regionSize);
        this.start(eventSection, world, regionSize);
    }

    public void start(ConfigurationSection eventSection, World world, int regionSize) {
        if (this.location != null) {
            Location minLoc = this.location.clone().add(-regionSize, -regionSize, -regionSize);
            Location maxLoc = this.location.clone().add(regionSize, regionSize, regionSize);

            this.regionId = "container-" + Utils.getRKey((byte) 12);
            RegionManager.createRegion(this.regionId, minLoc, maxLoc);
            ProtectedRegion region = RegionManager.getRegion(this.regionId);
            region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
            region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
            region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
            region.setFlag(Flags.BUILD, StateFlag.State.DENY);
            region.setFlag(Flags.PISTONS, StateFlag.State.DENY);
            region.setFlag(Flags.MUSHROOMS, StateFlag.State.DENY);
            region.setFlag(Flags.WITHER_DAMAGE, StateFlag.State.DENY);

            this.pasteSchematic();
            this.container = new Container(this.location,
                    Utils.color(Objects.requireNonNull(eventSection.getString("name"))));

            this.taskId = Bukkit.getScheduler().runTaskLater(SateContainer.getPlugin(),
                    () -> this.end(false), eventSection.getInt("lifeTime") * 20L).getTaskId();

            Bukkit.getOnlinePlayers().forEach(player ->
                    Config.sendMessage(player, "message_on_start_event",
                            String.valueOf(this.location.getBlockX()),
                            String.valueOf(this.location.getBlockY()),
                            String.valueOf(this.location.getBlockZ()),
                            world.getName(),
                            this.container.getName()));
            ContainerManager.setEvent(this);
        }
        else this.end(true);
    }

    private void pasteSchematic() {
        File schemDir = new File(SateContainer.getPlugin().getDataFolder(), "schems");
        if (schemDir.exists() && schemDir.isDirectory()) {
            File[] files = schemDir.listFiles();
            if (files != null) {
                List<File> fileList = Stream.of(files)
                        .filter(file -> file.getName().contains(".schem"))
                        .collect(Collectors.toList());
                if (!fileList.isEmpty()) {
                    int index = Math.min(Utils.getRandom().nextInt(fileList.size()), fileList.size() - 1);
                    File schem = fileList.get(index);

                    ConfigurationSection section = OffsetsConfig.getSection(
                            schem.getName().replace(".schem", ""));
                    if (section != null) {
                        int offsetY = section.getInt("offset_y");
                        this.editSession = WorldEditManager.pasteSchematic(
                                schem, this.location,
                                section.getInt("offset_x"),
                                offsetY,
                                section.getInt("offset_z"),
                                section.getBoolean("ignoreAirBlocks"));
                        this.location.add(0, offsetY, 0);
                    }
                }
            }
        }
    }

    public void initLocation(ConfigurationSection eventSection, World world, int regionSize) {
        int maxX = eventSection.getInt("maxX");
        int maxZ = eventSection.getInt("maxZ");

        List<String> invalidBiomes = eventSection.getStringList("invalid_biomes");

        assert world != null;
        int attempts = Config.getInt("settings.findLocationAttempts");
        while (this.location == null && attempts > 0) {
            Location location = Utils.findRandomLocation(world, maxX, maxZ);
            location.add(0, 1, 0);
            if (!RegionManager.hasRegionsInside(location, regionSize + 1) &&
                    !invalidBiomes.contains(location.getBlock().getBiome().name())) {
                this.location = location;
                break;
            }
            else attempts--;
        }
    }

    public void end(boolean bugged) {
        if (!ContainerManager.getEvent().equals(this)) return;

        if (this.container != null) {
            this.container.delete();
            new ArrayList<>(MenuManager.getActiveInventories().values()).forEach(menu -> {
                if (menu instanceof CMenu) ((CMenu) menu).getPlayer().closeInventory();
            });
        }
        if (this.editSession != null && this.location != null) WorldEditManager.undo(this.editSession, this.location.getWorld());
        if (Bukkit.getScheduler().isQueued(this.taskId) || Bukkit.getScheduler().isCurrentlyRunning(this.taskId))
            Bukkit.getScheduler().cancelTask(this.taskId);
        this.stopMenuScroll();

        String messageId = bugged ? "message_on_bugged_event" : "message_on_stop_event";
        Bukkit.getOnlinePlayers().forEach(player ->
                Config.sendMessage(player, messageId, this.container.getName()));

        RegionManager.removeRegion(this.regionId);
        ContainerManager.setEvent(null);
    }

    public void stopMenuScroll() {
        if (this.menuTask != null) this.menuTask.cancel();
    }
}
