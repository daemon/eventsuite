package net.rocketeer.eventsuite;

import java.nio.ByteBuffer;

public class StringNetBuffer {
  public static byte TERMINAL = '\0';
  ByteBuffer buffer;
  private StringBuilder builder;
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

  public void parse() {
    this.buffer.flip();
    try {
      while (this.buffer.hasRemaining()) {
        byte c = this.buffer.get();
        if (c != TERMINAL) {
          builder.append(c);
          continue;
        }

        String str = builder.toString();
        if (this.callback != null)
          this.callback.onMessage(str);
      }
    } finally {
      this.buffer.clear();
    }
  }

  public interface Callback {
    void onMessage(String message);
  }
}
