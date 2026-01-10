package alpha3166.optimpdf.setview;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;

public class SamplePdfGenerator {
  @Test
  void createSamplePdfs() throws Exception {
    var base = DataManager.makeTestDir();

    // Default
    try (var pdfDoc = createBasePdf(base.resolve("Default.pdf"))) {
      // do nothing
    }

    // PageLayout
    try (var pdfDoc = createBasePdf(base.resolve("PageLayout-1-SimglePage.pdf"))) {
      pdfDoc.getCatalog().setPageLayout(PdfName.SinglePage);
    }
    try (var pdfDoc = createBasePdf(base.resolve("PageLayout-2-OneColumn.pdf"))) {
      pdfDoc.getCatalog().setPageLayout(PdfName.OneColumn);
    }
    try (var pdfDoc = createBasePdf(base.resolve("PageLayout-3-TwoColumnLeft.pdf"))) {
      pdfDoc.getCatalog().setPageLayout(PdfName.TwoColumnLeft);
    }
    try (var pdfDoc = createBasePdf(base.resolve("PageLayout-4-TwoColumnRight.pdf"))) {
      pdfDoc.getCatalog().setPageLayout(PdfName.TwoColumnRight);
    }
    try (var pdfDoc = createBasePdf(base.resolve("PageLayout-5-TwoPageLeft.pdf"))) {
      pdfDoc.getCatalog().setPageLayout(PdfName.TwoPageLeft);
    }
    try (var pdfDoc = createBasePdf(base.resolve("PageLayout-6-TwoPageRight.pdf"))) {
      pdfDoc.getCatalog().setPageLayout(PdfName.TwoPageRight);
    }

    // Direction
    try (var pdfDoc = createBasePdf(base.resolve("Direction-1-L2R.pdf"))) {
      pdfDoc.getCatalog()
          .setViewerPreferences(new PdfViewerPreferences().setDirection(PdfViewerPreferencesConstants.LEFT_TO_RIGHT));
    }
    try (var pdfDoc = createBasePdf(base.resolve("Direction-2-R2L.pdf"))) {
      pdfDoc.getCatalog()
          .setViewerPreferences(new PdfViewerPreferences().setDirection(PdfViewerPreferencesConstants.RIGHT_TO_LEFT));
    }

    // OpenAction
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-1-Fit.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.Fit);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-2-FitH.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.FitH);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-3-FitV.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.FitV);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-4-FitR.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.FitR);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-5-FitB.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.FitB);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-6-FitBH.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.FitBH);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }
    try (var pdfDoc = createBasePdf(base.resolve("OpenAction-7-FitBV.pdf"))) {
      var array = new PdfArray();
      array.add(pdfDoc.getPage(1).getPdfObject());
      array.add(PdfName.FitBV);
      pdfDoc.getCatalog().put(PdfName.OpenAction, array);
    }

    // Cropped
    try (var pdfDoc = createBasePdf(base.resolve("Cropped.pdf"))) {
      for (var page = 1; page <= 4; page++) {
        pdfDoc.getPage(page).setCropBox(new Rectangle(25, 25, 570, 817));
      }
    }
  }

  private PdfDocument createBasePdf(Path path) throws IOException {
    var pdfWriter = new PdfWriter(path.toFile());
    var pdfDoc = new PdfDocument(pdfWriter);
    var doc = new Document(pdfDoc);
    for (var page = 1; page <= 4; page++) {
      if (page > 1) {
        pdfDoc.addNewPage();
        doc.add(new AreaBreak());
      }
      doc.add(new Paragraph(page + "").setFontSize(256));
    }
    return pdfDoc;
  }
}