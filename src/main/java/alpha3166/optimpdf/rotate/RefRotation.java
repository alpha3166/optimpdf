package alpha3166.optimpdf.rotate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

public class RefRotation {
  private List<Integer> refPdfRotations; // page 1 corresponds to index 0
  private int refDegree;

  public RefRotation(Path refPdfPath, int degree) throws FileNotFoundException, IOException {
    if (refPdfPath == null) {
      refDegree = degree;
    } else {
      refPdfRotations = new ArrayList<>();
      try (var refPdf = new PdfDocument(new PdfReader(refPdfPath.toFile()))) {
        for (int page = 1; page <= refPdf.getNumberOfPages(); page++) {
          refPdfRotations.add(refPdf.getPage(page).getRotation()); // page 1 corresponds to index 0
        }
      }
    }
  }

  public int getRefRotation(int page) {
    return refPdfRotations == null ? refDegree : refPdfRotations.get(page - 1); // page 1 corresponds to index 0
  }

  public int getRefPdfNumberOfPages() {
    if (refPdfRotations == null) {
      throw new IllegalStateException("--ref-pdf not specified.");
    }
    return refPdfRotations.size();
  }
}
