package net.rocketeer.eventsuite.arena;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.rocketeer.eventsuite.EventSuitePlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaManager {
  private Map<String, Arena> arenas = new HashMap<>();
  private Map<ProtectedRegion, Arena> regionArenaMap = new HashMap<>();
  public ArenaManager(List<Arena> arenas) {
    arenas.forEach(this::register);
  }

  public void register(Arena arena) {
    this.arenas.put(arena.name(), arena);
    for (ProtectedRegion region : arena.regions())
      this.regionArenaMap.put(region, arena);
  }

  public boolean registerNew(Arena arena) {
    WorldGuardPlugin wg = EventSuitePlugin.instance.worldGuard();
    RegionManager manager = wg.getRegionManager(arena.world());
    for (ProtectedRegion region : arena.regions())
      if (manager.getRegion(arena.name()) != null)
        return false;
    arena.regions().forEach(manager::addRegion);
    try {
      manager.save();
    } catch (StorageException e) {
      e.printStackTrace();
      return false;
    }
    EventSuitePlugin.instance.configManager().save(arena);
    this.register(arena);
    return true;
  }

  public Arena find(String name) {
    return this.arenas.get(name);
  }

  public Arena find(ProtectedRegion region) {
    return this.regionArenaMap.get(region);
  }
}
