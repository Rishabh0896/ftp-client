# FTP Client Project

This project implements a robust, extensible command-line FTP client in Java, capable of performing a wide range of FTP operations including listing directories, copying files, moving files, and creating/deleting directories on remote FTP servers.

## High-Level Approach

I designed this FTP client using a variant of the command pattern, emphasizing modularity and ease of extension:

1. `Main`: Serves as the entry point, handling command-line argument parsing and orchestrating FTP command execution.
2. `FTPClient`: Implements core FTP functionality, managing server connections and executing FTP commands.
3. `FTPCommand` and `FTPExecutor`: Establishes the foundation for my command pattern implementation. The Executor manages the entire connection lifecycle centrally.
4. `FTPPathHandler`: Specializes in parsing and handling FTP URLs and local paths.
5. `ParseArgs`: Efficiently parses and structures command-line arguments.

### Command Pattern Implementation

My approach to the command pattern allows for easy maintenance and  extensibility:

- FTP operations are encapsulated as methods within the `FTPClient` class.
- A `BiFunction` HashMap in the `Main` class maps command strings to their corresponding operations.
- To add a new command, one only needs to:
    1. Implement the operation in the `FTPClient` class.
    2. Add a single entry to the `BiFunction` HashMap in `Main`.

This streamlined process automatically handles connections and error management for any new command, significantly reducing the complexity of extending the client's functionality.

## Challenges and Solutions

### Testing Strategy: Record-Replay Approach

One of my most significant challenges was developing an efficient testing methodology for the FTP client. My solution is an innovative record-replay approach, implemented in the `FTPCommandTest` class.

#### How it Works:

1. **Recording Mode**:
    - Connects to an actual FTP server and executes a comprehensive series of FTP commands.
    - Responses are recorded and serialized to `ftp_test_cases.dat`.

2. **Replay Mode**:
    - Deserializes previously recorded responses.
    - Executes tests by comparing recorded responses against expected results.

3. **FTPRecorder Class**:
    - Manages the recording and replaying of test cases.
    - Provides persistent storage of test data.

This approach offers two advantages:
- Consistent test environment without relying on a live FTP server.
- Comprehensive coverage of various scenarios, including error cases.

## Future Improvements

- Developing a mock FTP server that can simulate various server behaviors using the recorded interaction data.
- Investigating the integration of existing Java libraries to improve my record-replay mechanism.
- Implementing a hybrid approach that combines my record-replay tests with live integration tests for more robust coverage.
