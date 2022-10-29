package alpha3166.optimpdf.unzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class UnzipMainTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;
  UnzipMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

    base = DataManager.makeTestDir();
    sut = new UnzipMain();
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
  void testHelp() throws Exception {
    // Exercise
    var exitCode = cmd.execute("--help");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(out.toString().startsWith("Usage: "));
  }

  @Test
  void testNoOptions() throws Exception {
    // Setup
    DataManager.generateZip(base.resolve("sample.zip"));
    // Exercise
    var exitCode = cmd.execute(base + "/sample.zip");
    // Verify
    assertEquals(0, exitCode);
    var pdfReader = new PdfReader(base.resolve("sample.pdf").toFile());
    var pdfDoc = new PdfDocument(pdfReader);
    assertEquals(4, pdfDoc.getNumberOfPages());
    assertEquals(PdfName.TwoPageRight, pdfDoc.getCatalog().getPageLayout());
    assertNull(pdfDoc.getCatalog().getViewerPreferences());
    pdfDoc.close();
    assertLogMatches(Level.INFO, //
        base.resolve("sample.zip").toString(), //
        "  -> " + base.resolve("sample.pdf"));
  }

  @Test
  void testRightToLeft() throws Exception {
    // Setup
    DataManager.generateZip(base.resolve("sample.zip"));
    // Exercise
    var exitCode = cmd.execute("--right-to-left", base + "/sample.zip");
    // Verify
    assertEquals(0, exitCode);
    var pdfReader = new PdfReader(base.resolve("sample.pdf").toFile());
    var pdfDoc = new PdfDocument(pdfReader);
    assertEquals(4, pdfDoc.getNumberOfPages());
    assertEquals(PdfName.TwoPageRight, pdfDoc.getCatalog().getPageLayout());
    var actualDirection = pdfDoc.getTrailer().getAsDictionary(PdfName.Root)
        .getAsDictionary(PdfName.ViewerPreferences).getAsName(PdfName.Direction);
    assertEquals(PdfName.R2L, actualDirection);
    pdfDoc.close();
    assertLogMatches(Level.INFO, //
        base.resolve("sample.zip").toString(), //
        "  -> " + base.resolve("sample.pdf"));
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
