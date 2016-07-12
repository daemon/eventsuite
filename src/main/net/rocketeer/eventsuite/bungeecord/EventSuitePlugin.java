package net.rocketeer.eventsuite.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

public class EventSuitePlugin extends Plugin {
  public static EventSuitePlugin instance;
  @Override
  public void onEnable() {
    instance = this;
    try {
      EventBusServer announceServer = new EventBusServer();
      announceServer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void runAsync(Runnable runnable) {
    instance.getProxy().getScheduler().runAsync(instance, runnable);
  }
}
