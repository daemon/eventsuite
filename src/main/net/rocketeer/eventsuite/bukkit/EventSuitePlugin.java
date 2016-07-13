package net.rocketeer.eventsuite.bukkit;

import net.md_5.bungee.api.ChatColor;
import net.rocketeer.eventsuite.EndpointEnum;
import net.rocketeer.eventsuite.EventBus;
import net.rocketeer.eventsuite.Subscribe;
import net.rocketeer.eventsuite.bukkit.command.AnnounceCommand;
import net.rocketeer.eventsuite.bukkit.command.EventSuiteBaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;

  @Override
  public void onEnable() {
    instance = this;
    this.eventBus = new EventBus();
//    Bukkit.getScheduler().runTaskAsynchronously(this, this.eventBus::connect);
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", eventBus);
    EventSuiteBaseCommand baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    this.getCommand("es").setExecutor(baseCmd);
    this.eventBus.subscribe(EndpointEnum.ANNOUNCE_MESSAGE.get(), this);
  }

  @Subscribe
  public void test(String message) {
    this.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
  }

  public EventBus eventBus() {
    return this.eventBus;
  }
}
