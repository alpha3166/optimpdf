package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

public class MainTest {
  Main sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    sut = new Main();
    cmd = new CommandLine(sut);
    out = new StringWriter();
    cmd.setOut(new PrintWriter(out));
    err = new StringWriter();
    cmd.setErr(new PrintWriter(err));
  }

  @Test
  void testNoArguments() throws Exception {
    // Exercise
    var exitCode = cmd.execute();
    // Verify
    assertEquals(0, exitCode);
    assertEquals("", out.toString());
    assertEquals("", err.toString());
  }

  @Test
  void testShortHelpOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-h");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().startsWith("Usage: optimpdf "));
    assertEquals("", err.toString());
  }

  @Test
  void testLongHelpOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--help");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().startsWith("Usage: optimpdf "));
    assertEquals("", err.toString());
  }

  @Test
  void testShortVersionOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-V");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().matches("OptimPDF \\d+\\.\\d+\\.\\d+\\n"));
    assertEquals("", err.toString());
  }

  @Test
  void testLongVersionOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--version");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().matches("OptimPDF \\d+\\.\\d+\\.\\d+\\n"));
    assertEquals("", err.toString());
  }

  @Test
  void testUnknownOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--unknown");
    // Verify
    assertEquals(2, exitCode);
    assertEquals("", out.toString());
    assertTrue(err.toString().startsWith("Unknown option: '--unknown'"));
  }

  @Test
  void testHelpSubcommand() throws Exception {
    // Exercise
    var exitCode = cmd.execute("help");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().startsWith("Usage: optimpdf "));
    assertEquals("", err.toString());
  }

  @Test
  void testUnknownSubcommand() throws Exception {
    // Exercise
    var exitCode = cmd.execute("unknown");
    // Verify
    assertEquals(2, exitCode);
    assertEquals("", out.toString());
    assertTrue(err.toString().startsWith("Unmatched argument at index 0: 'unknown'"));
  }
}
