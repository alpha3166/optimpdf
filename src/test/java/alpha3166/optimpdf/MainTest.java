package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainTest {
	List<String> logs = LogAppender.logs;
	Path base;
	Main sut;

	@BeforeEach
	void setUp() throws Exception {
		logs.clear();
		base = DataManager.makeTestDir();
		sut = new Main();
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
		assertEquals(3, logs.size());
		assertEquals(base + "/src.pdf", logs.get(0));
		assertTrue(logs.get(1).matches("  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%"));
		assertEquals("  -> " + base + "/src_r.pdf", logs.get(2));
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
		assertEquals(1, logs.size());
		assertEquals(base + "/src.pdf -> " + base + "/src_r.pdf", logs.get(0));
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
		assertEquals(3, logs.size());
		assertEquals(base + "/src.pdf", logs.get(0));
		assertTrue(logs.get(1).matches("  1/1 480x640 \\d+K \\(fit 1024x1536\\) > 480x640 \\d+K \\d+%"));
		assertEquals("  -> " + base + "/src_r.pdf", logs.get(2));
	}
}
