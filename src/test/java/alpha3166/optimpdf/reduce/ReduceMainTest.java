package alpha3166.optimpdf.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class ReduceMainTest {
  List<String> logs = LogAppender.logs;
  Path base;
  ReduceMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    logs.clear();
    base = DataManager.makeTestDir();
    sut = new ReduceMain();
    cmd = new CommandLine(sut);
    out = new StringWriter();
    cmd.setOut(new PrintWriter(out));
    err = new StringWriter();
    cmd.setErr(new PrintWriter(err));
  }

  @AfterEach
  void tearDown() throws Exception {
    DataManager.removeDir(base);
  }

  @Test
  void test() throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg();
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    // Exercise
    var exitCode = cmd.execute(base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(Files.isRegularFile(base.resolve("src_r.pdf")));
    assertEquals(3, logs.size());
    assertEquals(base + "/src.pdf", logs.get(0));
    assertTrue(logs.get(1).matches("  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%"));
    assertEquals("  -> " + base + "/src_r.pdf", logs.get(2));
  }

  @Test
  void test_Abort() throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg();
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    // Exercise
    var exitCode = cmd.execute("-l", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    assertFalse(Files.exists(base.resolve("src_r.pdf")));
    assertEquals(1, logs.size());
    assertEquals(base + "/src.pdf -> " + base + "/src_r.pdf", logs.get(0));
  }

  @Test
  void test_DryRun() throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg();
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    // Exercise
    var exitCode = cmd.execute("-n", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    assertFalse(Files.exists(base.resolve("src_r.pdf")));
    assertEquals(3, logs.size());
    assertEquals(base + "/src.pdf", logs.get(0));
    assertTrue(logs.get(1).matches("  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%"));
    assertEquals("  -> " + base + "/src_r.pdf", logs.get(2));
  }

  @Test
  void test_Help() throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg();
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    // Exercise
    var exitCode = cmd.execute("-h", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("Usage:", out.toString().substring(0, 6));
    assertEquals(0, err.getBuffer().length());
    assertFalse(Files.exists(base.resolve("src_r.pdf")));
    assertEquals(0, logs.size());
  }

  @Test
  void test_UnknownOption() throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg();
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    // Exercise
    var exitCode = cmd.execute("-X", base + "/src.pdf");
    // Verify
    assertEquals(2, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("Unknown option: '-X'", err.toString().substring(0, 20));
    assertFalse(Files.exists(base.resolve("src_r.pdf")));
    assertEquals(0, logs.size());
  }
}
