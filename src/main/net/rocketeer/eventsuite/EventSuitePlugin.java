package net.rocketeer.eventsuite;

import net.rocketeer.eventsuite.command.TeleportCommand;
import net.rocketeer.eventsuite.database.DatabaseManager;
import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.EventBus;
import net.rocketeer.eventsuite.command.AnnounceCommand;
import net.rocketeer.eventsuite.command.EventSuiteBaseCommand;
import net.rocketeer.eventsuite.command.FindCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;
  private String serverName;
  private ConfigManager configManager;
  private DatabaseManager databaseManager;
  private BungeeServerManager manager;

  private void setupManagers() {
    this.configManager = new ConfigManager(this.getConfig());
    this.eventBus = new EventBus();
    BungeeServerManager manager = new BungeeServerManager();
    this.manager = manager;
    try {
      this.databaseManager = new DatabaseManager();
      this.databaseManager.createDefaultTables();
    } catch (PropertyVetoException | SQLException e) {
      Bukkit.getLogger().warning("Couldn't connect to database");
      return;
    } catch (IOException e) {
      Bukkit.getLogger().warning("Schema file not present in .jar");
      return;
    }
    Bukkit.getPluginManager().registerEvents(manager, this);
    Messenger messenger = this.getServer().getMessenger();
    messenger.registerOutgoingPluginChannel(this, "BungeeCord");
    messenger.registerIncomingPluginChannel(this, "BungeeCord", eventBus);
    messenger.registerIncomingPluginChannel(this, "BungeeCord", manager);
    this.eventBus.subscribe(Subscribers.makeDefault());
  }

  private void setupCommands() {
    EventSuiteBaseCommand baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    baseCmd.registerPlayerCommand(new FindCommand());
    baseCmd.registerCommand(new TeleportCommand());
    this.getCommand("es").setExecutor(baseCmd);
  }

  @Override
  public void onEnable() {
    instance = this;
    this.saveDefaultConfig();
    this.setupManagers();
    this.setupCommands();
  }

  public BungeeServerManager bungeeManager() {
    return this.manager;
  }

  public ConfigManager configManager() {
    return this.configManager;
  }

  public DatabaseManager databaseManager() {
    return this.databaseManager();
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
