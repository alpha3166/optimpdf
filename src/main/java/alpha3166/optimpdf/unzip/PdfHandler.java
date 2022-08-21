package alpha3166.optimpdf.unzip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

public class PdfHandler {
  public static void createPdfFromImages(Path pdfPath, List<Path> imagePathList, boolean rightToLeft) throws IOException {
    var pdfWriter = new PdfWriter(pdfPath.toFile());
    var pdfDoc = new PdfDocument(pdfWriter);

    pdfDoc.getCatalog().setPageLayout(PdfName.TwoPageRight);

    if (rightToLeft) {
      var viewerPref = new PdfViewerPreferences();
      viewerPref.setDirection(PdfViewerPreferencesConstants.RIGHT_TO_LEFT);
      pdfDoc.getCatalog().setViewerPreferences(viewerPref);
    }

    var doc = new Document(pdfDoc);
    for (var imagePath : imagePathList) {
      var bytes = Files.readAllBytes(imagePath);

      var imageData = ImageDataFactory.create(bytes);
      // Assume the image is 300dpi, and calculate the size in points.
      var widthPt = (imageData.getWidth() / 300) * 72;
      var heightPt = (imageData.getHeight() / 300) * 72;

      pdfDoc.setDefaultPageSize(new PageSize(widthPt, heightPt));
      doc.setMargins(0, 0, 0, 0);

      var image = new Image(imageData);
      image.scaleToFit(widthPt, heightPt);
      doc.add(image);
    }
    doc.close();
  }
}
