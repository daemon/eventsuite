package net.rocketeer.eventsuite.arena;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Arena {
  private final ProtectedRegion baseRegion;
  private final World world;
  private final String name;
  private List<ProtectedRegion> regions = new LinkedList<>();
  private List<NamedPoint> points = new LinkedList<>();
  private Map<String, ProtectedRegion> regionMap = new HashMap<>();
  private Map<String, NamedPoint> pointMap = new HashMap<>();

  public Arena(String name, World world, ProtectedRegion baseRegion) {
    this.world = world;
    this.baseRegion = baseRegion;
    this.regionMap.put("base", baseRegion);
    this.name = name;
  }

  public String name() {
    return this.name;
  }

  public World world() {
    return this.world;
  }

  public List<NamedPoint> points() {
    return this.points;
  }

  public void addRegions(List<ProtectedRegion> regions) {
    if (this.regions.isEmpty())
      this.regions = regions;
    else
      this.regions.addAll(regions);
    for (ProtectedRegion region : regions)
      this.regionMap.put(region.getId().replace(this.name + "_", ""), region);
  }

  public void addPoints(List<NamedPoint> points) {
    if (this.points.isEmpty())
      this.points = points;
    else
      this.points.addAll(points);
    for (NamedPoint point : points)
      this.pointMap.put(point.name(), point);
  }

  public ProtectedRegion baseRegion() {
    return this.baseRegion;
  }

  public ProtectedRegion findRegion(String name) {
    return this.regionMap.get(name);
  }

  public NamedPoint findPoint(String name) {
    return this.pointMap.get(name);
  }

  public List<ProtectedRegion> regions() {
    return this.regions;
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
}