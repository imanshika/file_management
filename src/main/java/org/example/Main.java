package org.example;

import model.DirectoryNode;
import model.FileNode;
import service.FileManagementService;

public class Main {
    public static void main(String[] args) {

        FileManagementService service = new FileManagementService();

        DirectoryNode dir = service.mkdir("/a/b/c");
        System.out.println("Created directory: " + dir.getName());

        //cd
        service.cd("/a/b/c");
        System.out.println("Changed directory to: " + service.pwd());


        //addFile("/a/b/file.txt", "hello")
        FileNode file = service.addFile("/a/b/file.txt", "hello");
        System.out.println("Added file: " + file.getName());

        //ls("/a/b")           -> ["file.txt"]
        System.out.println("ls(\"/a/b\"): " + service.ls("/a/b/"));

        //readFile("/a/b/file.txt") -> "hello"
        System.out.println("readFile(\"/a/b/file.txt\") : " + service.readFile("/a/b/file.txt"));

        //find file or directory
        System.out.println("Found File or Directory: " + service.find("/a/b", "file.txt"));
        System.out.println("Found File or Directory: " + service.find("/a/b", "c"));

        //rm file or directory
        service.rm("/a/b", false);
        System.out.println("Removed File or Directory non recursive");

        // rm recursively
        service.rm("/a/b", true);
        System.out.println("Removed File or Directory recursive");


    }
}