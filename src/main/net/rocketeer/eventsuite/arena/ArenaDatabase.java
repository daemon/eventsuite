package net.rocketeer.eventsuite.arena;

import com.sk89q.worldedit.regions.CuboidRegion;
import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.database.DatabaseManager;
import net.rocketeer.eventsuite.database.TablesLock;
import net.rocketeer.eventsuite.geometry.Point;
import net.rocketeer.eventsuite.geometry.WKBReader;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ArenaDatabase {
  private final DatabaseManager manager;
  // aid, pid, x, y, z, server_id, world_id, pname, sname, wname
  private static final String findArenaStmt = "SELECT arena.id, server.name AS sname, " +
      "world.name AS wname, region.name AS rname, xz1, xz2, y1, y2 FROM arena INNER JOIN server ON " +
      "arena.server_id=server.id INNER JOIN world ON arena.world_id=world.id INNER JOIN region ON " +
      "arena.base_region_id=region.id WHERE arena.name=?";
  private static final String findRegionsStmt = "SELECT region_id, name, xz1, xz2, y1, y2 FROM arena_region_assoc " +
      "INNER JOIN region ON region.id=region_id WHERE arena_id=?";
  private static final String[] TABLES = {"arena", "arena_point_assoc", "arena_region_assoc", "server", "world", "point", "region"};

  public ArenaDatabase() {
    this.manager = EventSuitePlugin.instance.databaseManager();
  }

  private Point[] readPoints(ResultSet rs, int begin, int end) throws SQLException, IOException {
    Point[] points = new Point[end - begin + 1];
    for (int i = begin; i <= end; ++i)
      try (WKBReader reader = new WKBReader(rs.getBinaryStream(i))) {
        Point point = reader.asPoint();
        if (point == null)
          return null;
        points[i - begin] = point;
      }
    return points;
  }

  private Arena readArenaRs(ResultSet rs) throws SQLException, IOException {
    int arenaId = rs.getInt(1);
    String serverName = rs.getString(2);
    String worldName = rs.getString(3);
    String regionName = rs.getString(4);
    Point[] points = this.readPoints(rs, 5, 8);
    if (points == null)
      return null;
    World world = Bukkit.getWorld(worldName);
    if (world == null)
      return null;
    CuboidRegion region = Point.from(world, points[0], points[1], points[2], points[3]);
    return new Arena(arenaId, serverName, world, new Arena.Region(regionName, region));
  }

  private List<Arena.Region> readRegionsRs(ResultSet rs, World world) throws SQLException, IOException {
    List<Arena.Region> regions = new LinkedList<>();
    while (rs.next()) {
      String name = rs.getString(2);
      Point[] points = this.readPoints(rs, 3, 6);
      if (points == null)
        continue;
      regions.add(new Arena.Region(name, Point.from(world, points[0], points[1], points[2], points[3])));
    }
    return regions;
  }

  public Arena findArena(String name) {
    try (Connection c = this.manager.getConnection();
         PreparedStatement stmt = c.prepareStatement(findArenaStmt);
         PreparedStatement stmt2 = c.prepareStatement(findRegionsStmt);
         TablesLock unused = new TablesLock(c, TablesLock.Type.READ, TABLES)) {
      stmt.setString(1, name);
      Arena arena;
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          arena = this.readArenaRs(rs);
          if (arena == null)
            return null;
        } else {
          return null;
        }
      }
      try (ResultSet rs = stmt2.executeQuery()) {
        List<Arena.Region> regions = this.readRegionsRs(rs, arena.world());
        arena.addRegions(regions);
        return arena;
      }
    } catch (Exception e) {
      return null;
    }
  }
}
