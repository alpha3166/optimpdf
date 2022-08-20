package alpha3166.optimpdf.reduce;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

public class DataManager {
	public static Path makeTestDir() throws IOException {
		return Files.createTempDirectory(Paths.get(""), "junit");
	}

	public static void removeDir(Path dir) throws IOException {
		Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	public static byte[] generateJpeg() throws IOException {
		return execCommand("convert wizard: jpeg:-");
	}

	public static byte[] generateJpeg(int width, int height) throws IOException {
		return execCommand(String.format("convert wizard: -resize %dx%d! jpeg:-", width, height));
	}

	private static byte[] execCommand(String command) throws IOException {
		var pb = new ProcessBuilder(command.split(" "));
		var p = pb.start();

		var pErrReader = new AsyncProcessStderrReader(p.getErrorStream());
		var pErrReaderThread = new Thread(pErrReader);
		pErrReaderThread.start();

		var pOutReader = new AsyncProcessOutputReader(p.getInputStream());
		var pOutReaderThread = new Thread(pOutReader);
		pOutReaderThread.start();

		try {
			p.waitFor();
			pOutReaderThread.join();
			pErrReaderThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return pOutReader.getBytes();
	}

	public static void generatePdf(Path path, byte[] jpeg) throws FileNotFoundException {
		var imageData = ImageDataFactory.create(jpeg);
		// Assume the jpeg is 300dpi, and calculate the size in points.
		var widthPt = (imageData.getWidth() / 300) * 72;
		var heightPt = (imageData.getHeight() / 300) * 72;

		var pdfWriter = new PdfWriter(path.toFile());
		var pdfDocument = new PdfDocument(pdfWriter);
		var document = new Document(pdfDocument, new PageSize(widthPt, heightPt));
		document.setMargins(0, 0, 0, 0);
		var image = new Image(imageData);
		image.scaleToFit(widthPt, heightPt);
		document.add(image);
		document.close();
	}
}
