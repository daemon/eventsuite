package net.rocketeer.eventsuite.bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.rocketeer.eventsuite.Endpoint;
import net.rocketeer.eventsuite.Endpoints;
import net.rocketeer.eventsuite.EventBus;
import net.rocketeer.eventsuite.bukkit.command.AnnounceCommand;
import net.rocketeer.eventsuite.bukkit.command.EventSuiteBaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;

  @Override
  public void onEnable() {
    instance = this;
    this.eventBus = new EventBus();
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", eventBus);
    EventSuiteBaseCommand baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    this.getCommand("es").setExecutor(baseCmd);
    this.eventBus.subscribe(Subscribers.makeDefault());
  }

  public static void announce(String str) {
    announce(str, "Events");
  }

  public static void announce(String str, String tag) {
    str = "&b[&6" + tag + "&b]&f " + str.trim();
    String bc = ChatColor.translateAlternateColorCodes('&', str);
    Bukkit.getServer().broadcastMessage(bc);
    EventSuitePlugin.instance.eventBus().publish(new Endpoint(Endpoints.ANNOUNCE_MESSAGE), str);
  }

  public EventBus eventBus() {
    return this.eventBus;
  }
}
