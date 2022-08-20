package alpha3166.optimpdf.rotate;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "rotate", description = "Show page rotation differences, and fix them if ordered")
public class RotateMain implements Callable<Integer> {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Option(names = "-h", usageHelp = true, description = "display this help and exit")
	boolean help;

	@Option(names = "-f", description = "fix rotations to be consistent with reference PDF")
	boolean fix;

	@Parameters(index = "0", paramLabel = "REFERENCE_PDF")
	Path refPath;

	@Parameters(index = "1", paramLabel = "TARGET_PDF")
	Path targetPath;

	@Override
	public Integer call() throws Exception {
		if (!Files.isRegularFile(refPath)) {
			throw new NoSuchFileException(refPath.toString());
		}
		if (!Files.isRegularFile(targetPath)) {
			throw new NoSuchFileException(targetPath.toString());
		}
		if (refPath.toRealPath().equals(targetPath.toRealPath())) {
			throw new IllegalArgumentException("Reference and target PDFs are the same");
		}

		logger.info(targetPath.toString());
		var targetDirPath = targetPath.toRealPath().getParent(); // toRealPath() prevents bare filename's parent be null
		var newPdfPath = Files.createTempFile(targetDirPath, targetPath.getFileName() + ".", ".pdf");
		var updated = false;
		try (var refPdf = new PdfDocument(new PdfReader(refPath.toFile()));
				var targetPdf = new PdfDocument(new PdfReader(targetPath.toFile()),
						new PdfWriter(newPdfPath.toFile()))) {

			if (refPdf.getNumberOfPages() != targetPdf.getNumberOfPages()) {
				throw new RuntimeException(String.format("The number of pages differs: %d vs %d",
						refPdf.getNumberOfPages(), targetPdf.getNumberOfPages()));
			}

			for (int page = 1; page <= refPdf.getNumberOfPages(); page++) {
				int refRotation = refPdf.getPage(page).getRotation();
				int targetRotaion = targetPdf.getPage(page).getRotation();
				if (refRotation != targetRotaion) {
					updated = true;
					logger.info(String.format("  p%d: %d -> %d", page, targetRotaion, refRotation));
					targetPdf.getPage(page).setRotation(refRotation);
				}
			}
		}
		if (fix && updated) {
			Files.move(newPdfPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} else {
			Files.delete(newPdfPath);
		}
		return 0;
	}
}
