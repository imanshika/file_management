# In-Memory File System

A Java implementation of a Unix-like in-memory file system, built as a Low-Level Design (LLD) / Machine Coding exercise.

## Problem Statement

Design and implement an in-memory file system that organizes data in a hierarchical directory tree and exposes APIs to create, navigate, read, and list files and directories — all stored in memory with no real disk I/O.

## Architecture

```
model/
├── Node.java              # Abstract base class (name, parent, nodeType)
├── NodeType.java          # Enum: FILE, DIRECTORY
├── FileNode.java          # Leaf node with text content (StringBuilder)
└── DirectoryNode.java     # Internal node with children map (HashMap)

service/
└── FileManagementService.java   # All file system operations

org/example/
└── Main.java              # Demo / driver
```

### Class Design

```
        Node (abstract)
       /    \
 FileNode   DirectoryNode
```

- **`Node`** — holds `name`, `parent` reference, `nodeType`, and an abstract `validateName()` (template method pattern).
- **`FileNode`** — stores content via `StringBuilder`. Supports `getContent()`, `setContent()`, and `appendContent()`.
- **`DirectoryNode`** — stores children in a `HashMap<String, Node>`. Returns an unmodifiable view via `getChildren()`. Guards against type conflicts in `addChild()`.

## Supported Operations

| API | Description |
|---|---|
| `mkdir(path)` | Creates directories along the path, including intermediates (like `mkdir -p`) |
| `cd(path)` | Changes current working directory |
| `pwd()` | Returns absolute path of current working directory |
| `addFile(path, content)` | Creates a new file with the given content |
| `readFile(path)` | Returns content of the file at the given path |
| `ls(path)` | Lists all children (files and directories) of a directory |

### Path Support

- **Absolute paths**: `/a/b/c` — resolved from root
- **Relative paths**: `a/b/c` — resolved from current directory
- **`.`** — current directory
- **`..`** — parent directory

## Example Usage

```java
FileManagementService service = new FileManagementService();

service.mkdir("/a/b/c");
service.cd("/a/b/c");
service.pwd();                          // "/a/b/c"

service.addFile("/a/b/file.txt", "hello");
service.ls("/a/b");                     // ["file.txt", "c"]
service.readFile("/a/b/file.txt");      // "hello"
```

## Design Decisions

- **Template method for validation**: `Node` calls `validateName()` in the constructor; each subclass defines its own rules (e.g., root `/` is valid only for directories).
- **Unmodifiable children map**: `DirectoryNode.getChildren()` returns `Collections.unmodifiableMap()` to prevent external mutation.
- **Type conflict guard**: `addChild()` throws if you try to create a file where a directory exists (or vice versa), preventing silent overwrites.
- **Shared path helpers**: `resolveParent()` and `extractFileName()` eliminate duplicated parent-directory resolution logic across `addFile` and `readFile`.
- **Null-safe resolution**: `resolvePath()` returns `null` for non-existent paths; callers decide whether to throw or handle gracefully.

## Complexity

| Operation | Time | Space |
|---|---|---|
| `mkdir`, `cd`, `addFile`, `readFile` | O(k) | O(1) per call |
| `ls` | O(n) | O(n) |
| `pwd` | O(d) | O(d) |

Where `k` = path depth, `n` = number of children, `d` = depth of current directory.

## Build & Run

```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

Requires **Java 21+** and **Maven**.

## TODO — Next Extensions

- [ ] `rm(path)` — delete a file or empty directory
- [ ] `rm(path, recursive)` — recursive delete (like `rm -rf`)
- [ ] `find(basePath, name)` — search for files/directories by name
- [ ] `mv(src, dest)` — move or rename files/directories
- [ ] File permissions (read/write/execute)
- [ ] Symbolic links
