package alpha3166.optimpdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.itextpdf.io.image.ImageDataFactory;
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
		var pdfWriter = new PdfWriter(path.toFile());
		var pdfDocument = new PdfDocument(pdfWriter);
		var document = new Document(pdfDocument);
		var imageData = ImageDataFactory.create(jpeg);
		var image = new Image(imageData);
		document.add(image);
		document.close();
	}
}
