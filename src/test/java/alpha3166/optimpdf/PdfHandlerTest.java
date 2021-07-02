package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PdfHandlerTest {
	static Path base;

	@BeforeAll
	static void setUpClass() throws Exception {
		base = DataManager.makeTestDir();
		var jpeg = DataManager.generateJpeg();
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
	}

	@AfterAll
	static void tearDownClass() throws Exception {
		DataManager.removeDir(base);
	}

	@AfterEach
	void tearDown() throws Exception {
		Files.deleteIfExists(base.resolve("dest.pdf"));
	}

	@Test
	void testPdfHandler_ReadOnlyMode() throws Exception {
		// Exercise
		var sut = new PdfHandler(base.resolve("src.pdf"));
		// Verify
		assertNotNull(sut);
		assertFalse(Files.isRegularFile(base.resolve("dest.pdf")));
	}

	@Test
	void testPdfHandler_StampingMode() throws Exception {
		// Exercise
		var sut = new PdfHandler(base.resolve("src.pdf"), base.resolve("dest.pdf"));
		// Verify
		assertNotNull(sut);
		assertTrue(Files.isRegularFile(base.resolve("dest.pdf")));
	}

	@Test
	void testExtractJpeg() throws Exception {
		// Setup
		var sut = new PdfHandler(base.resolve("src.pdf"), base.resolve("dest.pdf"));
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
		var newJpeg = DataManager.generateJpeg(240, 320);
		var sut = new PdfHandler(base.resolve("src.pdf"), base.resolve("dest.pdf"));
		// Exercise
		sut.replaceJpeg(1, newJpeg, 240, 320, false);
		// Verify
		var jpegHandler = new JpegHandler(sut.extractJpeg(1));
		assertEquals(240, jpegHandler.getWidth());
		assertEquals(320, jpegHandler.getHeight());
	}

	@Test
	void testClose() throws Exception {
		// Setup
		var newJpeg = DataManager.generateJpeg(240, 320);
		var sut = new PdfHandler(base.resolve("src.pdf"), base.resolve("dest.pdf"));
		sut.replaceJpeg(1, newJpeg, 240, 320, false);
		// Exercise
		sut.close();
		// Verify
		var pdfHandler = new PdfHandler(base.resolve("dest.pdf"));
		var jpegHandler = new JpegHandler(pdfHandler.extractJpeg(1));
		assertEquals(240, jpegHandler.getWidth());
		assertEquals(320, jpegHandler.getHeight());
	}

	@Test
	void testGetNumberOfPages() throws Exception {
		// Setup
		var sut = new PdfHandler(base.resolve("src.pdf"), base.resolve("dest.pdf"));
		// Exercise
		var actual = sut.getNumberOfPages();
		// Verify
		assertEquals(1, actual);
	}
}
