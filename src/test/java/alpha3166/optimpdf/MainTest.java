package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainTest {
	Path base;
	Main sut;
	LogHandler logHandler;

	@BeforeEach
	void setUp() throws Exception {
		base = DataManager.makeTestDir();
		sut = new Main();
		logHandler = new LogHandler();
		var logger = Logger.getLogger("");
		for (var handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}
		logger.addHandler(logHandler);
		sut.logger = logger;
	}

	@AfterEach
	void tearDown() throws Exception {
		DataManager.removeDir(base);
	}

	@Test
	void test() throws Exception {
		// Setup
		var jpeg = DataManager.generateJpeg();
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		sut.execute(base + "/src.pdf");
		// Verify
		assertTrue(Files.isRegularFile(base.resolve("src_r.pdf")));
		assertEquals(3, logHandler.getLogCount());
		assertEquals(base + "/src.pdf", logHandler.getLog(0));
		assertTrue(logHandler.getLog(1).matches("  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%"));
		assertEquals("  -> " + base + "/src_r.pdf", logHandler.getLog(2));
	}

	@Test
	void test_Abort() throws Exception {
		// Setup
		var jpeg = DataManager.generateJpeg();
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		sut.execute("-l", base + "/src.pdf");
		// Verify
		assertFalse(Files.exists(base.resolve("src_r.pdf")));
		assertEquals(1, logHandler.getLogCount());
		assertEquals(base + "/src.pdf -> " + base + "/src_r.pdf", logHandler.getLog(0));
	}

	@Test
	void test_DryRun() throws Exception {
		// Setup
		var jpeg = DataManager.generateJpeg();
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		sut.execute("-n", base + "/src.pdf");
		// Verify
		assertFalse(Files.exists(base.resolve("src_r.pdf")));
		assertEquals(3, logHandler.getLogCount());
		assertEquals(base + "/src.pdf", logHandler.getLog(0));
		assertTrue(logHandler.getLog(1).matches("  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%"));
		assertEquals("  -> " + base + "/src_r.pdf", logHandler.getLog(2));
	}
}
