package org.example;

import model.FileNode;
import service.FileSystem;
import service.UserSession;

public class Main {
    public static void main(String[] args) {

        FileSystem fs = new FileSystem();

        UserSession userA = new UserSession("userA", fs);
        UserSession userB = new UserSession("userB", fs);

        // User A creates directories and a file
        userA.mkdir("/a/b/c");
        userA.cd("/a/b/c");
        System.out.println("UserA pwd: " + userA.pwd());

        FileNode file = userA.addFile("/a/b/file.txt", "hello");
        System.out.println("UserA added file: " + file.getName());

        // User B can see User A's changes — shared tree
        System.out.println("UserB ls(\"/a/b\"): " + userB.ls("/a/b"));
        System.out.println("UserB readFile: " + userB.readFile("/a/b/file.txt"));

        // User B navigates independently
        userB.cd("/a");
        System.out.println("UserA pwd: " + userA.pwd());  // still /a/b/c
        System.out.println("UserB pwd: " + userB.pwd());  // /a

        // Find works across the shared tree
        System.out.println("UserB find: " + userB.find("/", "file.txt"));
    }
}
