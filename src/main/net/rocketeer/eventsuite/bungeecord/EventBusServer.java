package net.rocketeer.eventsuite.bungeecord;

import net.rocketeer.eventsuite.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBusServer {
  private boolean running = false;
  private Selector selector;
  private Map<SocketChannel, StringNetBuffer> channelToMessage = new HashMap<>();
  private EndpointTrie<SocketChannel> subscribers = new EndpointTrie<>();

  public synchronized void start() throws IOException {
    if (this.running)
      return;
    this.running = true;
    this.selector = Selector.open();
    EventSuitePlugin.runAsync(new ProcessTask());
  }

  public void registerSubscriber(Endpoint endpoint, SocketChannel subscriber) {
    this.subscribers.insert(endpoint, subscriber);
  }

  private class ProcessTask implements Runnable {
    private void cleanup(ServerSocketChannel serverChannel) {
      channelToMessage.clear();
      try {
        selector.close();
        if (serverChannel != null)
          serverChannel.close();
      } catch (IOException ignored) {}
      running = false;
    }

    private StringNetBuffer createEventBuffer() {
      StringNetBuffer buffer = new StringNetBuffer().onNewMessage((channel, message) -> {
        EventMessage.Type type = EventMessage.type(message);
        if (type == EventMessage.Type.PUBLISH) {
          EventMessage.PublishMessage pubMsg = EventMessage.toPublishMessage(message);
          if (pubMsg == null)
            return;
          List<SocketChannel> channels = subscribers.lookup(pubMsg.endpoint());
          ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
          for (SocketChannel c : channels) {
            try {
              c.write(buf);
            } catch (IOException ignored) {
              subscribers.remove(c);
              channelToMessage.remove(c);
            }
          }
        } else { // SUBSCRIBE
          Endpoint target = EventMessage.toSubscribeMessage(message);
          if (target == null)
            return;
          subscribers.insert(target, channel);
        }
      });
      return buffer;
    }

    @Override
    public void run() {
      ServerSocketChannel serverChannel = null;
      try {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(AnnounceClient.INET_ADDRESS);
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
      } catch (IOException e) {
        running = false;
        e.printStackTrace();
        return;
      }

      while (true) {
        if (!running) {
          this.cleanup(serverChannel);
          return;
        }

        selector.selectedKeys().clear();
        try {
          selector.select(7000);
        } catch (IOException ignored) {}
        for (SelectionKey key : selector.selectedKeys()) {
          if (key.isAcceptable()) {
            SocketChannel channel;
            try {
              channel = ((ServerSocketChannel) key.channel()).accept();
              channel.configureBlocking(false);
              channel.register(selector, SelectionKey.OP_READ);
              channelToMessage.put(channel, createEventBuffer());
            } catch (IOException ignored) { ignored.printStackTrace(); }
            continue;
          }

          SocketChannel clientChannel = (SocketChannel) key.channel();
          StringNetBuffer buffer = channelToMessage.get(clientChannel);
          int read = 0;
          try {
            read = clientChannel.read(buffer.buffer());
            buffer.parse(clientChannel);
          } catch (IOException e) {
            try {
              clientChannel.close();
            } catch (IOException ignored) {}
            key.cancel();
            channelToMessage.remove(clientChannel);
            continue;
          }

          if (read == -1) {
            channelToMessage.remove(clientChannel);
            key.cancel();
          }
        }
      }
    }
  }
}
