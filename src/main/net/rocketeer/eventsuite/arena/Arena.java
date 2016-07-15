package net.rocketeer.eventsuite.arena;

import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;

public class Arena {
  private final Region baseRegion;
  private final String server;
  private final World world;
  private List<Region> regions = new LinkedList<>();
  final int id;

  Arena(int id, String server, World world, Region baseRegion) {
    this.server = server;
    this.world = world;
    this.baseRegion = baseRegion;
    this.id = id;
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

  public static class Region {
    private CuboidRegion cuboidRegion;
    private String name;

    public Region(String name, CuboidRegion cuboidRegion) {
      this.cuboidRegion = cuboidRegion;
      this.name = name;
    }
  }
}
