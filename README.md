## In-Memory File System – Problem Statement

You are asked to design and implement a **simple in-memory file system** as part of a Low-Level Design / Machine Coding interview round.

### High-Level Goal

Build a mini file system that:
- Organizes data in a **hierarchical directory tree** (folders and files).
- Exposes a small set of **APIs to create, navigate, and read** from this tree.
- Stores everything **in memory only** (no real disk I/O).

### Core Requirements

- **Root directory**
  - The file system starts with a single root directory `/`.

- **Path format**
  - Use **Unix-like paths**, e.g. `/`, `/a`, `/a/b`, `/a/b/file.txt`.
  - Paths are separated by `/`.
  - You may be asked to support:
    - **Absolute paths** (starting with `/`).
    - Optional: **relative paths**, `.` and `..` for current / parent directory.

- **Entities**
  - **Directory**
    - Can contain sub-directories and files.
    - Names are strings without `/`.
  - **File**
    - Has a name and textual content (string).
    - Lives inside a directory.

### Typical APIs to Implement

The exact names may vary per interviewer, but commonly you need to support some or all of:

- **`mkdir(String path)`**
  - Create a directory at `path`.
  - Automatically create intermediate directories if they do not exist (depending on spec).
  - If the directory already exists, usually it is a no-op or error (clarify with interviewer).

- **`ls(String path)`**
  - If `path` is a **file**, return a list with that single file name.
  - If `path` is a **directory**, return a **sorted list of names** of all children (files and directories).
  - If `path` is empty, list the contents of the current or root directory (depending on spec).

- **`addFile(String filePath, String content)`** (also seen as `addContentToFile`)
  - If the file does **not exist**:
    - Create it along the given path.
    - Set initial content to `content`.
  - If the file **already exists**:
    - **Append** `content` to the existing file (unless otherwise specified).

- **`readFile(String filePath)`** (also seen as `readContentFromFile`)
  - Return the full content of the file at `filePath`.
  - If `filePath` does not exist or refers to a directory, throw/return an error.

- **`cd(String path)`** (optional)
  - Change the current working directory to `path`.

- **`rm(String path)`** (optional)
  - Remove a file or (optionally) an empty directory.

### Data Modeling Expectations

You are expected to model the file system as an **in-memory tree**:
- A common approach is:
  - A base `Node` type with:
    - `name`
    - `parent`
  - `DirectoryNode`:
    - Extends `Node`
    - Holds a map of `children` (name → node).
  - `FileNode`:
    - Extends `Node`
    - Holds `content` (e.g. `StringBuilder` or `String`).

The interviewer is looking for:
- **Clean class design** (separation of file vs directory responsibilities).
- **Clear API signatures** and behavior.
- **Correct path parsing** and navigation.
- **Handling of edge cases** (non-existent paths, name clashes, file vs directory confusion).

### Complexity Expectations

Assume:
- `k` = number of components in the path (e.g. `/a/b/c` → 3),
- Lookups inside a directory are `O(1)` using a hash map.

Then:
- `mkdir`, `cd`, `addFile`, `readFile`, `ls` should be approximately **O(k)** time.
- Space usage is proportional to the number of directories, files, and total content stored.

### Example Usage Scenario

```text
mkdir("/a/b")
addFile("/a/b/file.txt", "hello")
ls("/a/b")           -> ["file.txt"]
readFile("/a/b/file.txt") -> "hello"
addFile("/a/b/file.txt", " world")
readFile("/a/b/file.txt") -> "hello world"
```

### What Interviewers Evaluate

- **Design skills**: how you structure classes and relationships.
- **Code clarity**: naming, modularity, readability.
- **Correctness**: behavior matches the specified API contract.
- **Edge cases**: empty paths, duplicate names, invalid paths.
- **Communication**: how you explain your choices and trade-offs while coding.

