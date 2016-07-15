package net.rocketeer.eventsuite.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

public class TablesLock implements AutoCloseable {
  private final Connection conn;
  private boolean locked = false;

  public enum Type { READ, WRITE }
  public TablesLock(Connection connection, Type type, String... tables) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("LOCK TABLES ");
    StringJoiner joiner = new StringJoiner(", ");
    for (String table : tables) {
      if (type == Type.READ)
        joiner.add(table + " READ");
      else
        joiner.add(table + " WRITE");
    }
    String query = queryBuilder.append(joiner.toString()).toString();
    this.conn = connection;
    try (Statement stmt = this.conn.createStatement()) {
      stmt.execute(query);
      this.locked = true;
    } catch (SQLException e) {}
  }

  @Override
  public void close() throws Exception {
    if (!this.locked)
      return;
    this.locked = false;
    try (Statement stmt = this.conn.createStatement()) {
      stmt.execute("UNLOCK TABLES");
    } catch (SQLException e) {}
  }
}
