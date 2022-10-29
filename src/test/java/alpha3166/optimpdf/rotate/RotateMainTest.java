package alpha3166.optimpdf.rotate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class RotateMainTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;
  RotateMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void beforeEach() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

    base = DataManager.makeTestDir();
    sut = new RotateMain();
    cmd = new CommandLine(sut);
    out = new StringWriter();
    cmd.setOut(new PrintWriter(out));
    err = new StringWriter();
    cmd.setErr(new PrintWriter(err));
  }

  @AfterEach
  void afterEach() throws Exception {
    DataManager.removeDir(base);

    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.detachAppender(mockAppender);
  }

  //
  // simple normal cases
  //

  @Test
  void testHelp() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--help");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().startsWith("Usage: "));
  }

  @Test
  void testDegreeDefault() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    // Exercise
    var exitCode = cmd.execute("--force", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(0, resultPdf.getPage(2).getRotation());
      assertEquals(0, resultPdf.getPage(3).getRotation());
      assertEquals(0, resultPdf.getPage(4).getRotation());
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  p2: 90 -> 0",
        "  p3: 180 -> 0",
        "  p4: 270 -> 0",
        "  -> " + base + "/src.pdf");
  }

  @Test
  void testDegree0() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    // Exercise
    var exitCode = cmd.execute("--degree", "0", "--force", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(0, resultPdf.getPage(2).getRotation());
      assertEquals(0, resultPdf.getPage(3).getRotation());
      assertEquals(0, resultPdf.getPage(4).getRotation());
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  p2: 90 -> 0",
        "  p3: 180 -> 0",
        "  p4: 270 -> 0",
        "  -> " + base + "/src.pdf");
  }

  @Test
  void testDegree90() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    // Exercise
    var exitCode = cmd.execute("--degree", "90", "--force", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      assertEquals(90, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(90, resultPdf.getPage(3).getRotation());
      assertEquals(90, resultPdf.getPage(4).getRotation());
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  p1: 0 -> 90",
        "  p3: 180 -> 90",
        "  p4: 270 -> 90",
        "  -> " + base + "/src.pdf");
  }

  @Test
  void testDegree180() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    // Exercise
    var exitCode = cmd.execute("--degree", "180", "--force", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      assertEquals(180, resultPdf.getPage(1).getRotation());
      assertEquals(180, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(180, resultPdf.getPage(4).getRotation());
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  p1: 0 -> 180",
        "  p2: 90 -> 180",
        "  p4: 270 -> 180",
        "  -> " + base + "/src.pdf");
  }

  @Test
  void testDegree270() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    // Exercise
    var exitCode = cmd.execute("--degree", "270", "--force", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      assertEquals(270, resultPdf.getPage(1).getRotation());
      assertEquals(270, resultPdf.getPage(2).getRotation());
      assertEquals(270, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  p1: 0 -> 270",
        "  p2: 90 -> 270",
        "  p3: 180 -> 270",
        "  -> " + base + "/src.pdf");
  }

  @Test
  void testRefPdf() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("target.pdf"), 90, 180, 270, 0);
    // Exercise
    var exitCode = cmd.execute("--ref-pdf", base + "/ref.pdf", "--force", base + "/target.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/target.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
    assertLogMatches(Level.INFO,
        base + "/target.pdf",
        "  p1: 90 -> 0",
        "  p2: 180 -> 90",
        "  p3: 270 -> 180",
        "  p4: 0 -> 270",
        "  -> " + base + "/target.pdf");
  }

  //
  // complex normal cases
  //

  @Test
  void testContentsNotUpdated() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("target.pdf"), 0, 90, 180, 270);
    var originalTargetTimestamp = Files.getLastModifiedTime(base.resolve("target.pdf"));
    Thread.sleep(1000); // to be sure the newer is newer on any file system
    // Exercise
    var exitCode = cmd.execute("--ref-pdf", base + "/ref.pdf", "--force", base + "/target.pdf");
    // Verify
    assertEquals(0, exitCode);
    var currentTargetTimestamp = Files.getLastModifiedTime(base.resolve("target.pdf"));
    assertEquals(originalTargetTimestamp, currentTargetTimestamp);
  }

  //
  // error cases
  //

  @Test
  void testInvalidDegree() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0);
    // Exercise
    var exitCode = cmd.execute("--degree", "1", "--force", base + "/src.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals("java.lang.IllegalArgumentException: --degree must be 0, 90, 180, or 270",
        err.toString().split("\\n")[0]);
  }

  @Test
  void testRefPdfNotExist() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("target.pdf"), 90, 180, 270, 0);
    // Exercise
    var exitCode = cmd.execute("--ref-pdf", base + "/ref.pdf", "--force", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.NoSuchFileException: %s/ref.pdf", base), err.toString().split("\\n")[0]);
  }

  @Test
  void testRefPdfIsDir() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("target.pdf"), 90, 180, 270, 0);
    Files.createDirectory(base.resolve("dir1"));
    // Exercise
    var exitCode = cmd.execute("--ref-pdf", base + "/dir1", "--force", base + "/target.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals(String.format("java.nio.file.NoSuchFileException: %s/dir1", base), err.toString().split("\\n")[0]);
  }

  @Test
  void testRefPdfAndTargetPdfAreSame() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    // Exercise
    var exitCode = cmd.execute("--ref-pdf", base + "/ref.pdf", "--force", base + "/ref.pdf");
    // Verify
    assertEquals(1, exitCode);
    assertEquals("java.lang.IllegalArgumentException: Reference and target PDFs are the same",
        err.toString().split("\\n")[0]);
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
