package alpha3166.optimpdf;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

public class ITextHelper {
	public static void generatePdf(Path path, byte[] jpeg) throws FileNotFoundException {
		var pdfWriter = new PdfWriter(path.toFile());
		var pdfDocument = new PdfDocument(pdfWriter);
		var document = new Document(pdfDocument);
		var imageData = ImageDataFactory.create(jpeg);
		var image = new Image(imageData);
		document.add(image);
		document.close();
	}
}
