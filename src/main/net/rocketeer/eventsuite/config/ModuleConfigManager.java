package net.rocketeer.eventsuite.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

public class ModuleConfigManager {
  private volatile FileConfiguration config;
  private volatile List<String> arenaNames;
  private final JavaPlugin plugin;

  public ModuleConfigManager(JavaPlugin plugin) {
    this.config = plugin.getConfig();
    this.plugin = plugin;
  }

  private void loadArenaNames() {
    this.arenaNames = this.config.getStringList("arenas");
    if (arenaNames != null)
      return;
    this.config.set("arenas", new LinkedList<String>());
    this.plugin.saveConfig();
    this.arenaNames = this.config.getStringList("arenas");
  }

  public List<String> arenaNames() {
    return this.arenaNames;
  }

  public void addArenaName(String name) {
    this.arenaNames.add(name);
    this.plugin.saveConfig();
  }

  public void reload() {
    this.config = this.plugin.getConfig();
    this.loadArenaNames();
  }
}
