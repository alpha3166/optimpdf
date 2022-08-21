package alpha3166.optimpdf.rotate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import picocli.CommandLine;

public class RotateMainTest {
  List<String> logs = LogAppender.logs;
  Path base;
  RotateMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  public void beforeEach() throws Exception {
    logs.clear();
    base = DataManager.makeTestDir();
    sut = new RotateMain();
    cmd = new CommandLine(sut);
    out = new StringWriter();
    cmd.setOut(new PrintWriter(out));
    err = new StringWriter();
    cmd.setErr(new PrintWriter(err));
  }

  @AfterEach
  public void afterEach() throws Exception {
    DataManager.removeDir(base);
  }

  @Test
  public void test_hOption() throws Exception {
    // Exercise & Verify
    assertDoesNotThrow(() -> cmd.execute("-h"));
  }

  @Test
  public void test_invalidOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-X", base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(2, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("Unknown option: '-X'", err.toString().substring(0, 20));
  }

  @Test
  public void test_onePdf() throws Exception {
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf");
    // Verify
    assertEquals(2, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("Missing required parameter: 'TARGET_PDF'", err.toString().split("\n")[0]);
  }

  @Test
  public void test_refPdfDoesNotExist() throws Exception {
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("java.nio.file.NoSuchFileException: " + base + "/ref.pdf", err.toString().split("\n")[0]);
  }

  @Test
  public void test_refPdfIsDir() throws Exception {
    // Setup
    Files.createDirectory(base.resolve("ref.pdf"));
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("java.nio.file.NoSuchFileException: " + base + "/ref.pdf", err.toString().split("\n")[0]);
  }

  @Test
  public void test_targetPdfDoesNotExist() throws Exception {
    // Setup
    Files.createFile(base.resolve("ref.pdf"));
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("java.nio.file.NoSuchFileException: " + base + "/target.pdf", err.toString().split("\n")[0]);
  }

  @Test
  public void test_targetPdfIsDir() throws Exception {
    // Setup
    Files.createFile(base.resolve("ref.pdf"));
    Files.createDirectory(base.resolve("target.pdf"));
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("java.nio.file.NoSuchFileException: " + base + "/target.pdf", err.toString().split("\n")[0]);
  }

  @Test
  public void test_refAndTargetPdfsAreSame() throws Exception {
    // Setup
    Files.createFile(base.resolve("ref.pdf"));
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf", base + "/./ref.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("java.lang.IllegalArgumentException: Reference and target PDFs are the same",
        err.toString().split("\n")[0]);
  }

  @Test
  public void test_numberOfPagesDiffer() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0);
    DataManager.generatePdf(base.resolve("target.pdf"), 0, 90);
    // Exercise
    var exitCode = cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("java.lang.RuntimeException: The number of pages differs: 1 vs 2", err.toString().split("\n")[0]);
  }

  @Test
  public void test_noDiff() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("target.pdf"), 0, 90, 180, 270);
    // Exercise
    cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, logs.size());
    assertEquals(base + "/target.pdf", logs.get(0));
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/target.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  public void test_allPagesDiffer() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("target.pdf"), 90, 180, 270, 0);
    // Exercise
    cmd.execute(base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(5, logs.size());
    assertEquals(base + "/target.pdf", logs.get(0));
    assertEquals("  p1: 90 -> 0", logs.get(1));
    assertEquals("  p2: 180 -> 90", logs.get(2));
    assertEquals("  p3: 270 -> 180", logs.get(3));
    assertEquals("  p4: 0 -> 270", logs.get(4));
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/target.pdf"))) {
      assertEquals(90, resultPdf.getPage(1).getRotation());
      assertEquals(180, resultPdf.getPage(2).getRotation());
      assertEquals(270, resultPdf.getPage(3).getRotation());
      assertEquals(0, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  public void test_fOptionWithNoDiff() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("target.pdf"), 0, 90, 180, 270);
    // Exercise
    cmd.execute("-f", base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(1, logs.size());
    assertEquals(base + "/target.pdf", logs.get(0));
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/target.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  public void test_fOptionWithAllPagesDiffer() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("target.pdf"), 90, 180, 270, 0);
    // Exercise
    cmd.execute("-f", base + "/ref.pdf", base + "/target.pdf");
    // Verify
    assertEquals(5, logs.size());
    assertEquals(base + "/target.pdf", logs.get(0));
    assertEquals("  p1: 90 -> 0", logs.get(1));
    assertEquals("  p2: 180 -> 90", logs.get(2));
    assertEquals("  p3: 270 -> 180", logs.get(3));
    assertEquals("  p4: 0 -> 270", logs.get(4));
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/target.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  public void test_fOptionWithAllPagesDiffer_relativePath() throws Exception {
    // Setup
    Path refPdf = Paths.get("junit_ref.pdf");
    Path targetPdf = Paths.get("junit_target.pdf");
    DataManager.generatePdf(refPdf, 0, 90, 180, 270);
    DataManager.generatePdf(targetPdf, 90, 180, 270, 0);
    // Exercise
    cmd.execute("-f", refPdf.toString(), targetPdf.toString());
    // Verify
    assertEquals(5, logs.size());
    assertEquals(targetPdf.toString(), logs.get(0));
    assertEquals("  p1: 90 -> 0", logs.get(1));
    assertEquals("  p2: 180 -> 90", logs.get(2));
    assertEquals("  p3: 270 -> 180", logs.get(3));
    assertEquals("  p4: 0 -> 270", logs.get(4));
    try (var resultPdf = new PdfDocument(new PdfReader(targetPdf.toString()))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
    // Teardown
    Files.delete(refPdf);
    Files.delete(targetPdf);
  }
}
