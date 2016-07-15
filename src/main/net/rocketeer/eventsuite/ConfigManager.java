package net.rocketeer.eventsuite;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
  private final String hostname;
  private final String database;
  private final String password;
  private final String user;
  private final int port;

  public ConfigManager(FileConfiguration config) {
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

  public File eventModuleDirectory() {
    return null;
  }
}
