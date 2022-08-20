package alpha3166.optimpdf.unzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class OptionHandlerTest {
	Path base;

	@BeforeEach
	void setUp() throws Exception {
		base = DataManager.makeTestDir();

		Files.createFile(base.resolve("a.zip"));

		var dir1 = base.resolve("dir1");
		Files.createDirectory(dir1);
		Files.createFile(dir1.resolve("b.zip"));

		Files.createDirectory(base.resolve("out"));
	}

	@AfterEach
	void tearDown() throws Exception {
		DataManager.removeDir(base);
	}

	@Test
	void test_NoArgs() throws Exception {
		// Exercise
		var sut = new OptionHandler(parse());
		// Verify
		assertEquals(Collections.EMPTY_MAP, sut.zipMap());
	}

	@Test
	void test_1File() throws Exception {
		// Exercise
		var sut = new OptionHandler(parse(base + "/a.zip"));
		// Verify
		var expected = Map.of(base.resolve("a.zip"), base.resolve("a.pdf"));
		assertEquals(expected, sut.zipMap());
	}

	@Test
	void test_2Files() throws Exception {
		// Exercise
		var sut = new OptionHandler(parse(base + "/a.zip", base + "/dir1/b.zip"));
		// Verify
		var expected = Map.of( //
				base.resolve("a.zip"), base.resolve("a.pdf"), //
				base.resolve("dir1/b.zip"), base.resolve("dir1/b.pdf"));
		assertEquals(expected, sut.zipMap());
	}

	@Test
	void test_NoSuchFile() throws Exception {
		// Exercise & Verify
		assertThrows(NoSuchFileException.class, () -> new OptionHandler(parse("x.zip")));
	}

	@Test
	void test_dOption() throws Exception {
		// Exercise
		var sut = new OptionHandler(parse("-d", base + "/out", base + "/a.zip", base + "/dir1/b.zip"));
		// Verify
		var expected = Map.of( //
				base.resolve("a.zip"), base.resolve("out/a.pdf"), //
				base.resolve("dir1/b.zip"), base.resolve("out/b.pdf"));
		assertEquals(expected, sut.zipMap());
	}

	@Test
	void test_dOption_NoSuchDir() throws Exception {
		// Exercise & Verify
		assertThrows(NoSuchFileException.class, () -> new OptionHandler(parse("-d", "no_such_dir")));
	}

	@Test
	void test_rOption_Default() throws Exception {
		// Exercise
		var sut = new OptionHandler(parse(base + "/a.zip"));
		// Verify
		assertFalse(sut.isRightToLeft());
	}

	@Test
	void test_rOption() throws Exception {
		// Exercise
		var sut = new OptionHandler(parse("-r", base + "/a.zip"));
		// Verify
		assertTrue(sut.isRightToLeft());
	}

	OptionParser parse(String... args) {
		return CommandLine.populateCommand(new OptionParser(), args);
	}
}
