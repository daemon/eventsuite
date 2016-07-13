package net.rocketeer.eventsuite.eventbus.message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class StringNetBuffer {
  public static byte TERMINAL = '\0';
  ByteBuffer buffer;
  private StringBuilder builder = new StringBuilder();
  private Callback callback;
  public StringNetBuffer() {
    this(256);
  }

  public StringNetBuffer(int bufferSize) {
    this.buffer = ByteBuffer.allocate(bufferSize);
  }

  public ByteBuffer buffer() {
    return this.buffer;
  }

  public StringNetBuffer onNewMessage(Callback callback) {
    this.callback = callback;
    return this;
  }

  public void parse(SocketChannel channel) {
    this.buffer.flip();
    try {
      while (this.buffer.hasRemaining()) {
        byte c = this.buffer.get();
        if (c != TERMINAL) {
          this.builder.append(c);
          continue;
        }

        String str = this.builder.toString();
        this.builder.setLength(0);
        if (this.callback != null)
          this.callback.onMessage(channel, str);
      }
    } finally {
      this.buffer.clear();
    }
  }

  public interface Callback {
    void onMessage(SocketChannel channel, String message);
  }
}
