package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void test_Help_Short() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-h");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("Usage:", out.toString().substring(0, 6));
    assertEquals(0, err.getBuffer().length());
  }

  @Test
  void test_Help() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--help");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("Usage:", out.toString().substring(0, 6));
    assertEquals(0, err.getBuffer().length());
  }

  @Test
  void test_Version_Short() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-V");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("OptimPDF ", out.toString().substring(0, 9));
    assertEquals(0, err.getBuffer().length());
  }

  @Test
  void test_Version() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--version");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("OptimPDF ", out.toString().substring(0, 9));
    assertEquals(0, err.getBuffer().length());
  }

  @Test
  void test_UnknownOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-X");
    // Verify
    assertEquals(2, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("Unknown option: '-X'", err.toString().substring(0, 20));
  }
}
