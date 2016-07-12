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
    endNode.data.add(data);
    if (endpoint.inclusive())
      endNode.inclusiveData.add(data);
  }

  public void remove(T data) {
    root.data.remove(data);
    root.inclusiveData.remove(data);
    remove(root, data);
  }

  private void remove(Node<T> node, T data) {
    for (Node<T> child : node.children.values()) {
      child.data.remove(data);
      child.inclusiveData.remove(data);
      remove(child, data);
    }
  }

  private Node<T> additiveTraverse(Endpoint endpoint, T data, Node<T> node, int i) {
    if (i >= endpoint.tokens().length)
      return node;
    String tok = endpoint.tokens()[i];
    if (!node.children.containsKey(tok))
      node.children.put(tok, new Node<>(tok));
    Node<T> n = node.children.get(tok);
    return additiveTraverse(endpoint, data, n, i + 1);
  }

  public List<T> lookup(Endpoint endpoint) {
    List<T> list = new LinkedList<>();
    Node<T> node = collectFind(endpoint, root, 0, list);
    if (node == null)
      return list;
    list.addAll(node.data);
    if (endpoint.inclusive())
      addSubtree(node, list);
    return list;
  }

  private void addSubtree(Node<T> node, List<T> list) {
    for (Node<T> child : node.children.values()) {
      list.addAll(child.data);
      addSubtree(child, list);
    }
  }

  private Node<T> collectFind(Endpoint endpoint, Node<T> node, int i, List<T> data) {
    if (i > endpoint.tokens().length)
      return null;
    else if (i == endpoint.tokens().length)
      return node;
    data.addAll(node.inclusiveData);
    Node<T> child = node.children.get(endpoint.tokens()[i]);
    if (child == null)
      return null;
    return collectFind(endpoint, child, i + 1, data);
  }

  private Node<T> find(Endpoint endpoint, Node<T> node, int i) {
    if (i > endpoint.tokens().length)
      return null;
    else if (i == endpoint.tokens().length)
      return node;
    Node<T> child = node.children.get(endpoint.tokens()[i]);
    if (child == null)
      return null;
    return find(endpoint, child, i + 1);
  }

  private static class Node<T> {
    private final String word;
    private Map<String, Node<T>> children = new HashMap<>();
    private Set<T> data = new HashSet<>();
    private Set<T> inclusiveData = new HashSet<>();
    public Node(String word) {
      this.word = word;
    }

    public Set<T> data() {
      return this.data;
    }
  }
}
