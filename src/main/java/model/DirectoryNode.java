package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DirectoryNode  extends Node {

    private final Map<String, Node> children = new HashMap<>();

    public DirectoryNode(String name, Node parent) {
        super(name, parent, NodeType.DIRECTORY);
    }

    @Override
    protected void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Directory name cannot be null or empty");
        }
        if (name.equals("/")) return;
        if (name.contains("/") || name.contains("*") || name.contains("?")) {
            throw new IllegalArgumentException("Directory name contains invalid characters");
        }
    }

    public boolean hasChildren(){
        return !children.isEmpty();
    }

    public Map<String, Node> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    public Node getChild(String childName) {
        return children.get(childName);
    }

    public void addChild(Node child) {
        Node existing = children.get(child.getName());
        if (existing != null && existing.getNodeType() != child.getNodeType()) {
            throw new IllegalArgumentException(
                "Cannot create " + child.getNodeType() + " '" + child.getName()
                + "': a " + existing.getNodeType() + " with that name already exists");
        }
        children.put(child.getName(), child);
    }

    public void removeChild(String childName) {
        children.remove(childName);
    }
}
