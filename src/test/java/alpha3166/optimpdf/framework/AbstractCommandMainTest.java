package alpha3166.optimpdf.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class AbstractCommandMainTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;
  DummyMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

    base = DataManager.makeTestDir();

    sut = new DummyMain();
    cmd = new CommandLine(sut);
    out = new StringWriter();
    cmd.setOut(new PrintWriter(out));
    err = new StringWriter();
    cmd.setErr(new PrintWriter(err));
  }

  @AfterEach
  void tearDown() throws Exception {
    DataManager.removeDir(base);

    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.detachAppender(mockAppender);
  }

  //
  // simple normal cases
  //

  @Test
  void testOutfile() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--outfile", base + "/out.txt", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out.txt")).get(0));
  }

  @Test
  void testSuffix() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--suffix", "_new", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("a_new.txt")).get(0));
  }

  @Test
  void testOutdir() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createDirectory(base.resolve("out"));
    // Exercise
    var exitCode = cmd.execute("--outdir", base + "/out", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out/a.txt")).get(0));
  }

  @Test
  void testForce() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--force", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("a.txt")).get(0));
  }

  @Test
  void testUpdate() throws Exception {
    // SetUp
    Files.createFile(base.resolve("a.txt"));
    Files.createFile(base.resolve("b.txt"));
    Files.createFile(base.resolve("c.txt"));
    Files.createDirectory(base.resolve("out"));
    Files.createFile(base.resolve("out/a.txt"));
    Files.createFile(base.resolve("out/b.txt"));
    Thread.sleep(1000); // to be sure the newer is newer on any file system
    Files.writeString(base.resolve("a.txt"), "setup");
    Files.writeString(base.resolve("out/b.txt"), "setup");
    Files.writeString(base.resolve("c.txt"), "setup");
    // Exercise
    var exitCode = cmd.execute("--update", "--outdir", base + "/out", base + "/a.txt", base + "/b.txt",
        base + "/c.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out/a.txt")).get(0));
    assertEquals("setup", Files.readAllLines(base.resolve("out/b.txt")).get(0));
    assertEquals("done", Files.readAllLines(base.resolve("out/c.txt")).get(0));
  }

  @Test
  void testDryRun() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--dry-run", "--outfile", base + "/out.txt", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertFalse(Files.exists(base.resolve("/out.txt")));
  }

  @Test
  void testRecursive() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createDirectory(base.resolve("dir1"));
    Files.createFile(base.resolve("dir1/b.txt"));
    Files.createDirectory(base.resolve("dir1/dir2"));
    Files.createFile(base.resolve("dir1/dir2/c.txt"));
    Files.createDirectory(base.resolve("out"));
    // Exercise
    var exitCode = cmd.execute("--recursive", "--outdir", base + "/out", base + "/a.txt", base + "/dir1");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out/a.txt")).get(0));
    assertEquals("done", Files.readAllLines(base.resolve("out/b.txt")).get(0));
    assertEquals("done", Files.readAllLines(base.resolve("out/c.txt")).get(0));
  }

  @Test
  void testList() throws Exception {
    // Setup
    Files.createDirectory(base.resolve("dir1"));
    Files.createFile(base.resolve("dir1/a.txt"));
    Files.createDirectory(base.resolve("dir1/dir2"));
    Files.createFile(base.resolve("dir1/dir2/b.txt"));
    Files.createDirectory(base.resolve("out"));
    // Exercise
    var exitCode = cmd.execute("--list", "--recursive", "--outdir", base + "/out", base + "/dir1");
    // Verify
    assertEquals(0, exitCode);
    assertLogMatches(Level.INFO, //
        base + "/dir1/a.txt -> " + base + "/out/a.txt",
        base + "/dir1/dir2/b.txt -> " + base + "/out/b.txt");
    assertFalse(Files.exists(base.resolve("/out/a.txt")));
    assertFalse(Files.exists(base.resolve("/out/b.txt")));
  }

  @Test
  void testQuiet() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--quiet", "--outfile", base + "/out.txt", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out.txt")).get(0));
  }

  //
  // complex normal cases
  //

  @Test
  void testOutfileOutdir() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createDirectory(base.resolve("out"));
    // Exercise
    var exitCode = cmd.execute("--outfile", "out.txt", "--outdir", base + "/out", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out/out.txt")).get(0));
  }

  @Test
  void testSuffixOutdirForce() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createDirectory(base.resolve("out"));
    Files.createFile(base.resolve("out/a_new.txt"));
    // Exercise
    var exitCode = cmd.execute("--suffix", "_new", "--outdir", base + "/out", "--force", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out/a_new.txt")).get(0));
  }

  @Test
  void testInplaceAndContentsNotUpdated() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--process-result", "false", "--force", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals(0, Files.readAllLines(base.resolve("a.txt")).size());
  }

  //
  // special normal cases
  //

  @Test
  void testNoArguments() throws Exception {
    // Exercise
    var exitCode = cmd.execute();
    // Verify
    assertEquals(0, exitCode);
  }

  @Test
  void testNoSrcPaths() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--suffix", "_new");
    // Verify
    assertEquals(0, exitCode);
  }

  //
  // error cases
  //

  @Test
  void testSrcNotExist() throws Exception {
    // Exercise
    var exitCode = cmd.execute(base + "/a.txt");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.NoSuchFileException: %s/a.txt", base), err.toString().split("\\n")[0]);
  }

  @Test
  void testSrcIsDir() throws Exception {
    // Setup
    Files.createDirectory(base.resolve("dir1"));
    // Exercise
    var exitCode = cmd.execute(base + "/dir1");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.NoSuchFileException: %s/dir1", base), err.toString().split("\\n")[0]);
  }

  @Test
  void testDestAlreadyExists() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute(base + "/a.txt");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.FileAlreadyExistsException: %s/a.txt", base),
        err.toString().split("\\n")[0]);
  }

  @Test
  void testOutfileAlreadyExists() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createFile(base.resolve("out.txt"));
    // Exercise
    var exitCode = cmd.execute("--outfile", base + "/out.txt", base + "/a.txt");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.FileAlreadyExistsException: %s/out.txt", base),
        err.toString().split("\\n")[0]);
  }

  @Test
  void testOutfileForTwoSrcPaths() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createFile(base.resolve("b.txt"));
    // Exercise
    var exitCode = cmd.execute("--outfile", "out.txt", base + "/a.txt", base + "/b.txt");
    // Verify
    assertEquals(1, exitCode);
    assertEquals("java.lang.IllegalArgumentException: --outfile is for single input only",
        err.toString().split("\\n")[0]);
  }

  @Test
  void testOutdirNotExist() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--outdir", base + "/out", base + "/a.txt");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.NoSuchFileException: %s/out", base), err.toString().split("\\n")[0]);
  }

  @Test
  void testOutdirIsNotDir() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    Files.createFile(base.resolve("out"));
    // Exercise
    var exitCode = cmd.execute("--outdir", base + "/out", base + "/a.txt");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.NoSuchFileException: %s/out", base), err.toString().split("\\n")[0]);
  }

  //
  // whitebox tests
  //

  @Test
  void testAdjustFileMapIsCalled() throws Exception {
    // Setup
    Files.createDirectory(base.resolve("dir1"));
    Files.createFile(base.resolve("dir1/a.txt"));
    Files.createFile(base.resolve("dir1/b.jpg"));
    // Exercise
    var exitCode = cmd.execute("--force", "--recursive", base + "/dir1");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("dir1/a.txt")).get(0));
    assertEquals(0, Files.readAllLines(base.resolve("dir1/b.jpg")).size());
  }

  @Test
  void testHandleLocalOptionIsCalled() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--ext", "doc", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("a.doc")).get(0));
  }

  @Test
  void testProcessFileIsCalled() throws Exception {
    // Setup
    Files.createFile(base.resolve("a.txt"));
    // Exercise
    var exitCode = cmd.execute("--outfile", base + "/out.txt", base + "/a.txt");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("done", Files.readAllLines(base.resolve("out.txt")).get(0));
  }

  //
  // utilities
  //

  void assertLogMatches(Level expectedLevel, String... expectedMessages) {
    verify(mockAppender, times(expectedMessages.length)).doAppend(captor.capture());
    var expectedLines = new LinkedList<>(Arrays.asList(expectedMessages));
    for (var event : captor.getAllValues()) {
      assertEquals(expectedLevel, event.getLevel());
      assertTrue(event.getFormattedMessage().matches(expectedLines.removeFirst()));
    }
  }
}
