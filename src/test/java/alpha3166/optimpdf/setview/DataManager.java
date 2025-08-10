package alpha3166.optimpdf.setview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;

public class DataManager {
  public static Path makeTestDir() throws IOException {
    return Files.createTempDirectory(Paths.get(""), "junit");
  }

  public static void removeDir(Path dir) throws IOException {
    Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  public static void generatePdf(Path path, PdfName layout, PdfViewerPreferencesConstants direction, PdfName fit)
      throws IOException {
    var pdfWriter = new PdfWriter(path.toFile());
    try (var pdfDoc = new PdfDocument(pdfWriter); var doc = new Document(pdfDoc)) {
      for (var page = 1; page <= 4; page++) {
        if (page > 1) {
          pdfDoc.addNewPage();
          doc.add(new AreaBreak());
        }
        doc.add(new Paragraph(page + "").setFontSize(256));
      }
      if (layout != null) {
        pdfDoc.getCatalog().setPageLayout(layout);
      }
      if (direction != null) {
        var viewerPref = new PdfViewerPreferences();
        viewerPref.setDirection(direction);
        pdfDoc.getCatalog().setViewerPreferences(viewerPref);
      }
      if (fit != null) {
        var array = new PdfArray();
        array.add(pdfDoc.getPage(1).getPdfObject());
        array.add(fit);
        pdfDoc.getCatalog().put(PdfName.OpenAction, array);
      }
    }
  }
}
