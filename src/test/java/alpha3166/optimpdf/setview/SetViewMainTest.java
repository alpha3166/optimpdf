package alpha3166.optimpdf.setview;

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
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class SetViewMainTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;
  SetViewMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

    base = DataManager.makeTestDir();
    sut = new SetViewMain();
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
  void testClear() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), PdfName.SinglePage, PdfViewerPreferencesConstants.LEFT_TO_RIGHT,
        PdfName.FitV);
    // Exercise
    var exitCode = cmd.execute("--force", "--clear", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      var catalog = resultPdf.getCatalog();
      assertNull(catalog.getPdfObject().get(PdfName.OpenAction));
      assertNull(catalog.getPageLayout());
      assertNull(catalog.getViewerPreferences());
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  -> " + base + "/src.pdf");
  }

  @Test
  void testAll() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), null, null, null);
    // Exercise
    var exitCode = cmd.execute("--force", "--page-layout","TwoPageRight","--right-to-left", "--fit", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/src.pdf"))) {
      var catalog = resultPdf.getCatalog();
      var array = catalog.getPdfObject().getAsArray(PdfName.OpenAction);
      assertEquals(resultPdf.getPage(1).getPdfObject(), array.get(0));
      assertEquals(PdfName.Fit, array.get(1));
      assertEquals(PdfName.TwoPageRight, catalog.getPageLayout());
      assertEquals(PdfName.R2L, catalog.getViewerPreferences().getPdfObject().get(PdfName.Direction));
    }
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  -> " + base + "/src.pdf");
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
