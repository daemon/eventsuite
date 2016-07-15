package net.rocketeer.eventsuite.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.rocketeer.eventsuite.ConfigManager;
import net.rocketeer.eventsuite.EventSuitePlugin;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
  private final ComboPooledDataSource source;
  private final ConfigManager cfgManager;

  public DatabaseManager(ConfigManager cfgManager) throws PropertyVetoException {
    this.cfgManager = cfgManager;
    this.source = new ComboPooledDataSource();
    this.source.setMaxStatements(128);
    this.source.setMaxStatementsPerConnection(16);
    this.source.setMinPoolSize(2);
    this.source.setMaxPoolSize(8);
    int port = this.cfgManager.sqlPort();
    String user = this.cfgManager.sqlUser();
    String password = this.cfgManager.sqlPassword();
    String database = this.cfgManager.sqlDatabaseName();
    String hostname = this.cfgManager.sqlHostname();
    this.source.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
    this.source.setUser(user);
    this.source.setPassword(password);
    this.source.setMaxIdleTime(3600);
    this.source.setAutoCommitOnClose(true);
    this.source.setDriverClass("com.mysql.jdbc.Driver");
  }

  public Connection getConnection() throws SQLException {
    return this.source.getConnection();
  }

  public void createDefaultTables() throws SQLException, IOException {
    try (Connection c = this.getConnection();
         InputStream in = this.getClass().getResourceAsStream("/schema.sql")) {
      new SqlStreamExecutor(c, in).execute();
    }
  }
}
