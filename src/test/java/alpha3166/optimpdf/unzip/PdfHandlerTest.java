package alpha3166.optimpdf.unzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;

class PdfHandlerTest {
  Path base;

  @BeforeEach
  void setUp() throws Exception {
    base = DataManager.makeTestDir();
    DataManager.generateZip(base.resolve("sample.zip"));
  }

  @AfterEach
  void tearDown() throws Exception {
    DataManager.removeDir(base);
  }

  @Test
  void testCreatePdfFromImages() throws Exception {
    // Setup
    var zipHandler = new ZipHandler(base.resolve("sample.zip"));
    var imagePathList = zipHandler.getImagePathList();
    // Exercise
    PdfHandler.createPdfFromImages(base.resolve("sample.pdf"), imagePathList, false);
    // Verify
    var pdfReader = new PdfReader(base.resolve("sample.pdf").toFile());
    var pdfDoc = new PdfDocument(pdfReader);
    assertEquals(4, pdfDoc.getNumberOfPages());
    assertEquals(PdfName.TwoPageRight, pdfDoc.getCatalog().getPageLayout());
    assertNull(pdfDoc.getCatalog().getViewerPreferences());
    // Teardown
    pdfDoc.close();
    zipHandler.close();
  }

  @Test
  void testCreatePdfFromImages_rightToLeft() throws Exception {
    // Setup
    var zipHandler = new ZipHandler(base.resolve("sample.zip"));
    var imagePathList = zipHandler.getImagePathList();
    // Exercise
    PdfHandler.createPdfFromImages(base.resolve("sample.pdf"), imagePathList, true);
    // Verify
    var pdfReader = new PdfReader(base.resolve("sample.pdf").toFile());
    var pdfDoc = new PdfDocument(pdfReader);
    assertEquals(4, pdfDoc.getNumberOfPages());
    assertEquals(PdfName.TwoPageRight, pdfDoc.getCatalog().getPageLayout());
    var actualDirection = pdfDoc.getTrailer().getAsDictionary(PdfName.Root)
        .getAsDictionary(PdfName.ViewerPreferences).getAsName(PdfName.Direction);
    assertEquals(PdfName.R2L, actualDirection);
    // Teardown
    pdfDoc.close();
    zipHandler.close();
  }
}
