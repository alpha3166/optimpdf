package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainTest {
	Path base;

	@BeforeEach
	void setUp() throws Exception {
		base = DataManager.makeTestDir();
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
		Main.main(base + "/src.pdf");
		// Verify
		assertTrue(Files.isRegularFile(base.resolve("src_r.pdf")));
	}

	@Test
	void test_Abort() throws Exception {
		// Setup
		var jpeg = DataManager.generateJpeg();
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		Main.main("-l", base + "/src.pdf");
		// Verify
		assertFalse(Files.exists(base.resolve("src_r.pdf")));
	}

	@Test
	void test_DryRun() throws Exception {
		// Setup
		var jpeg = DataManager.generateJpeg();
		DataManager.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		Main.main("-n", base + "/src.pdf");
		// Verify
		assertFalse(Files.exists(base.resolve("src_r.pdf")));
	}
}
