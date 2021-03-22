package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainTest {
	Path base;

	@BeforeEach
	void setUp() throws Exception {
		base = Files.createTempDirectory(Paths.get(""), "junit");
	}

	@AfterEach
	void tearDown() throws Exception {
		Files.walk(base).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	void test() throws Exception {
		// Setup
		var jpeg = ImageMagickHelper.exec("wizard: jpeg:-");
		ITextHelper.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		Main.main(base + "/src.pdf");
		// Verify
		assertTrue(Files.isRegularFile(base.resolve("src_r.pdf")));
	}

	@Test
	void test_Abort() throws Exception {
		// Setup
		var jpeg = ImageMagickHelper.exec("wizard: jpeg:-");
		ITextHelper.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		Main.main("-l", base + "/src.pdf");
		// Verify
		assertFalse(Files.exists(base.resolve("src_r.pdf")));
	}

	@Test
	void test_DryRun() throws Exception {
		// Setup
		var jpeg = ImageMagickHelper.exec("wizard: jpeg:-");
		ITextHelper.generatePdf(base.resolve("src.pdf"), jpeg);
		// Exercise
		Main.main("-n", base + "/src.pdf");
		// Verify
		assertFalse(Files.exists(base.resolve("src_r.pdf")));
	}
}
