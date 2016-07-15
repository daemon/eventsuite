package net.rocketeer.eventsuite.geometry;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.World;

public class Point {
  public final double x;
  public final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public static CuboidRegion from(World world, Point xz1, Point xz2, Point y1, Point y2) {
    Vector vec1 = new Vector(xz1.x, y1.y, xz1.y);
    Vector vec2 = new Vector(xz2.x, y2.y, xz2.y);
    return new CuboidRegion(new BukkitWorld(world), vec1, vec2);
  }
}
