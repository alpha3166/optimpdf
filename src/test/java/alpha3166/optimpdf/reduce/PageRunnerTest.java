package alpha3166.optimpdf.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageRunnerTest {
	List<String> logs = LogAppender.logs;
	Path base;

	@BeforeEach
	void setUp() throws Exception {
		logs.clear();
		base = DataManager.makeTestDir();
	}

	@AfterEach
	void tearDown() throws Exception {
		DataManager.removeDir(base);
	}

	@Test
	void test_BigPortrait() throws Exception {
		test(1800, 2540, 1451, 2048);
	}

	@Test
	void test_BigLandscape() throws Exception {
		test(2540, 1800, 2048, 1451);
	}

	@Test
	void test_SmallPortrait() throws Exception {
		test(1800, 2539, 1024, 1444);
	}

	@Test
	void test_SmallLandscape() throws Exception {
		test(2539, 1800, 1444, 1024);
	}

	void test(int srcWidth, int srcHeight, int expectedWidth, int expectedHeight) throws Exception {
		// Setup
		var jpeg = DataManager.generateJpeg(srcWidth, srcHeight);
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
		var pdfHandler = new PdfHandler(base.resolve("src.pdf"));
		var optHandler = new OptionHandler(base + "/src.pdf");
		// Exercise
		var pageRunner = new PageRunner(pdfHandler, 1, optHandler);
		pageRunner.run();
		// Verify
		var jpegHandler = new JpegHandler(pdfHandler.extractJpeg(1));
		assertEquals(expectedWidth, jpegHandler.getWidth());
		assertEquals(expectedHeight, jpegHandler.getHeight());
		assertEquals(1, logs.size());
		var expectedLog = String.format("  1/1 %dx%d \\d+K \\(fit \\d+x\\d+\\) > %dx%d \\d+K \\d+%%", //
				srcWidth, srcHeight, expectedWidth, expectedHeight);
		assertTrue(logs.get(0).matches(expectedLog));
	}
}
