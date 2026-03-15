package service;

import model.DirectoryNode;
import model.FileNode;
import model.Node;
import model.NodeType;

import java.util.ArrayList;
import java.util.List;

public class FileManagementService {

    private final DirectoryNode rootNode;
    private DirectoryNode currentNode;

    public FileManagementService() {
        this.rootNode = new DirectoryNode("/", null);
        currentNode = rootNode;
    }

    public DirectoryNode mkdir(String directoryPath){
        DirectoryNode node = startingNode(directoryPath);

        for(String segment : splitPath(directoryPath, node)){
            if(segment.isEmpty() || segment.equals(".")) continue;
            if(segment.equals("..")){
                if(node.getParent() != null) node = (DirectoryNode) node.getParent();
                continue;
            }
            Node existing = node.getChild(segment);
            if(existing != null && existing.getNodeType() != NodeType.DIRECTORY){
                throw new IllegalArgumentException("Path conflict: '" + segment + "' exists as a file");
            }
            DirectoryNode child = (DirectoryNode) existing;
            if(child == null) child = new DirectoryNode(segment, node);
            node.addChild(child);
            node = child;
        }

        return node;
    }

    public DirectoryNode cd(String directoryPath){
        DirectoryNode node = resolvePath(directoryPath);
        if(node == null) throw new IllegalArgumentException("Path does not exist: " + directoryPath);
        currentNode = node;
        return node;
    }

    public DirectoryNode resolvePath(String path){
        DirectoryNode node = startingNode(path);

        for(String segment : splitPath(path, node)){
            if(segment.isEmpty() || segment.equals(".")) continue;
            if(segment.equals("..")){
                if(node.getParent() != null) node = (DirectoryNode) node.getParent();
                continue;
            }
            Node child = node.getChild(segment);
            if(child == null || child.getNodeType() != NodeType.DIRECTORY) return null;
            node = (DirectoryNode) child;
        }

        return node;
    }

    public String pwd(){
        if(currentNode == rootNode) return "/";

        StringBuilder path = new StringBuilder();
        DirectoryNode node = currentNode;
        while(node != null && node != rootNode){
            path.insert(0, "/" + node.getName());
            node = (DirectoryNode) node.getParent();
        }
        return path.toString();
    }

    public FileNode addFile(String filePath, String content){
        DirectoryNode directoryNode = resolveParent(filePath);
        String fileName = extractLastName(filePath);

        Node existing = directoryNode.getChild(fileName);
        if(existing != null) throw new IllegalArgumentException("File already exists: " + filePath);

        FileNode fileNode = new FileNode(fileName, directoryNode);
        fileNode.setContent(content);
        directoryNode.addChild(fileNode);
        return fileNode;
    }

    public List<String> ls(String path){
        DirectoryNode node = resolvePath(path);
        if(node == null) throw new IllegalArgumentException("Directory does not exist: " + path);

        List<String> entries = new ArrayList<>();
        for(Node child : node.getChildren().values()){
            entries.add(child.getName());
        }
        return entries;
    }

    public String readFile(String filePath){
        DirectoryNode directoryNode = resolveParent(filePath);
        String fileName = extractLastName(filePath);

        Node fileNode = directoryNode.getChild(fileName);
        if(fileNode == null || fileNode.getNodeType() != NodeType.FILE){
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
        return ((FileNode) fileNode).getContent();
    }

    public void rm(String path){
        rm(path, false);
    }

    public void rm(String path, boolean recursive){

        if(path.equals("/")) throw new IllegalArgumentException("Cannot delete Root Directory");

        DirectoryNode parentNode = resolveParent(path);
        String nodeName = extractLastName(path);
        Node child = parentNode.getChild(nodeName);

        if(child == null){
            throw new IllegalArgumentException("File/Directory does not exist: " + path);
        }

        if(isAncestor(child, currentNode)){
            throw new IllegalArgumentException("Cannot delete: " + path + ". Currently in use");
        }

        if(!recursive && child.getNodeType() == NodeType.DIRECTORY && ((DirectoryNode) child).hasChildren()){
            throw new IllegalArgumentException("Cannot delete: " + path + ". Directory is not empty");
        }

        if(recursive && child.getNodeType() == NodeType.DIRECTORY){
            removeRecursive((DirectoryNode) child);
        }

        parentNode.removeChild(nodeName);
    }

    public List<String> find(String basePath, String name){
        List<String> nodeList = new ArrayList<>();
        DirectoryNode directoryNode = resolvePath(basePath);
        if(directoryNode == null) throw new IllegalArgumentException("Invalid basePath: " + basePath);
        String currentPath = getAbsolutePath(directoryNode);
        findByDFS(directoryNode, currentPath, name, nodeList);
        return nodeList;
    }

    public void mv(String source, String dest){

        if(source.equals("/")) throw new IllegalArgumentException("Cannot move root node");

        DirectoryNode sourceParent = resolveParent(source);
        String sourceName = extractLastName(source);
        Node sourceNode = sourceParent.getChild(sourceName);

        if(sourceNode == null){
            throw new IllegalArgumentException("Source does not exists: " + source);
        }

        if(isAncestor(sourceNode, currentNode)){
            throw new IllegalArgumentException("Cannot move Source is currently in use");
        }

        DirectoryNode destParent;
        String destName;
        DirectoryNode existingDir = resolvePath(dest);
        if(existingDir != null){
            //dest is existing directory → move inside it
            destParent = existingDir;
            destName = sourceName;
        } else {
            // dest doesn't exist → rename/move to dest's parent
            destParent = resolveParent(dest);  // throws if parent missing
            destName = extractLastName(dest);
        }

        //Cycle detection (directory into itself)
        if(sourceNode.getNodeType() == NodeType.DIRECTORY && isAncestor(sourceNode, destParent)) {
            throw  new IllegalArgumentException("Source is Ancestor of Destination");
        }

        //Duplicate check
        if(destParent.getChild(destName) != null) throw new IllegalArgumentException("Source File or Directory Already exist in destination");



        sourceParent.removeChild(sourceName);
        sourceNode.setName(destName);
        sourceNode.setParent(destParent);
        destParent.addChild(sourceNode);
    }

    private void findByDFS(Node node, String currentPath, String name, List<String> nodeList){

        if(node.getName().equals(name)){
            nodeList.add(currentPath);
        }

        if(node.getNodeType() == NodeType.DIRECTORY){
            for(Node child : ((DirectoryNode) node).getChildren().values()){
                String childPath = currentPath.equals("/") ? "/" + child.getName() : currentPath + "/" + child.getName();
                findByDFS(child, childPath, name, nodeList);
            }
        }
    }

    private void removeRecursive(DirectoryNode dir){
        for(Node child : new ArrayList<>(dir.getChildren().values())){
            if(child.getNodeType() == NodeType.DIRECTORY){
                removeRecursive((DirectoryNode) child);
            }
            dir.removeChild(child.getName());
        }
    }

    // Return true if Node a is ancestor of Node b
    private boolean isAncestor(Node a, Node b){
        while(b != null){
            if(b.equals(a)) return true;
            b = b.getParent();
        }
        return false;
    }

    private String getAbsolutePath(Node node){
        if(node == rootNode) return "/";
        StringBuilder absolutePath = new StringBuilder();
        while(node != null && node != rootNode){
            absolutePath.insert(0, "/" + node.getName());
            node = node.getParent();
        }
        return absolutePath.toString();
    }

    private DirectoryNode resolveParent(String filePath){
        String parentPath = extractParentPath(filePath);
        DirectoryNode parent = resolvePath(parentPath);
        if(parent == null) throw new IllegalArgumentException("Directory does not exist: " + parentPath);
        return parent;
    }
    
    private String extractLastName(String filePath){
        int lastSlash = filePath.lastIndexOf("/");
        return lastSlash == -1 ? filePath : filePath.substring(lastSlash + 1);
    }

    private String extractParentPath(String path){
        int lastSlash = path.lastIndexOf("/");
        return (lastSlash == -1) ? "." : (lastSlash == 0 ? "/" : path.substring(0, lastSlash));
    }

    private DirectoryNode startingNode(String path){
        return path.startsWith("/") ? rootNode : currentNode;
    }

    private String[] splitPath(String path, DirectoryNode startNode){
        return startNode == rootNode ? path.substring(1).split("/") : path.split("/");
    }
}

