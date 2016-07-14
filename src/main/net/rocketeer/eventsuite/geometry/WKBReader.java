package net.rocketeer.eventsuite.geometry;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class WKBReader {
  private final boolean reverseBytes;
  DataInputStream in;

  public WKBReader(InputStream stream) throws IOException {
    this.in = new DataInputStream(stream);
    boolean isLittleEndian = this.in.readByte() == 0;
    this.reverseBytes = (isLittleEndian != ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN));
  }

  private short readShort() throws IOException {
    short s = this.in.readShort();
    if (this.reverseBytes)
      return Short.reverseBytes(s);
    return s;
  }

  private int readInt() throws IOException {
    int i = this.in.readInt();
    if (this.reverseBytes)
      return Integer.reverseBytes(i);
    return i;
  }

  public Point asPoint() throws IOException {
    int type = this.readInt();
    if (type != 1)
      return null;
    return new Point(this.in.readDouble(), this.in.readDouble());
  }
}
