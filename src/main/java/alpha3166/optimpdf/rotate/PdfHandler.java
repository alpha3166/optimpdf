package alpha3166.optimpdf.rotate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

public class PdfHandler {
  public static boolean updatePdfPageRotation(Path src, Path temp, boolean isRefPdfSpecified, RefRotation refRotation)
      throws FileNotFoundException, IOException {
    Logger logger = LoggerFactory.getLogger(PdfHandler.class);
    var updated = false;

    var pdfReader = new PdfReader(src.toFile());
    var pdfWriter = new PdfWriter(temp.toFile());
    try (var pdfDoc = new PdfDocument(pdfReader, pdfWriter)) {
      if (isRefPdfSpecified) {
        if (pdfDoc.getNumberOfPages() != refRotation.getRefPdfNumberOfPages()) {
          throw new RuntimeException(String.format("The number of pages differs: %d vs %d",
              refRotation.getRefPdfNumberOfPages(), pdfDoc.getNumberOfPages()));
        }
      }

      for (int page = 1; page <= pdfDoc.getNumberOfPages(); page++) {
        int currentRotaion = pdfDoc.getPage(page).getRotation();
        var newRotation = refRotation.getRefRotation(page);
        if (currentRotaion != newRotation) {
          updated = true;
          logger.info(String.format("  p%d: %d -> %d", page, currentRotaion, newRotation));
          pdfDoc.getPage(page).setRotation(newRotation);
        }
      }
    }
    return updated;
  }
}
