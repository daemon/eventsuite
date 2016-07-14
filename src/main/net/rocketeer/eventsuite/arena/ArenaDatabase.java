package net.rocketeer.eventsuite.arena;

import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ArenaDatabase {
  private final DatabaseManager manager;
  // aid, pid, x, y, z, server_id, world_id, pname, sname, wname
  private static final String findArenaStmt = "SELECT t2.*, name AS wname FROM (SELECT t1.*, name AS sname FROM " +
      "(SELECT arenas.id AS aid, points.id AS pid, x, y, z, server_id, world_id, points.name as pname FROM arenas " +
      "INNER JOIN points ON map_base_pt_id=points.id WHERE arenas.name = ?) AS t1 INNER JOIN servers ON " +
      "server_id=servers.id) AS t2 INNER JOIN worlds ON worlds.id=world_id";

  public ArenaDatabase() {
    this.manager = EventSuitePlugin.instance.databaseManager();
  }

  public Arena findArena(String name) {
    try (Connection c = this.manager.getConnection();
         PreparedStatement stmt = c.prepareStatement(findArenaStmt)) {
      stmt.setString(1, name);
      Arena arena;
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          int arenaId = rs.getInt(1);
          int x = rs.getInt(3);
          int y = rs.getInt(4);
          int z = rs.getInt(5);
          String pointName = rs.getString(8);
          String serverName = rs.getString(9);
          String worldName = rs.getString(10);
        }
      }
    } catch (Exception e) {
      return null;
    }
}
