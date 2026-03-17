# In-Memory File System

A Java implementation of a Unix-like in-memory file system, built as a Low-Level Design (LLD) / Machine Coding exercise. Supports multi-user concurrent access with per-directory read-write locking.

## Problem Statement

Design and implement an in-memory file system that organizes data in a hierarchical directory tree and exposes APIs to create, navigate, read, and list files and directories — all stored in memory with no real disk I/O.

## Architecture

```
model/
├── Node.java              # Abstract base class (name, parent, nodeType)
├── NodeType.java          # Enum: FILE, DIRECTORY
├── FileNode.java          # Leaf node with text content (StringBuilder)
└── DirectoryNode.java     # Internal node with ConcurrentHashMap + ReentrantReadWriteLock

service/
├── FileSystem.java        # Shared file system — all tree operations (stateless per user)
└── UserSession.java       # Per-user session — holds currentNode, delegates to FileSystem

org/example/
└── Main.java              # Demo / driver
```

### Class Design

```
        Node (abstract)
       /    \
 FileNode   DirectoryNode
                 │
                 ├── ConcurrentHashMap<String, Node> children
                 └── ReentrantReadWriteLock rwLock


 FileSystem (shared singleton)         UserSession (per user)
 ┌──────────────────────────┐         ┌────────────────────────┐
 │ rootNode                 │◄────────│ fileSystem ref         │
 │ mkdir, addFile, rm, mv   │         │ currentNode + readLock │
 │ ls, readFile, find       │         │ cd, pwd, logout        │
 └──────────────────────────┘         └────────────────────────┘
```

- **`Node`** — holds `name`, `parent` reference, `nodeType`, and an abstract `validateName()` (template method pattern). Supports `setName()` and `setParent()` for move operations.
- **`FileNode`** — stores content via `StringBuilder`. Supports `getContent()`, `setContent()`, and `appendContent()`.
- **`DirectoryNode`** — stores children in a `ConcurrentHashMap<String, Node>`. Each directory has its own `ReentrantReadWriteLock` for concurrent access. Returns an unmodifiable view via `getChildren()`. Guards against type conflicts in `addChild()`.
- **`FileSystem`** — shared singleton that owns the root node and all tree operations. Every method accepts a `workingDir` parameter — no per-user state.
- **`UserSession`** — per-user session that tracks `currentNode` and holds a persistent read lock on it. Delegates all operations to `FileSystem`.

## Supported Operations

| API | Description |
|---|---|
| `mkdir(path)` | Creates directories along the path, including intermediates (like `mkdir -p`) |
| `cd(path)` | Changes current working directory |
| `pwd()` | Returns absolute path of current working directory |
| `addFile(path, content)` | Creates a new file with the given content |
| `readFile(path)` | Returns content of the file at the given path |
| `ls(path)` | Lists all children (files and directories) of a directory |
| `rm(path)` | Deletes a file or empty directory |
| `rm(path, recursive)` | Recursively deletes a directory and all contents (like `rm -rf`) |
| `find(basePath, name)` | Searches recursively for files/directories matching name |
| `mv(src, dest)` | Moves or renames a file/directory with cycle detection |
| `logout()` | Releases session lock and cleans up |

### Path Support

- **Absolute paths**: `/a/b/c` — resolved from root
- **Relative paths**: `a/b/c` — resolved from current directory
- **`.`** — current directory
- **`..`** — parent directory

## Example Usage

```java
FileSystem fs = new FileSystem();

UserSession userA = new UserSession("userA", fs);
UserSession userB = new UserSession("userB", fs);

// User A creates structure
userA.mkdir("/a/b/c");
userA.cd("/a/b/c");
System.out.println(userA.pwd());              // "/a/b/c"

userA.addFile("/a/b/file.txt", "hello");

// User B sees the same tree — shared file system
System.out.println(userB.ls("/a/b"));         // ["file.txt", "c"]
System.out.println(userB.readFile("/a/b/file.txt"));  // "hello"

// Independent navigation
userB.cd("/a");
System.out.println(userA.pwd());              // "/a/b/c"
System.out.println(userB.pwd());              // "/a"

// Search
System.out.println(userB.find("/", "file.txt"));  // ["/a/b/file.txt"]

// Move
userA.mv("/a/b/file.txt", "/a/renamed.txt");

// Cleanup
userA.logout();
userB.logout();
```

## Concurrency Model

### Per-Directory Read-Write Locks

Each `DirectoryNode` holds a `ReentrantReadWriteLock`. Multiple readers can proceed in parallel; writers get exclusive access.

| Operation | Lock Type | Locked Node |
|---|---|---|
| `ls(path)` | Read | target directory |
| `readFile(path)` | Read | parent directory |
| `resolvePath(path)` | Read (each segment) | each directory during traversal |
| `find(basePath, name)` | Read (DFS) | each directory visited |
| `mkdir(path)` | Write | each directory as children are created |
| `addFile(path)` | Write | parent directory |
| `rm(path)` | Write | parent directory (+ subtree for recursive) |
| `mv(src, dest)` | Write (both) | source parent + dest parent |

### Session Protection via Read Lock

`UserSession` holds a persistent **read lock** on its `currentNode`:
- `cd()` acquires read lock on the new directory, then releases on the old
- `rm()` needs a write lock — it blocks until all users have navigated away
- No session registry needed — the lock mechanism handles it

### Deadlock Prevention

`mv()` locks two directories (source parent + dest parent). To prevent deadlocks, locks are always acquired in **alphabetical order of absolute path**:

```java
// Thread 1: mv("/a/b", "/x/y") → locks /a then /x
// Thread 2: mv("/x/z", "/a/w") → locks /a then /x (same order)
// No circular wait possible
```

## Design Decisions

- **Shared FileSystem + per-user UserSession**: separates the shared tree from per-user navigation state, enabling multi-user access.
- **Template method for validation**: `Node` calls `validateName()` in the constructor; each subclass defines its own rules.
- **ConcurrentHashMap + RWLock**: ConcurrentHashMap provides safe concurrent reads as a safety net; the RWLock is the primary synchronization mechanism.
- **Consistent lock ordering**: prevents deadlocks when `mv` locks multiple directories.
- **Ancestor check for rm/mv**: prevents deleting or moving a directory the caller is currently inside.
- **Cycle detection for mv**: prevents moving a directory into its own subtree.

## Complexity

| Operation | Time | Space |
|---|---|---|
| `mkdir`, `cd`, `addFile`, `readFile` | O(k) | O(1) per call |
| `ls` | O(n) | O(n) |
| `pwd` | O(d) | O(d) |
| `rm(recursive)` | O(N) | O(N) |
| `find` | O(N) | O(N) |
| `mv` | O(k) | O(1) |

Where `k` = path depth, `n` = children count, `d` = current directory depth, `N` = total nodes in subtree.

## Build & Run

```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

Requires **Java 21+** and **Maven**.

## Possible Extensions

- [ ] `cp(src, dest)` — deep copy files/directories
- [ ] File permissions (read/write/execute per user)
- [ ] Symbolic links
- [ ] Disk persistence / serialization
