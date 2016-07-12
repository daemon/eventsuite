package net.rocketeer.eventsuite.bungeecord;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.rocketeer.eventsuite.AnnounceClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class AnnounceServer {
  private boolean running = false;
  private Selector selector;
  private Map<SocketChannel, StringBuilder> channelToMessage = new HashMap<>();

  public synchronized void start() throws IOException {
    if (this.running)
      return;
    this.running = true;
    this.selector = Selector.open();
    EventSuitePlugin.runAsync(new ProcessTask());
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
    @Override
    public void run() {
      ByteBuffer buffer = ByteBuffer.allocate(256);
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
            SocketChannel channel = null;
            try {
              channel = ((ServerSocketChannel) key.channel()).accept();
              channel.configureBlocking(false);
              channel.register(selector, SelectionKey.OP_READ);
              channelToMessage.put(channel, new StringBuilder());
            } catch (IOException ignored) { ignored.printStackTrace(); }
            continue;
          }

          SocketChannel clientChannel = (SocketChannel) key.channel();
          StringBuilder builder = channelToMessage.get(clientChannel);
          int read = 0;
          try {
            read = clientChannel.read(buffer);
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
            continue;
          } else if (read == 0)
            continue;

          buffer.flip();
          while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            if (c == '\n') { // terminal
              ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', builder.toString())));
              builder.setLength(0);
            } else
              builder.append(c);
          }
          buffer.clear();
        }
      }
    }
  }
}
