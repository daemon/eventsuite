package net.rocketeer.eventsuite.config;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.*;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class ConfigManager {
  private final FileConfiguration config;
  private final List<Arena> arenas = new LinkedList<>();

  public ConfigManager(FileConfiguration config) {
    this.config = config;
    ConfigurationSection cfg = this.config.getConfigurationSection("arenas");
    if (cfg == null)
      return;
    Set<String> arenaNames = cfg.getKeys(false);
    for (String name : arenaNames) {
      ConfigurationSection arenaCfg = cfg.getConfigurationSection(name);
      String worldName = arenaCfg.getString("world");
      List<String> regionNames = arenaCfg.getStringList("regions");
      List<Arena.NamedPoint> points = new LinkedList<>();
      ConfigurationSection pointsCfg = arenaCfg.getConfigurationSection("points");
      for (String key : pointsCfg.getKeys(false)) {
        double x = pointsCfg.getConfigurationSection(key).getDouble("x");
        double y = pointsCfg.getConfigurationSection(key).getDouble("y");
        double z = pointsCfg.getConfigurationSection(key).getDouble("z");
        points.add(new Arena.NamedPoint(key, new Vector(x, y, z)));
      }

      WorldGuardPlugin wg = EventSuitePlugin.instance.worldGuard();
      World world = Bukkit.getWorld(worldName);
      if (world == null)
        continue;
      RegionManager rgMgr = wg.getRegionManager(world);
      if (rgMgr == null)
        continue;
      boolean failure = false;
      List<ProtectedRegion> regions = new LinkedList<>();
      for (String rgName : regionNames) {
        ProtectedRegion region = rgMgr.getRegion(rgName);
        if (region == null) {
          failure = true;
          break;
        }
        regions.add(region);
      }
      if (failure)
        continue;
      ProtectedRegion base = rgMgr.getRegion(name + "_base");
      Arena arena = new Arena(name, world, base);
      arena.addPoints(points);
      arena.addRegions(regions);
      this.arenas.add(arena);
    }
  }

  public void save(Arena arena) {
    if (!this.config.contains("arenas"))
      this.config.createSection("arenas");
    ConfigurationSection cfg = this.config.getConfigurationSection("arenas");
    ConfigurationSection arenaCfg = cfg.createSection(arena.name());
    arenaCfg.set("world", arena.world().getName());
    List<String> regionNames = new LinkedList<>();
    for (ProtectedRegion region : arena.regions())
      regionNames.add(region.getId());
    arenaCfg.set("regions", regionNames);
    ConfigurationSection pointCfg = arenaCfg.createSection("points");
    for (Arena.NamedPoint point : arena.points()) {
      ConfigurationSection section = pointCfg.createSection(point.name());
      section.set("x", point.point().getX());
      section.set("y", point.point().getY());
      section.set("z", point.point().getZ());
    }

    EventSuitePlugin.instance.saveConfig();
  }

  public List<Arena> arenas() {
    return this.arenas;
  }

  public void reload() {
  }
}
