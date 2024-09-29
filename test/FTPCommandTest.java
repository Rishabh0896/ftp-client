import command.FTPExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FTPRecorder Class
 * This class implements a mechanism for recording and replaying FTP command test cases.
 * It allows for serialization and deserialization of test cases, enabling a record-replay
 * testing strategy for FTP operations.
 */

class FTPRecorder implements Serializable {
    private Map<String, TestCase> testCases = new HashMap<>();

    /**
     * TestCase Inner Class
     * Represents a single test case with the actual response, expected content,
     * and a flag indicating whether the expected content should be present in the response.
     */
    static class TestCase implements Serializable {
        String actualResponse;
        String expectedContent;
        boolean shouldContain;

        TestCase(String actualResponse, String expectedContent, boolean shouldContain) {
            this.actualResponse = actualResponse;
            this.expectedContent = expectedContent;
            this.shouldContain = shouldContain;
        }
    }

    /**
     * Records a new test case.
     *
     * @param testName The name of the test case
     * @param actualResponse The actual response from the FTP server
     * @param expectedContent The content expected in the response
     * @param shouldContain Whether the expected content should be present in the response
     */
    void recordTestCase(String testName, String actualResponse, String expectedContent, boolean shouldContain) {
        testCases.put(testName, new TestCase(actualResponse, expectedContent, shouldContain));
    }

    /**
     * Saves all recorded test cases to a file.
     *
     * @param filename The name of the file to save the test cases
     * @throws IOException If an I/O error occurs
     */
    void saveTestCases(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(testCases);
        }
    }

    /**
     * Loads test cases from a file.
     *
     * @param filename The name of the file to load the test cases from
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If the class of a serialized object cannot be found
     */
    void loadTestCases(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            testCases = (Map<String, TestCase>) ois.readObject();
        }
    }

    /**
     * Retrieves all recorded test cases.
     *
     * @return A Set of Map.Entry objects representing the test cases
     */
    Set<Map.Entry<String, TestCase>> getTestCases() {
        return testCases.entrySet();
    }
}

/**
 * FTPCommandTest Class
 *
 * This class implements a record-replay testing strategy for FTP commands.
 * It uses the FTPRecorder to either record new test cases or replay previously recorded ones.
 */
public class FTPCommandTest {
    private static final FTPRecorder recorder = new FTPRecorder();
    private static final boolean isRecordMode = false;
    private static final String TEST_DATA_FILE = "ftp_test_cases.dat";

    /**
     * Sets up the test environment.
     * If in record mode, it records new FTP responses.
     * Otherwise, it loads previously recorded test cases.
     */
    @BeforeAll
    static void setUp() throws IOException, ClassNotFoundException {
        if (isRecordMode) {
            recordFTPResponses();
        } else {
            recorder.loadTestCases(TEST_DATA_FILE);
        }
    }

    /**
     * Records FTP responses for various test cases.
     * This method is called when isRecordMode is true.
     * It tests various FTP operations like ls, mkdir, cp, mv, rm, and rmdir.
     */
    private static void recordFTPResponses() throws IOException {
        FTPExecutor executor = new FTPExecutor("ftp.4700.network", 21, "gupta.risha", "f60048066e1d153c5d1ecd1032a26369766ed8b32e1ef4b0466d553cbe8a77ef");
        // Test Cases for ls command
        recorder.recordTestCase("ls_root",executor.executeCommandCaptureLogs(client -> client.listFiles("/")), "hello.txt", true);
        recorder.recordTestCase("ls_nonexistent", executor.executeCommandCaptureLogs(client -> client.listFiles("/labradoodle")), "test", false);

        // Test Cases for mkdir command
        recorder.recordTestCase("mkdir_test", executor.executeCommandCaptureLogs(client -> client.createDirectory("/test")), "257 \"/test\" created", true);
        recorder.recordTestCase("mkdir_ls_test", executor.executeCommandCaptureLogs(client -> client.listFiles("/")), "test", true);
        recorder.recordTestCase("mkdir_invalid_directory_name", executor.executeCommandCaptureLogs(client -> client.createDirectory("/inv//lid_directory")), "550 Create directory operation failed.", true);
        recorder.recordTestCase("mkdir_invalid_path", executor.executeCommandCaptureLogs(client -> client.createDirectory("/pathNotPresent/new_folder")), "550 Create directory operation failed.", true);

        // Test Cases for cp command
        recorder.recordTestCase("cp_test_upload", executor.executeCommandCaptureLogs(client -> client.copyFile("/test/test.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", false)), "226 Transfer complete", true);
        recorder.recordTestCase("cp_invalid_remote_filepath", executor.executeCommandCaptureLogs(client -> client.copyFile("/pathNotPresent/gg.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", true)), "550 Failed to open file.", true);
        recorder.recordTestCase("cp_remove_local_copy", executor.executeCommandCaptureLogs(client -> client.deleteFile("/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", false)), "250 Delete operation successful.", false);
        recorder.recordTestCase("cp_test_download", executor.executeCommandCaptureLogs(client -> client.copyFile("/test/test.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", true)), "226 Transfer complete", true);
        recorder.recordTestCase("cp_download_file_already_exists", executor.executeCommandCaptureLogs(client -> client.copyFile("/test/test.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", true)), "226 Transfer complete.", true);

        // Test for mv command
        recorder.recordTestCase("mv_delete_remote_file", executor.executeCommandCaptureLogs(client -> client.deleteFile("/test/test.txt", true)), "250 Delete operation successful.", true);
        recorder.recordTestCase("mv_test_upload", executor.executeCommandCaptureLogs(client -> client.moveFile("/test/test.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", false)), "226 Transfer complete", true);

        recorder.recordTestCase("mv_invalid_remote_filepath", executor.executeCommandCaptureLogs(client -> client.copyFile("/pathNotPresent/gg.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", true)), "550 Failed to open file.", true);
        recorder.recordTestCase("mv_remove_local_copy", executor.executeCommandCaptureLogs(client -> client.deleteFile("/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", false)), "250 Delete operation successful.", false);
        recorder.recordTestCase("mv_test_download", executor.executeCommandCaptureLogs(client -> client.copyFile("/test/test.txt", "/Users/rishggup/Documents/Coursework/CS5700_Networks/Project_2/test/test.txt", true)), "226 Transfer complete", true);


        // Test Case for rm command
        recorder.recordTestCase("rm_test", executor.executeCommandCaptureLogs(client -> client.deleteFile("/test/test.txt", true)), "250 Delete operation successful.", true);
        recorder.recordTestCase("rm_file_not_found", executor.executeCommandCaptureLogs(client -> client.deleteFile("/test/test.txt", true)), "550 Delete operation failed.", true);



        // Test Cases for rmdir command
        recorder.recordTestCase("rmdir_test", executor.executeCommandCaptureLogs(client -> client.deleteDirectory("/test")), "250 Remove directory operation successful.", true);
        recorder.recordTestCase("rmdir_ls_test", executor.executeCommandCaptureLogs(client -> client.deleteDirectory("/test")), "test", false);
        recorder.recordTestCase("rmdir_invalid_directory_name", executor.executeCommandCaptureLogs(client -> client.deleteDirectory("/inv//lid_directory")), "550 Remove directory operation failed.", true);
        recorder.recordTestCase("mkdir_invalid_path", executor.executeCommandCaptureLogs(client -> client.deleteDirectory("/pathNotPresent/new_folder")), "550 Remove directory operation failed.", true);

        recorder.saveTestCases(TEST_DATA_FILE);
    }

    /**
     * Generates dynamic tests based on the recorded or loaded test cases.
     * Each test case is converted into a DynamicTest.
     *
     * @return A Stream of DynamicTest objects
     */
    @TestFactory
    Stream<DynamicTest> testFTPCommands() {
        return recorder.getTestCases().stream()
                .map(entry -> DynamicTest.dynamicTest("Test " + entry.getKey(), () -> {
                    FTPRecorder.TestCase testCase = entry.getValue();
                    if (testCase.shouldContain) {
                        assertTrue(testCase.actualResponse.contains(testCase.expectedContent),
                                "Expected response to contain: " + testCase.expectedContent + "\n Actual response: " + testCase.actualResponse);
                    } else {
                        assertFalse(testCase.actualResponse.contains(testCase.expectedContent),
                                "Expected response not to contain: " + testCase.expectedContent + "\n Actual response: " + testCase.actualResponse);
                    }
                }));
    }
}