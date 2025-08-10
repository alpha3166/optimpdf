package alpha3166.optimpdf.setview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;

public class PdfHandlerTest {
  Path base;

  @BeforeEach
  public void beforeEach() throws Exception {
    base = DataManager.makeTestDir();
  }

  @AfterEach
  public void afterEach() throws Exception {
    DataManager.removeDir(base);
  }

  @Test
  void clear() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), PdfName.SinglePage, PdfViewerPreferencesConstants.RIGHT_TO_LEFT,
        PdfName.Fit);
    // Exercise
    PdfHandler.setView(base.resolve("src.pdf"), base.resolve("temp.pdf"), true, null, false, false);
    // Verify
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      var catalog = resultPdf.getCatalog();
      assertNull(catalog.getPdfObject().get(PdfName.OpenAction));
      assertNull(catalog.getPageLayout());
      assertNull(catalog.getViewerPreferences());
    }
  }

  @Test
  void pageLayout_SinglePage() throws Exception {
    testPageLayout("SinglePage", PdfName.SinglePage);
  }

  @Test
  void pageLayout_OneColumn() throws Exception {
    testPageLayout("OneColumn", PdfName.OneColumn);
  }

  @Test
  void pageLayout_TwoColumnLeft() throws Exception {
    testPageLayout("TwoColumnLeft", PdfName.TwoColumnLeft);
  }

  @Test
  void pageLayout_TwoColumnRight() throws Exception {
    testPageLayout("TwoColumnRight", PdfName.TwoColumnRight);
  }

  @Test
  void pageLayout_TwoPageLeft() throws Exception {
    testPageLayout("TwoPageLeft", PdfName.TwoPageLeft);
  }

  @Test
  void pageLayout_TwoPageRight() throws Exception {
    testPageLayout("TwoPageRight", PdfName.TwoPageRight);
  }

  void testPageLayout(String param, PdfName expected) throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), null, null, null);
    // Exercise
    PdfHandler.setView(base.resolve("src.pdf"), base.resolve("temp.pdf"), false, param, false, false);
    // Verify
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      var catalog = resultPdf.getCatalog();
      assertNull(catalog.getPdfObject().get(PdfName.OpenAction));
      assertEquals(expected, catalog.getPageLayout());
      assertNull(catalog.getViewerPreferences());
    }
  }

  @Test
  void pageLayout_InvalidValue() throws Exception {
    // Exercise & Verify
    assertThrows(IllegalArgumentException.class,
        () -> PdfHandler.setView(base.resolve("src.pdf"), base.resolve("temp.pdf"), false, "InvalidValue", false,
            false));
  }

  @Test
  void rightToLeft() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), null, null, null);
    // Exercise
    PdfHandler.setView(base.resolve("src.pdf"), base.resolve("temp.pdf"), false, null, true, false);
    // Verify
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      var catalog = resultPdf.getCatalog();
      assertNull(catalog.getPdfObject().get(PdfName.OpenAction));
      assertNull(catalog.getPageLayout());
      assertEquals(PdfName.R2L, catalog.getViewerPreferences().getPdfObject().get(PdfName.Direction));
    }
  }

  @Test
  void fit() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("src.pdf"), null, null, null);
    // Exercise
    PdfHandler.setView(base.resolve("src.pdf"), base.resolve("temp.pdf"), false, null, false, true);
    // Verify
    try (var resultPdf = new PdfDocument(new PdfReader(base + "/temp.pdf"))) {
      var catalog = resultPdf.getCatalog();
      var array = catalog.getPdfObject().getAsArray(PdfName.OpenAction);
      assertEquals(resultPdf.getPage(1).getPdfObject(), array.get(0));
      assertEquals(PdfName.Fit, array.get(1));
      assertNull(catalog.getPageLayout());
      assertNull(catalog.getViewerPreferences());
    }
  }
}
