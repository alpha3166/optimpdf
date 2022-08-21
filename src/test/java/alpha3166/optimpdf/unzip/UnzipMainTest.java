package alpha3166.optimpdf.unzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;

import picocli.CommandLine;

class UnzipMainTest {
  List<String> logs = LogAppender.logs;
  Path base;
  UnzipMain sut;
  CommandLine cmd;
  StringWriter out;
  StringWriter err;

  @BeforeEach
  void setUp() throws Exception {
    logs.clear();
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
  }

  @Test
  void test() throws Exception {
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
    assertEquals(1, logs.size());
    assertEquals(base.resolve("sample.zip").toString(), logs.get(0));
  }

  @Test
  void test_Help() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-h", base + "/sample.zip");
    // Verify
    assertEquals(0, exitCode);
    assertEquals("Usage:", out.toString().substring(0, 6));
    assertEquals(0, err.getBuffer().length());
  }

  @Test
  void test_UnknownOption() throws Exception {
    // Exercise
    var exitCode = cmd.execute("-X", base + "/sample.zip");
    // Verify
    assertEquals(2, exitCode);
    assertEquals(0, out.getBuffer().length());
    assertEquals("Unknown option: '-X'", err.toString().substring(0, 20));
  }
}
