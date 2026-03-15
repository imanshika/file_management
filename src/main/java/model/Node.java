package model;

public abstract class Node {

    protected String name;
    protected Node parent;
    protected final NodeType nodeType;

    protected Node(String name, Node parent, NodeType nodeType) {
        validateName(name);
        this.name = name;
        this.parent = parent;
        this.nodeType = nodeType;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    protected abstract void validateName(String name);

}

