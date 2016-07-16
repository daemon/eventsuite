package net.rocketeer.eventsuite;

import net.rocketeer.eventsuite.api.EventModule;
import net.rocketeer.eventsuite.arena.ArenaCreationWizard;
import net.rocketeer.eventsuite.command.*;
import net.rocketeer.eventsuite.config.ConfigManager;
import net.rocketeer.eventsuite.database.DatabaseManager;
import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.EventBus;
import net.rocketeer.eventsuite.eventbus.Subscribers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;
  private String serverName;
  private ConfigManager configManager;
  private DatabaseManager databaseManager;
  private BungeeServerManager manager;
  private EventSuiteBaseCommand baseCmd;

  // TODO ModuleManager?
  public ModuleBaseCommand registerModule(EventModule module) {
    String name = module.getConfig().getString("eventsuite.name");
    List<String> commands = module.getConfig().getStringList("eventsuite.commands");
    ModuleBaseCommand mbc = new ModuleBaseCommand(module);
    this.baseCmd.registerCommand(mbc);
    return mbc;
  }

  public BaseCommandExecutor baseCommand() {
    return this.baseCmd;
  }

  private void setupManagers() {
    this.configManager = new ConfigManager(this.getConfig());
    this.eventBus = new EventBus();
    BungeeServerManager manager = new BungeeServerManager();
    this.manager = manager;
    try {
      this.databaseManager = new DatabaseManager(this.configManager);
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
    this.baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    baseCmd.registerPlayerCommand(new FindCommand());
    baseCmd.registerCommand(new TeleportCommand());
    baseCmd.registerPlayerCommand(new ArenaCreateCommand());
    this.getCommand("es").setExecutor(baseCmd);
  }

  @Override
  public void onEnable() {
    instance = this;
    this.saveDefaultConfig();
    this.setupManagers();
    this.setupCommands();
    ArenaCreationWizard.init();
  }

  public BungeeServerManager bungeeManager() {
    return this.manager;
  }

  public ConfigManager configManager() {
    return this.configManager;
  }

  public DatabaseManager databaseManager() {
    return this.databaseManager;
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
