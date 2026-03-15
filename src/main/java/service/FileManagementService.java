package service;

import model.DirectoryNode;

public class FileManagementService {

    DirectoryNode rootNode;
    DirectoryNode currentNode;

    public FileManagementService() {
        this.rootNode = new DirectoryNode("/", null);
        currentNode = rootNode;
    }

    public DirectoryNode mkdir(String directoryPath){

        DirectoryNode node = directoryPath.startsWith("/") ? rootNode : currentNode;

        String[] paths = node.equals(rootNode) ? directoryPath.substring(1).split("/") : directoryPath.split("/");

        for(String path : paths){
            DirectoryNode childNode = (DirectoryNode) node.getChild(path);
            if(childNode == null){
                childNode = new DirectoryNode(path, node);
            }
            node = childNode;
        }

        return node;
    }
}

