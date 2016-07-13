package net.rocketeer.eventsuite.api;

import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.eventbus.EventBus;

public abstract class EventModule {
  private final EventSuitePlugin plugin;

  public EventModule(EventSuitePlugin plugin) {
    this.plugin = plugin;
  }

  public EventSuitePlugin plugin() {
    return this.plugin;
  }

  public EventBus eventBus() {
    return this.plugin.eventBus();
  }

  public abstract void onEnable();
  public abstract void onReload();
  public abstract void onDisable();
}
