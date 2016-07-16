package net.rocketeer.eventsuite.arena;

import com.sk89q.worldedit.regions.CuboidRegion;
import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.database.DatabaseManager;
import net.rocketeer.eventsuite.database.TablesLock;
import net.rocketeer.eventsuite.geometry.Point;
import net.rocketeer.eventsuite.geometry.WKBReader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArenaDatabase {
  public enum ArenaInsertResult { SUCCESS, EXISTS, SQL_FAILURE };
  private enum ArenaAreaInsertResult { SUCCESS, NO_SUCH_ARENA, SQL_FAILURE };
  private final DatabaseManager manager;
  // aid, pid, x, y, z, server_id, world_id, pname, sname, wname
  private static final String findArenaStmt = "SELECT arena.id, server.name AS sname, " +
      "world.name AS wname, region.name AS rname, xz1, xz2, y1, y2, arena.name AS aname FROM arena INNER JOIN server ON " +
      "arena.server_id=server.id INNER JOIN world ON arena.world_id=world.id INNER JOIN region ON " +
      "arena.base_region_id=region.id WHERE arena.name=?";
  private static final String findRegionsStmt = "SELECT region_id, name, xz1, xz2, y1, y2 FROM arena_region_assoc " +
      "INNER JOIN region ON region.id=region_id WHERE arena_id=?";
  private static final String insertArenaCallStmt = "{call InsertArena(?, ?, ?, POINT(?, ?), POINT(?, ?), " +
      "POINT(?, ?), POINT(?, ?), ?)}";
  private static final String insertArenaPointCallStmt = "{call InsertArenaPoint(?, POINT(?, ?), ?, ?, ?)}";
  private static final String insertArenaRegionCallStmt = "{call InsertArenaRegion(?, ?, POINT(?, ?), POINT(?, ?), POINT(?, ?), POINT(?, ?), ?)}";
  private static final String checkRegionsStmt = "SELECT * FROM region INNER JOIN arena_region_assoc ON " +
      "arena_region_assoc.region_id=region.id WHERE arena_id=? AND MBRContains(CreateEnvelope(xz1, xz2), " +
      "POINT(?, ?)) AND MBRContains(CreateEnvelope(y1, y2), POINT(?, ?))";
  // private static final String findRegionsContainingStmt = "SELECT "
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
    String serverName = rs.getString(2);
    String worldName = rs.getString(3);
    String regionName = rs.getString(4);
    Point[] points = this.readPoints(rs, 5, 8);
    String arenaName = rs.getString(9);
    if (points == null)
      return null;
    World world = Bukkit.getWorld(worldName);
    if (world == null)
      return null;
    CuboidRegion region = Point.from(world, points[0], points[1], points[2], points[3]);
    return new Arena(arenaName, serverName, world, new Arena.Region(regionName, region));
  }

  public RegionCheckResult findContainingRegions(Arena arena, Vector location) {
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    if (!arena.id().isPresent())
      return null;
    try (Connection c = this.manager.getConnection();
         PreparedStatement stmt = c.prepareCall(checkRegionsStmt);
         TablesLock unused = new TablesLock(c, TablesLock.Type.READ, TABLES)) {
      stmt.setInt(1, arena.id().get());
      stmt.setDouble(2, location.getX());
      stmt.setDouble(3, location.getZ());
      stmt.setDouble(4, location.getX());
      stmt.setDouble(5, location.getY());
      List<Arena.Region> regions;
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next())
          return new RegionCheckResult(this.readRegionsRs(rs, arena.world()));
        else
          return new RegionCheckResult(RegionCheckResult.Type.SUCCESS);
      }
    } catch (SQLException e) {
      return new RegionCheckResult(RegionCheckResult.Type.SQL_FAILURE);
    } catch (Exception e) {
      return new RegionCheckResult(RegionCheckResult.Type.UNKNOWN_FAILURE);
    }
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

  public ArenaAreaInsertResult insertArenaPoints(Connection c, Arena arena) throws SQLException {
    try (CallableStatement stmt = c.prepareCall(insertArenaPointCallStmt)) {
      for (Arena.NamedPoint point : arena.points()) {
        stmt.setString(1, arena.name());
        stmt.setDouble(2, point.point().getX());
        stmt.setDouble(3, point.point().getZ());
        stmt.setString(4, point.name());
        stmt.setInt(5, point.point().getBlockY());
        stmt.registerOutParameter(6, Types.INTEGER);
        stmt.executeUpdate();
        int status = stmt.getInt(6);
        ArenaAreaInsertResult ret = ArenaAreaInsertResult.values()[status];
        if (ret != ArenaAreaInsertResult.SUCCESS)
          return ret;
      }
    }
    return ArenaAreaInsertResult.SUCCESS;
  }

  public ArenaAreaInsertResult insertArenaRegions(Connection c, Arena arena) throws SQLException {
    try (CallableStatement stmt = c.prepareCall(insertArenaRegionCallStmt)) {
      for (Arena.Region region : arena.regions()) {
        stmt.setString(1, arena.name());
        stmt.setString(2, region.name());
        Point[] points = Point.from(region.cuboidRegion());
        for (int i = 0; i < 4; ++i) {
          stmt.setDouble(i * 2 + 3, points[i].x);
          stmt.setDouble(i * 2 + 4, points[i].y);
        }
        stmt.registerOutParameter(11, Types.INTEGER);
        stmt.executeUpdate();
        int status = stmt.getInt(11);
        ArenaAreaInsertResult ret = ArenaAreaInsertResult.values()[status];
        if (ret != ArenaAreaInsertResult.SUCCESS)
          return ret;
      }
    }
    return ArenaAreaInsertResult.SUCCESS;
  }

  public ArenaInsertResult insertArena(Arena arena) {
    try (Connection c = this.manager.getConnection();
         CallableStatement stmt = c.prepareCall(insertArenaCallStmt);
         TablesLock unused = new TablesLock(c, TablesLock.Type.WRITE, TABLES)) {
      stmt.setString(1, arena.name());
      stmt.setString(2, arena.world().getName());
      stmt.setString(3, arena.serverName());
      Arena.Region baseRegion = arena.baseRegion();
      Point[] points = Point.from(baseRegion.cuboidRegion());
      for (int i = 0; i < 4; ++i) {
        stmt.setDouble(i * 2 + 4, points[i].x);
        stmt.setDouble(i * 2 + 5, points[i].y);
      }
      stmt.registerOutParameter(12, Types.INTEGER);
      stmt.registerOutParameter(13, Types.INTEGER);
      stmt.executeUpdate();
      ArenaInsertResult ret = ArenaInsertResult.values()[stmt.getInt(12)];
      if (ret != ArenaInsertResult.SUCCESS)
        return ret;
      ArenaAreaInsertResult ret2 = this.insertArenaPoints(c, arena);
      if (ret2 != ArenaAreaInsertResult.SUCCESS)
        return ArenaInsertResult.SQL_FAILURE;
      ret2 = this.insertArenaRegions(c, arena);
      if (ret2 != ArenaAreaInsertResult.SUCCESS)
        return ArenaInsertResult.SQL_FAILURE;
      arena.setId(stmt.getInt(13));
    } catch (Exception e) {
      e.printStackTrace();
      return ArenaInsertResult.SQL_FAILURE;
    }
    return ArenaInsertResult.SUCCESS;
  }

  public Arena findArena(String name) {
    try (Connection c = this.manager.getConnection();
         PreparedStatement stmt = c.prepareStatement(findArenaStmt);
         PreparedStatement stmt2 = c.prepareStatement(findRegionsStmt);
         TablesLock unused = new TablesLock(c, TablesLock.Type.READ, TABLES)) {
      stmt.setString(1, name);
      Arena arena;
      int arenaId;
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          arenaId = rs.getInt(1);
          arena = this.readArenaRs(rs);
          if (arena == null)
            return null;
        } else {
          return null;
        }
      }
      stmt2.setInt(1, arenaId);
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
