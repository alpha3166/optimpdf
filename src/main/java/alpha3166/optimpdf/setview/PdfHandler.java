package alpha3166.optimpdf.setview;

import java.io.IOException;
import java.nio.file.Path;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;
import com.itextpdf.kernel.pdf.PdfWriter;

public class PdfHandler {
  public static void setView(Path src, Path temp, boolean clear, String layout, boolean rightToLeft, boolean fit)
      throws IOException {

    PdfName layoutName = null;
    if (layout != null) {
      switch (layout) {
        case "SinglePage" -> layoutName = PdfName.SinglePage;
        case "OneColumn" -> layoutName = PdfName.OneColumn;
        case "TwoColumnLeft" -> layoutName = PdfName.TwoColumnLeft;
        case "TwoColumnRight" -> layoutName = PdfName.TwoColumnRight;
        case "TwoPageLeft" -> layoutName = PdfName.TwoPageLeft;
        case "TwoPageRight" -> layoutName = PdfName.TwoPageRight;
        default -> throw new IllegalArgumentException("layout: " + layout);
      }
    }

    var pdfReader = new PdfReader(src.toFile());
    var pdfWriter = new PdfWriter(temp.toFile());
    try (var pdfDoc = new PdfDocument(pdfReader, pdfWriter)) {
      if (clear) {
        var catalog = pdfDoc.getCatalog();
        catalog.remove(PdfName.OpenAction);
        catalog.remove(PdfName.PageLayout);
        catalog.remove(PdfName.ViewerPreferences);
      }

      if (layoutName != null) {
        pdfDoc.getCatalog().setPageLayout(layoutName);
      }

      if (rightToLeft) {
        var viewerPref = new PdfViewerPreferences();
        viewerPref.setDirection(PdfViewerPreferencesConstants.RIGHT_TO_LEFT);
        pdfDoc.getCatalog().setViewerPreferences(viewerPref);
      }

      if (fit) {
        var firstPage = pdfDoc.getPage(1).getPdfObject();
        var fitAction = PdfName.Fit;
        var array = new PdfArray();
        array.add(firstPage);
        array.add(fitAction);
        pdfDoc.getCatalog().put(PdfName.OpenAction, array);
      }
    }
  }
}
