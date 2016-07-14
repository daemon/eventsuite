package net.rocketeer.eventsuite.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.rocketeer.eventsuite.ConfigManager;
import net.rocketeer.eventsuite.EventSuitePlugin;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DatabaseManager {
  private final ComboPooledDataSource source;
  private final ConfigManager cfgManager;

  public DatabaseManager() throws PropertyVetoException {
    this.cfgManager = EventSuitePlugin.instance.configManager();
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

  public Connection getConnection() throws SQLException
  {
    return this.source.getConnection();
  }

  public void createDefaultTables() throws SQLException {
    try (Connection c = this.getConnection()) {
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS arenas (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, name VARCHAR(32) NOT NULL, map_base_pt_id INT NOT NULL, INDEX name_i(name), UNIQUE(name)) ENGINE=MyISAM");
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS regions (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, name VARCHAR(32) NOT NULL, p1_id INT UNSIGNED NOT NULL, p2_id INT UNSIGNED NOT NULL, py_id INT UNSIGNED NOT NULL LINESTRING NOT NULL, SPATIAL INDEX " +
          "xz_region_i(xz_region), SPATIAL INDEX y_range_i(y_range)) ENGINE=MyISAM");
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS points (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, name VARCHAR(32) NOT NULL, xz POINT NOT NULL, y INT NOT NULL, z INT NOT NULL, server_id INT UNSIGNED NOT NULL, " +
          "world_id INT UNSIGNED NOT NULL) ENGINE=MyISAM");
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS arena_region_assoc (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, arena_id INT UNSIGNED NOT NULL, region_id INT UNSIGNED NOT NULL, INDEX arena_id_i(arena_id)) ENGINE=MyISAM");
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS arena_point_assoc (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, arena_id INT UNSIGNED NOT NULL, point_id INT UNSIGNED NOT NULL, INDEX arena_id_i2(arena_id)) ENGINE=MyISAM");
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS servers (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, name VARCHAR(32) NOT NULL) ENGINE=MyISAM");
      c.createStatement().execute("CREATE TABLE IF NOT EXISTS worlds (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY " +
          "NOT NULL, name VARCHAR(32) NOT NULL) ENGINE=MyISAM");
      c.createStatement().execute("DROP FUNCTION IF EXISTS CreateEnvelope");
      c.createStatement().execute("CREATE FUNCTION CreateEnvelope(p1 POINT, p2 POINT) RETURNS POLYGON DETERMINISTIC BEGIN DECLARE bounding POLYGON; SET bounding = Envelope(LineString(p1, p2)); RETURN bounding; END");
    }
  }
}
