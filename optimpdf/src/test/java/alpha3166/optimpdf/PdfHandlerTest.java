package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PdfHandlerTest {
	PdfHandler sut;

	@BeforeAll
	static void setUpClass() throws Exception {
		byte[] srcPdf = ImageMagickHelper.exec("wizard: pdf:-");
		Files.write(Paths.get("src.pdf"), srcPdf);
	}

	@AfterAll
	static void tearDownClass() throws Exception {
		Files.delete(Paths.get("src.pdf"));
	}

	@BeforeEach
	void setUp() throws Exception {
		sut = new PdfHandler(Paths.get("src.pdf"));
	}

	@AfterEach
	void tearDown() throws Exception {
		Files.deleteIfExists(Paths.get("dest.pdf"));
	}

	@Test
	void testPdfHandler() throws Exception {
		// Verify
		assertNotNull(sut);
	}

	@Test
	void testExtractJpeg() throws Exception {
		// Exercise
		var actual = sut.extractJpeg(1);
		// Verify
		var jpegHandler = new JpegHandler(actual);
		assertEquals(480, jpegHandler.getWidth());
		assertEquals(640, jpegHandler.getHeight());
	}

	@Test
	void testReplaceJpeg() throws Exception {
		// Setup
		var newJpeg = ImageMagickHelper.exec("wizard: -resize 240x320 -");
		// Exercise
		sut.replaceJpeg(1, newJpeg, 240, 320, false);
		// Verify
		var jpegHandler = new JpegHandler(sut.extractJpeg(1));
		assertEquals(240, jpegHandler.getWidth());
		assertEquals(320, jpegHandler.getHeight());
	}

	@Test
	void testSave() throws Exception {
		// Exercise
		sut.save(Paths.get("dest.pdf"));
		// Verify
		var pdfHandler = new PdfHandler(Paths.get("dest.pdf"));
		var jpegHandler = new JpegHandler(pdfHandler.extractJpeg(1));
		assertEquals(480, jpegHandler.getWidth());
		assertEquals(640, jpegHandler.getHeight());
	}

	@Test
	void testGetNumberOfPages() {
		// Exercise
		var actual = sut.getNumberOfPages();
		// Verify
		assertEquals(1, actual);
	}
}
