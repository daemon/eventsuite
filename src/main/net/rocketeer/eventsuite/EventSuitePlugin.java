package net.rocketeer.eventsuite;

import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.EventBus;
import net.rocketeer.eventsuite.command.AnnounceCommand;
import net.rocketeer.eventsuite.command.EventSuiteBaseCommand;
import net.rocketeer.eventsuite.command.FindCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;
  private String serverName;

  @Override
  public void onEnable() {
    instance = this;
    this.eventBus = new EventBus();
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", eventBus);
    EventSuiteBaseCommand baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    baseCmd.registerPlayerCommand(new FindCommand());
    this.getCommand("es").setExecutor(baseCmd);
    this.eventBus.subscribe(Subscribers.makeDefault());
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
