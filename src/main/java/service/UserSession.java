package service;

import model.DirectoryNode;
import model.FileNode;

import java.util.List;

public class UserSession {

    private final String userId;
    private final FileSystem fileSystem;
    private DirectoryNode currentNode;

    public UserSession(String userId, FileSystem fileSystem) {
        this.userId = userId;
        this.fileSystem = fileSystem;
        this.currentNode = fileSystem.getRoot();
        currentNode.readLock().lock();
    }

    public String getUserId() {
        return userId;
    }

    public DirectoryNode cd(String directoryPath){
        DirectoryNode node = fileSystem.resolvePath(directoryPath, currentNode);
        if(node == null) throw new IllegalArgumentException("Path does not exist: " + directoryPath);
        node.readLock().lock();
        currentNode.readLock().unlock();
        currentNode = node;
        return node;
    }

    public String pwd(){
        return fileSystem.getAbsolutePath(currentNode);
    }

    public DirectoryNode mkdir(String path){
        return fileSystem.mkdir(path, currentNode);
    }

    public FileNode addFile(String filePath, String content){
        return fileSystem.addFile(filePath, content, currentNode);
    }

    public String readFile(String filePath){
        return fileSystem.readFile(filePath, currentNode);
    }

    public List<String> ls(String path){
        return fileSystem.ls(path, currentNode);
    }

    public void rm(String path){
        fileSystem.rm(path, currentNode);
    }

    public void rm(String path, boolean recursive){
        fileSystem.rm(path, recursive, currentNode);
    }

    public List<String> find(String basePath, String name){
        return fileSystem.find(basePath, name, currentNode);
    }

    public void mv(String source, String dest){
        fileSystem.mv(source, dest, currentNode);
    }

    public void logout(){
        currentNode.readLock().unlock();
    }
}
