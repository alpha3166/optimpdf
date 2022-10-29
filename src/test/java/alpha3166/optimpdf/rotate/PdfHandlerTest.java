package alpha3166.optimpdf.rotate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

@ExtendWith(MockitoExtension.class)
public class PdfHandlerTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;

  @BeforeEach
  public void beforeEach() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

    base = DataManager.makeTestDir();
  }

  @AfterEach
  public void afterEach() throws Exception {
    DataManager.removeDir(base);

    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.detachAppender(mockAppender);
  }

  @Test
  void refPdfSpecified_numberOfPagesMismatch() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180);
    var rotationRef = new RefRotation(base.resolve("ref.pdf"), 0);
    // Exercise & Verify
    assertThrows(RuntimeException.class,
        () -> PdfHandler.updatePdfPageRotation(base.resolve("src.pdf"), base.resolve("temp.pdf"), true, rotationRef));
  }

  @Test
  void refPdfSpecified_0update() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    var rotationRef = new RefRotation(base.resolve("ref.pdf"), 0);
    // Exercise
    boolean updated = PdfHandler.updatePdfPageRotation(base.resolve("src.pdf"), base.resolve("temp.pdf"), true,
        rotationRef);
    // Verify
    assertFalse(updated);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  void refPdfSpecified_1update() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("src.pdf"), 90, 90, 180, 270);
    var rotationRef = new RefRotation(base.resolve("ref.pdf"), 0);
    // Exercise
    boolean updated = PdfHandler.updatePdfPageRotation(base.resolve("src.pdf"), base.resolve("temp.pdf"), true,
        rotationRef);
    // Verify
    assertTrue(updated);
    assertLogMatches(Level.INFO, "  p1: 90 -> 0");
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  void refPdfSpecified_4updates() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    DataManager.generatePdf(base.resolve("src.pdf"), 90, 180, 270, 0);
    var rotationRef = new RefRotation(base.resolve("ref.pdf"), 0);
    // Exercise
    boolean updated = PdfHandler.updatePdfPageRotation(base.resolve("src.pdf"), base.resolve("temp.pdf"), true,
        rotationRef);
    // Verify
    assertTrue(updated);
    assertLogMatches(Level.INFO,
        "  p1: 90 -> 0",
        "  p2: 180 -> 90",
        "  p3: 270 -> 180",
        "  p4: 0 -> 270");
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(90, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(270, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  void refPdfNotSpecified_0update() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 0, 0, 0);
    var rotationRef = new RefRotation(null, 0);
    // Exercise
    boolean updated = PdfHandler.updatePdfPageRotation(base.resolve("src.pdf"), base.resolve("temp.pdf"), false,
        rotationRef);
    // Verify
    assertFalse(updated);
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      assertEquals(0, resultPdf.getPage(1).getRotation());
      assertEquals(0, resultPdf.getPage(2).getRotation());
      assertEquals(0, resultPdf.getPage(3).getRotation());
      assertEquals(0, resultPdf.getPage(4).getRotation());
    }
  }

  @Test
  void refPdfNotSpecified_3updates() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), 0, 90, 180, 270);
    var rotationRef = new RefRotation(null, 180);
    // Exercise
    boolean updated = PdfHandler.updatePdfPageRotation(base.resolve("src.pdf"), base.resolve("temp.pdf"), false,
        rotationRef);
    // Verify
    assertTrue(updated);
    assertLogMatches(Level.INFO,
        "  p1: 0 -> 180",
        "  p2: 90 -> 180",
        "  p4: 270 -> 180");
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      assertEquals(180, resultPdf.getPage(1).getRotation());
      assertEquals(180, resultPdf.getPage(2).getRotation());
      assertEquals(180, resultPdf.getPage(3).getRotation());
      assertEquals(180, resultPdf.getPage(4).getRotation());
    }
  }

  void assertLogMatches(Level expectedLevel, String... expectedMessages) {
    verify(mockAppender, times(expectedMessages.length)).doAppend(captor.capture());
    var expectedLines = new LinkedList<>(Arrays.asList(expectedMessages));
    for (var event : captor.getAllValues()) {
      assertEquals(expectedLevel, event.getLevel());
      assertTrue(event.getFormattedMessage().matches(expectedLines.removeFirst()));
    }
  }
}
