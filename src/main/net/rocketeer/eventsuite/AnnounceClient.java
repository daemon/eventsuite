package net.rocketeer.eventsuite;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AnnounceClient {
  public static int PORT = 15843;
  public static InetSocketAddress INET_ADDRESS;
  private SocketChannel channel;
  private boolean running = false;
  private NetworkTask networkTask = new NetworkTask();

  static {
    try {
      INET_ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
    } catch (UnknownHostException ignored) {}
  }

  public void connect() {
    if (this.running)
      return;
    this.running = true;
    this.networkTask.run();
  }

  public void broadcastMessage(String message) {
    if (message.length() > 255)
      return;
    this.networkTask.queueMessage(message + "\n");
  }

  private class NetworkTask implements Runnable {
    private Lock sendLock = new ReentrantLock();
    private Condition queueNotEmpty = sendLock.newCondition();
    private Queue<String> sendQueue = new LinkedList<>();

    private void cleanup() {
      if (!running)
        return;
      try {
        if (channel != null)
          channel.close();
      } catch (IOException ignored) {}
      running = false;
    }

    public void queueMessage(String message) {
      this.sendLock.lock();
      try {
        this.sendQueue.add(message);
        this.queueNotEmpty.signalAll();
      } finally {
        this.sendLock.unlock();
      }
    }

    private void connect() throws IOException {
      if (channel != null && channel.isConnected())
        return;
      channel = SocketChannel.open();
      channel.connect(INET_ADDRESS);
    }

    private void reconnect() {
      this.sendLock.unlock();
      while (true) {
        if (!running)
          return;
        if (channel != null)
          try {
            channel.close();
          } catch (IOException ignored) {}
        try {
          channel = SocketChannel.open();
          channel.connect(INET_ADDRESS);
        } catch (IOException ignored) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {}
        }

        try {
          if (channel != null && channel.finishConnect()) {
            sendLock.lock();
            return;
          }
        } catch (IOException e) {}
      }
    }

    public void sendBuffer(ByteBuffer buffer) {
      boolean sent = false;
      while (!sent) {
        try {
          sendLock.unlock();
          try {
            channel.write(buffer);
            sent = true;
          } finally {
            sendLock.lock();
          }
        } catch (IOException e) {
          this.reconnect();
        } finally {
          buffer.clear();
        }
      }
    }

    @Override
    public void run() {
      sendLock.lock();
      this.reconnect();
      ByteBuffer buffer = ByteBuffer.allocate(256);
      try {
        while (true) {
          if (!running)
            return;
          while (this.sendQueue.isEmpty())
            queueNotEmpty.await();
          while (!this.sendQueue.isEmpty()) {
            String message = this.sendQueue.poll();
            buffer.put(message.getBytes());
            buffer.flip();
            this.sendBuffer(buffer);
          }
        }
      } catch (InterruptedException e) {
        this.cleanup();
      } finally {
        this.cleanup();
        sendLock.unlock();
      }
    }
  }
}
