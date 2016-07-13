package net.rocketeer.eventsuite;

import net.rocketeer.eventsuite.command.TeleportCommand;
import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.EventBus;
import net.rocketeer.eventsuite.command.AnnounceCommand;
import net.rocketeer.eventsuite.command.EventSuiteBaseCommand;
import net.rocketeer.eventsuite.command.FindCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;
  private String serverName;
  private BungeeServerManager manager;

  @Override
  public void onEnable() {
    instance = this;
    this.eventBus = new EventBus();
    BungeeServerManager manager = new BungeeServerManager();
    this.manager = manager;
    Bukkit.getPluginManager().registerEvents(manager, this);
    Messenger messenger = this.getServer().getMessenger();
    messenger.registerOutgoingPluginChannel(this, "BungeeCord");
    messenger.registerIncomingPluginChannel(this, "BungeeCord", eventBus);
    messenger.registerIncomingPluginChannel(this, "BungeeCord", manager);
    EventSuiteBaseCommand baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    baseCmd.registerPlayerCommand(new FindCommand());
    baseCmd.registerCommand(new TeleportCommand());
    this.getCommand("es").setExecutor(baseCmd);
    this.eventBus.subscribe(Subscribers.makeDefault());
  }

  public BungeeServerManager bungeeManager() {
    return this.manager;
  }

  public static void announce(String str) {
    announce(str, "Events");
  }

  public static void announce(String str, String tag) {
    str = "&b[&6" + tag + "&b]&f " + str.trim();
    EventSuitePlugin.instance.eventBus().publishAll(new Endpoint(Endpoints.ANNOUNCE_MESSAGE), str);
  }

  public EventBus eventBus() {
    return this.eventBus;
  }
}
