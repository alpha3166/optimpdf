package alpha3166.optimpdf.rotate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.itextpdf.kernel.pdf.PdfDocument;
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

  public static void generatePdf(Path path, int... rotations) throws FileNotFoundException {
    var pdfWriter = new PdfWriter(path.toFile());
    try (var pdfDoc = new PdfDocument(pdfWriter); var doc = new Document(pdfDoc)) {
      for (int i = 0; i < rotations.length; i++) {
        if (i > 0) {
          doc.add(new AreaBreak());
        }
        var page = i + 1;
        doc.add(new Paragraph("Page " + page));
        pdfDoc.getPage(page).setRotation(rotations[i]);
      }
    }
  }
}
