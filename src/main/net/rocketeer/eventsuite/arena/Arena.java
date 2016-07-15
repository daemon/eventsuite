package net.rocketeer.eventsuite.arena;

import com.sk89q.worldedit.regions.CuboidRegion;
import net.rocketeer.eventsuite.geometry.Point;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;

public class Arena {
  private final Region baseRegion;
  private final String server;
  private final World world;
  private final String name;
  private List<Region> regions = new LinkedList<>();
  private List<NamedPoint> points = new LinkedList<>();

  public Arena(String name, String server, World world, Region baseRegion) {
    this.server = server;
    this.world = world;
    this.baseRegion = baseRegion;
    this.name = name;
  }

  public String serverName() {
    return this.server;
  }

  public String name() {
    return this.name;
  }

  public World world() {
    return this.world;
  }

  public void addRegions(List<Region> regions) {
    if (this.regions.isEmpty())
      this.regions = regions;
    else
      this.regions.addAll(regions);
  }

  public void addPoints(List<NamedPoint> points) {
    if (this.points.isEmpty())
      this.points = points;
    else
      this.points.addAll(points);
  }

  public Region baseRegion() {
    return this.baseRegion;
  }

  public static class NamedPoint {
    private final Vector point;
    private final String name;

    public NamedPoint(String name, Vector point) {
      this.point = point;
      this.name = name;
    }

    public String name() {
      return this.name;
    }

    public Vector point() {
      return this.point;
    }
  }

  public static class Region {
    private CuboidRegion cuboidRegion;
    private String name;

    public Region(String name, CuboidRegion cuboidRegion) {
      this.cuboidRegion = cuboidRegion;
      this.name = name;
    }

    public CuboidRegion cuboidRegion() {
      return this.cuboidRegion;
    }
  }
}
