package net.rocketeer.eventsuite.geometry;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.World;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Point {
  public final double x;
  public final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public InputStream toStream() {
    ByteBuffer buffer = ByteBuffer.allocate(21);
    buffer.put((byte) (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ? 1 : 0));
    buffer.putInt(1);
    buffer.putDouble(this.x);
    buffer.putDouble(this.y);
    buffer.flip();
    return new ByteArrayInputStream(buffer.array());
  }

  public static CuboidRegion from(World world, Point xz1, Point xz2, Point y1, Point y2) {
    Vector vec1 = new Vector(xz1.x, y1.y, xz1.y);
    Vector vec2 = new Vector(xz2.x, y2.y, xz2.y);
    return new CuboidRegion(new BukkitWorld(world), vec1, vec2);
  }

  public static Point[] from(CuboidRegion region) {
    Vector minPoint = region.getMinimumPoint();
    Vector maxPoint = region.getMaximumPoint();
    Point[] points = new Point[4];
    points[0] = new Point(minPoint.getX(), minPoint.getZ());
    points[1] = new Point(maxPoint.getX(), maxPoint.getZ());
    points[2] = new Point(maxPoint.getX(), minPoint.getY());
    points[3] = new Point(maxPoint.getX(), maxPoint.getY());
    return points;
  }
}