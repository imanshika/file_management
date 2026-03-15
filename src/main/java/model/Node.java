package model;

public abstract class Node {

    protected String name;
    protected Node parent;

    protected Node(String name, Node parent) {
        validateName(name);
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    protected abstract void validateName(String name);

}

