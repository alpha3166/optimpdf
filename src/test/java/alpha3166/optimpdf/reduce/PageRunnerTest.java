package alpha3166.optimpdf.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

@ExtendWith(MockitoExtension.class)
class PageRunnerTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;

  @BeforeEach
  void setUp() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

    base = DataManager.makeTestDir();
  }

  @AfterEach
  void tearDown() throws Exception {
    DataManager.removeDir(base);

    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.detachAppender(mockAppender);
  }

  @Test
  void test_BigPortrait() throws Exception {
    test(1800, 2540, 1451, 2048);
  }

  @Test
  void test_BigLandscape() throws Exception {
    test(2540, 1800, 2048, 1451);
  }

  @Test
  void test_SmallPortrait() throws Exception {
    test(1800, 2539, 1024, 1444);
  }

  @Test
  void test_SmallLandscape() throws Exception {
    test(2539, 1800, 1444, 1024);
  }

  void test(int srcWidth, int srcHeight, int expectedWidth, int expectedHeight) throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg(srcWidth, srcHeight);
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    var pdfHandler = new PdfHandler(base.resolve("src.pdf"), base.resolve("dest.pdf"));
    // Exercise
    var pageRunner = new PageRunner(pdfHandler, 1, 1536, 2048, 2539, 50, false, false);
    pageRunner.run();
    // Verify
    var jpegHandler = new JpegHandler(pdfHandler.extractJpeg(1));
    assertEquals(expectedWidth, jpegHandler.getWidth());
    assertEquals(expectedHeight, jpegHandler.getHeight());
    var expectedLog = String.format("  1/1 %dx%d \\d+K \\(fit \\d+x\\d+\\) > %dx%d \\d+K \\d+%%",
        srcWidth, srcHeight, expectedWidth, expectedHeight);
    assertLogMatches(Level.INFO, expectedLog);
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
