package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DirectoryNode  extends Node {

    private final Map<String, Node> children = new HashMap<>();

    public DirectoryNode(String name, Node parent) {
        super(name, parent);
    }

    @Override
    protected void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Directory name cannot be null or empty");
        }
        if (name.contains("/")) {
            throw new IllegalArgumentException("Directory name cannot contain '/'");
        }
    }

    public Map<String, Node> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    public Node getChild(String childName) {
        return children.get(childName);
    }

    public void addChild(Node child) {
        children.put(child.getName(), child);
    }

    public void removeChild(String childName) {
        children.remove(childName);
    }
}
