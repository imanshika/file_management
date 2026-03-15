package model;

public class FileNode extends Node {

    private StringBuilder content;

    public FileNode(String name, Node parent) {
        super(name, parent);
        this.content = new StringBuilder();
    }

    public void appendContent(String data) {
        content.append(data);
    }

    public String getContent() {
        return content.toString();
    }

    public void setContent(String data) {
        this.content = new StringBuilder(data);
    }

    @Override
    protected void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (name.contains("/")) {
            throw new IllegalArgumentException("File name cannot contain '/'");
        }
    }
}
