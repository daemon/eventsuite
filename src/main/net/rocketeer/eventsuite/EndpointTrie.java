package net.rocketeer.eventsuite;

import java.util.*;

// improper trie
public class EndpointTrie<T> {
  private Node<T> root;
  public EndpointTrie() {
    this.root = new Node<T>("");
  }

  public void insert(Endpoint endpoint, T data) {
    Node<T> endNode = additiveTraverse(endpoint, data, root, 0);
    endNode.data = Optional.of(data);
  }

  private Node<T> additiveTraverse(Endpoint endpoint, T data, Node<T> node, int i) {
    if (i >= endpoint.tokens().length)
      return node;
    String tok = endpoint.tokens()[i];
    if (!node.children.containsKey(tok))
      node.children.put(tok, new Node<T>(tok));
    Node<T> n = node.children.get(tok);
    return additiveTraverse(endpoint, data, n, i + 1);
  }

  public List<T> lookup(Endpoint endpoint) {
    List<T> list = new LinkedList<>();
    lookup(endpoint, root, 0, list);
  }

  private void lookup(Endpoint endpoint, Node<T> root, int i, List<T> list) {
    if (i >= endpoint.tokens().length)
      return;
    String tok = endpoint.tokens()[i];
    for (Node<T> child : root.children.values()) {
      if (child.data.isPresent())
        list.add(child.data.get());
      lookup(endpoint, child, i + 1, list);
    }
  }

  private static class Node<T> {
    private final String word;
    private Map<String, Node<T>> children = new HashMap<>();
    private Optional<T> data = Optional.empty();
    public Node(String word) {
      this.word = word;
    }

    public Optional<T> data() {
      return this.data;
    }
  }
}
