package net.rocketeer.eventsuite.api;

import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.command.ModuleBaseCommand;
import net.rocketeer.eventsuite.eventbus.EventBus;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class EventModule extends JavaPlugin {
  private final EventSuitePlugin plugin;
  private ModuleBaseCommand baseCommand;

  public EventModule(EventSuitePlugin plugin) {
    this.plugin = (EventSuitePlugin) Bukkit.getPluginManager().getPlugin("eventsuite");
  }

  public EventSuitePlugin plugin() {
    return this.plugin;
  }

  public EventBus eventBus() {
    return this.plugin.eventBus();
  }

  public ModuleBaseCommand baseCommand() {
    return this.baseCommand;
  }

  public abstract void onCreate();
  public abstract void onDestroy();
  public abstract void onReload();

  public void onEnable() {
    this.saveDefaultConfig();
    this.baseCommand = this.plugin.registerModule(this);
    this.onCreate();
  }

  public void onDisable() {
    this.onDestroy();
  }
}
