package net.rocketeer.eventsuite.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
  private final FileConfiguration config;
  private volatile String hostname;
  private volatile String database;
  private volatile String password;
  private volatile String user;
  private volatile int port;

  public ConfigManager(FileConfiguration config) {
    this.config = config;
    this.port = config.getInt("sql-port");
    this.user = config.getString("sql-user");
    this.password = config.getString("sql-password");
    this.database = config.getString("sql-database");
    this.hostname = config.getString("sql-hostname");
  }

  public String sqlHostname() {
    return this.hostname;
  }

  public String sqlDatabaseName() {
    return this.database;
  }

  public String sqlPassword() {
    return this.password;
  }

  public String sqlUser() {
    return this.user;
  }

  public int sqlPort() {
    return this.port;
  }

  public void reload() {
    this.port = config.getInt("sql-port");
    this.user = config.getString("sql-user");
    this.password = config.getString("sql-password");
    this.database = config.getString("sql-database");
    this.hostname = config.getString("sql-hostname");
  }
}
