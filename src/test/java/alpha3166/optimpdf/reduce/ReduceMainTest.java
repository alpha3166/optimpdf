package alpha3166.optimpdf.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.Nested;
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
import picocli.CommandLine.ParameterException;

@ExtendWith(MockitoExtension.class)
class ReduceMainTest {
  @Mock
  Appender<ILoggingEvent> mockAppender;
  @Captor
  ArgumentCaptor<ILoggingEvent> captor;

  Path base;
  ReduceMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);

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
  void testNormal() throws Exception {
    // Setup
    var jpeg = DataManager.generateJpeg();
    DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
    // Exercise
    var exitCode = cmd.execute("--outfile", base + "/out.pdf", base + "/src.pdf");
    // Verify
    assertEquals(0, exitCode);
    assertTrue(Files.isRegularFile(base.resolve("out.pdf")));
    assertLogMatches(Level.INFO,
        base + "/src.pdf",
        "  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%",
        "  -> " + base + "/out.pdf");
  }

  //
  // whitebox tests
  //

  @Nested
  class ParsePageDesignatorTest {
    @Test
    void testNormalCase() throws Exception {
      // Setup
      var sut = new ReduceMain();
      // Exercise
      var pages = sut.parsePageDesignator("1,3-5");
      // Verify
      assertTrue(pages.get(1));
      assertFalse(pages.get(2));
      assertTrue(pages.get(3));
      assertTrue(pages.get(4));
      assertTrue(pages.get(5));
      assertFalse(pages.get(6));
      assertFalse(pages.get(10000));
    }

    @Test
    void testErrorCase() throws Exception {
      // Setup
      var sut = new ReduceMain();
      // Exercise & Verify
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator("a"));
      assertThrows(IndexOutOfBoundsException.class, () -> sut.parsePageDesignator("0"));
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator("3-"));
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator("-5"));
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator("1-3-5"));
      assertThrows(IndexOutOfBoundsException.class, () -> sut.parsePageDesignator("5-3"));
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator("-"));
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator("1,,3-5"));
      assertThrows(NumberFormatException.class, () -> sut.parsePageDesignator(","));
    }
  }

  @Nested
  class HandleLocalOptionTest {
    @Test
    void testPagesNotSpecified() throws Exception {
      // Exercise
      var sut = parse();
      // Verify
      assertTrue(sut.isTargetPage(0)); // not used since pdf page number starts from 1
      assertTrue(sut.isTargetPage(1));
      assertTrue(sut.isTargetPage(10000));
    }

    @Test
    void testPagesSpecified() throws Exception {
      // Exercise
      var sut = parse("--pages", "1,3-5");
      // Verify
      assertFalse(sut.isTargetPage(0)); // not used since pdf page number starts from 1
      assertTrue(sut.isTargetPage(1));
      assertFalse(sut.isTargetPage(2));
      assertTrue(sut.isTargetPage(3));
      assertTrue(sut.isTargetPage(4));
      assertTrue(sut.isTargetPage(5));
      assertFalse(sut.isTargetPage(6));
      assertFalse(sut.isTargetPage(10000));
    }

    @Test
    void testPagesSpecifiedError() throws Exception {
      // Exercise & Verify
      assertThrows(IllegalArgumentException.class, () -> parse("--pages", "a"));
    }

    @Test
    void testScreenSizeNotSpecified() throws Exception {
      // Exercise
      var sut = parse();
      // Verify
      assertEquals(1536, sut.screenWidth);
      assertEquals(2048, sut.screenHeight);
    }

    @Test
    void testScreenSizeSpecifiedPortrait() throws Exception {
      // Exercise
      var sut = parse("--screen-size", "200x300");
      // Verify
      assertEquals(200, sut.screenWidth);
      assertEquals(300, sut.screenHeight);
    }
  
    @Test
    void testScreenSizeSpecifiedLandscape() throws Exception {
      // Exercise
      var sut = parse("--screen-size", "300x200");
      // Verify
      assertEquals(200, sut.screenWidth);
      assertEquals(300, sut.screenHeight);
    }

    @Test
    void testScreenSizeSpecifiedError() throws Exception {
      // Exercise & Verify
      assertThrows(IllegalArgumentException.class, () -> parse("--screen-size", "a"));
    }

    @Test
    void testDoublePageThresholdNotSpecified() throws Exception {
      // Exercise
      var sut = parse();
      // Verify
      assertEquals(2539, sut.opt.doublePageThreshold);
    }
  
    @Test
    void testDoublePageThresholdSpecified() throws Exception {
      // Exercise
      var sut = parse("--double-page-threshold", "2000");
      // Verify
      assertEquals(2000, sut.opt.doublePageThreshold);
    }
  
    @Test
    void testDoublePageThresholdSpecifiedError() throws Exception {
      // Exercise & Verify
      assertThrows(ParameterException.class, () -> parse("--double-page-threshold", "a"));
    }

    @Test
    void testJpegQualityNotSpecieifed() throws Exception {
      // Exercise
      var sut = parse();
      // Verify
      assertEquals(50, sut.opt.jpegQuality);
    }
  
    @Test
    void testJpegQualitySpecified() throws Exception {
      // Exercise
      var sut = parse("--jpeg-quality", "85");
      // Verify
      assertEquals(85, sut.opt.jpegQuality);
    }
  
    @Test
    void testJpegQualitySpecifiedError() throws Exception {
      // Exercise & Verify
      assertThrows(ParameterException.class, () -> parse("--jpeg-quality", "a"));
    }
  
      @Test
    void testBleachPagesNotSpecified() throws Exception {
      // Exercise
      var sut = parse();
      // Verify
      assertFalse(sut.isBleachPage(0)); // not used since pdf page number starts from 1
      assertFalse(sut.isBleachPage(1));
      assertFalse(sut.isBleachPage(10000));
    }

    @Test
    void testBleachPagesSpecified() throws Exception {
      // Exercise
      var sut = parse("--bleach-pages", "1,3-5");
      // Verify
      assertFalse(sut.isBleachPage(0)); // not used since pdf page number starts from 1
      assertTrue(sut.isBleachPage(1));
      assertFalse(sut.isBleachPage(2));
      assertTrue(sut.isBleachPage(3));
      assertTrue(sut.isBleachPage(4));
      assertTrue(sut.isBleachPage(5));
      assertFalse(sut.isBleachPage(6));
      assertFalse(sut.isBleachPage(10000));
    }

    @Test
    void testBleachPagesSpecifiedAll() throws Exception {
      // Exercise
      var sut = parse("--bleach-pages", "all");
      // Verify
      assertTrue(sut.isBleachPage(0)); // not used since pdf page number starts from 1
      assertTrue(sut.isBleachPage(1));
      assertTrue(sut.isBleachPage(10000));
    }

    @Test
    void testBleachPagesSpecifiedError() throws Exception {
      // Exercise & Verify
      assertThrows(IllegalArgumentException.class, () -> parse("--bleach-pages", "a"));
    }

    @Test
    void testNumberOfThreadsNotSpecified() throws Exception {
      // Exercise
      var sut = parse();
      // Verify
      var expected = Runtime.getRuntime().availableProcessors();
      assertEquals(expected, sut.opt.numberOfThreads);
    }
  
    @Test
    void testNumberOfThreadsSpecified() throws Exception {
      // Exercise
      var sut = parse("--number-of-threads", "4");
      // Verify
      assertEquals(4, sut.opt.numberOfThreads);
    }
  
    @Test
    void testNumberOfThreadsSpecifiedError() throws Exception {
      // Exercise & Verify
      assertThrows(ParameterException.class, () -> parse("--number-of-threads", "a"));
    }

    ReduceMain parse(String... args) throws Exception {
      var sut = CommandLine.populateCommand(new ReduceMain(), args);
      sut.handleLocalOption(null);
      return sut;
    }
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
