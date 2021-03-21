package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OptionHandlerTest {
	@Nested
	class TargetFilesHandlingTest {
		Path base;

		@BeforeEach
		void setUp() throws Exception {
			base = Files.createTempDirectory(Paths.get(""), "junit");

			Files.createFile(base.resolve("a.pdf"));

			var dir1 = base.resolve("dir1");
			Files.createDirectory(dir1);
			Files.createFile(dir1.resolve("b.pdf"));

			var dir2 = dir1.resolve("dir2");
			Files.createDirectory(dir2);
			Files.createFile(dir2.resolve("c.PDF"));
			Files.createFile(dir2.resolve("漢.pdf"));
			Files.createFile(dir2.resolve("some.txt"));

			Files.createDirectory(base.resolve("out"));
		}

		@AfterEach
		void tearDown() throws Exception {
			Files.walk(base).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}

		@Test
		void test_NoArgs() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertFalse(sut.abort());
			assertEquals(Collections.EMPTY_MAP, sut.pdfMap());
		}

		@Test
		void test_1File() throws Exception {
			// Exercise
			var sut = new OptionHandler(base + "/a.pdf");
			// Verify
			var expected = Map.of(base.resolve("a.pdf"), base.resolve("a_r.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_1File_1Dir() throws Exception {
			// Exercise
			var sut = new OptionHandler(base + "/a.pdf", base + "/dir1");
			// Verify
			var expected = Map.of( //
					base.resolve("a.pdf"), base.resolve("a_r.pdf"), //
					base.resolve("dir1/b.pdf"), base.resolve("dir1/b_r.pdf"), //
					base.resolve("dir1/dir2/c.PDF"), base.resolve("dir1/dir2/c_r.PDF"), //
					base.resolve("dir1/dir2/漢.pdf"), base.resolve("dir1/dir2/漢_r.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_NoSuchFile() throws Exception {
			// Exercise & Verify
			assertThrows(NoSuchFileException.class, () -> new OptionHandler("x.pdf"));
		}

		@Test
		void test_hOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-h", "a.pdf");
			// Verify
			assertTrue(sut.abort());
			assertNull(sut.pdfMap());
		}

		@Test
		void test_sOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-s", "SUFFIX", base + "/a.pdf", base + "/dir1");
			// Verify
			var expected = Map.of( //
					base.resolve("a.pdf"), base.resolve("aSUFFIX.pdf"), //
					base.resolve("dir1/b.pdf"), base.resolve("dir1/bSUFFIX.pdf"), //
					base.resolve("dir1/dir2/c.PDF"), base.resolve("dir1/dir2/cSUFFIX.PDF"), //
					base.resolve("dir1/dir2/漢.pdf"), base.resolve("dir1/dir2/漢SUFFIX.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_dOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-d", base + "/out", base + "/a.pdf", base + "/dir1");
			// Verify
			var expected = Map.of( //
					base.resolve("a.pdf"), base.resolve("out/a_r.pdf"), //
					base.resolve("dir1/b.pdf"), base.resolve("out/b_r.pdf"), //
					base.resolve("dir1/dir2/c.PDF"), base.resolve("out/c_r.PDF"), //
					base.resolve("dir1/dir2/漢.pdf"), base.resolve("out/漢_r.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_dOption_NoSuchDir() throws Exception {
			// Exercise & Verify
			assertThrows(NoSuchFileException.class, () -> new OptionHandler("-d", "no_such_dir"));
		}

		@Test
		void test_oOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-o", base + "/out.pdf", base + "/a.pdf");
			// Verify
			var expected = Map.of(base.resolve("a.pdf"), base.resolve("out.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_oOption_TooManyFiles() throws Exception {
			// Exercise & Verify
			assertThrows(IllegalArgumentException.class,
					() -> new OptionHandler("-o", base + "/out.pdf", base + "/dir1"));
		}

		@Test
		void test_dOption_oOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-d", base + "/out", "-o", base + "/out.pdf", base + "/a.pdf");
			// Verify
			var expected = Map.of(base.resolve("a.pdf"), base.resolve("out.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_uOption() throws Exception {
			// SetUp
			Files.createFile(base.resolve("out/c_r.PDF")); // older
			Thread.sleep(1000); // to be sure the newer is newer on any file system
			Files.createFile(base.resolve("out/b_r.pdf"));
			Files.write(base.resolve("dir1/dir2/c.PDF"), new byte[] { 0x41 }, StandardOpenOption.APPEND);
			// Exercise
			var sut = new OptionHandler("-u", "-d", base + "/out", base + "/a.pdf", base + "/dir1");
			// Verify
			assertTrue(sut.isForceOverwrite());
			var expected = Map.of( //
					base.resolve("a.pdf"), base.resolve("out/a_r.pdf"), //
					base.resolve("dir1/dir2/c.PDF"), base.resolve("out/c_r.PDF"), //
					base.resolve("dir1/dir2/漢.pdf"), base.resolve("out/漢_r.pdf"));
			assertEquals(expected, sut.pdfMap());
		}

		@Test
		void test_lOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-l", base + "/a.pdf", base + "/dir1");
			// Verify
			var expected = Map.of( //
					base.resolve("a.pdf"), base.resolve("a_r.pdf"), //
					base.resolve("dir1/b.pdf"), base.resolve("dir1/b_r.pdf"), //
					base.resolve("dir1/dir2/c.PDF"), base.resolve("dir1/dir2/c_r.PDF"), //
					base.resolve("dir1/dir2/漢.pdf"), base.resolve("dir1/dir2/漢_r.pdf"));
			assertEquals(expected, sut.pdfMap());
			assertTrue(sut.abort());
		}

		@Test
		void test_FileAlreadyExists() throws Exception {
			// SetUp
			Files.createFile(base.resolve("out/b_r.pdf"));
			// Exercise & Verify
			assertThrows(FileAlreadyExistsException.class,
					() -> new OptionHandler("-d", base + "/out", base + "/a.pdf", base + "/dir1"));
		}

		@Test
		void test_fOption() throws Exception {
			// SetUp
			Files.createFile(base.resolve("out/b_r.pdf"));
			// Exercise
			var sut = new OptionHandler("-f", "-d", base + "/out", base + "/a.pdf", base + "/dir1");
			// Verify
			assertTrue(sut.isForceOverwrite());
			var expected = Map.of( //
					base.resolve("a.pdf"), base.resolve("out/a_r.pdf"), //
					base.resolve("dir1/b.pdf"), base.resolve("out/b_r.pdf"), //
					base.resolve("dir1/dir2/c.PDF"), base.resolve("out/c_r.PDF"), //
					base.resolve("dir1/dir2/漢.pdf"), base.resolve("out/漢_r.pdf"));
			assertEquals(expected, sut.pdfMap());
		}
	}

	@Nested
	class ConstantsHandlingTest {
		@Test
		void test_pOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-p", "1,3-5");
			// Verify
			assertTrue(sut.isTargetPage(1));
			assertFalse(sut.isTargetPage(2));
			assertTrue(sut.isTargetPage(3));
			assertTrue(sut.isTargetPage(4));
			assertTrue(sut.isTargetPage(5));
			assertFalse(sut.isTargetPage(6));
			assertFalse(sut.isTargetPage(10000));
		}

		@Test
		void test_pOption_Errors() throws Exception {
			// Exercise & Verify
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "a"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "0"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "3-"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "-5"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "1-3-5"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "5-3"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "-"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", "1,,3-5"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-p", ","));
		}

		@Test
		void test_xOption_Default() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertEquals(1536, sut.screenWidth());
			assertEquals(2048, sut.screenHeight());
		}

		@Test
		void test_xOption_Portrait() throws Exception {
			// Exercise
			var sut = new OptionHandler("-x", "200x300");
			// Verify
			assertEquals(200, sut.screenWidth());
			assertEquals(300, sut.screenHeight());
		}

		@Test
		void test_xOption_Landscape() throws Exception {
			// Exercise
			var sut = new OptionHandler("-x", "300x200");
			// Verify
			assertEquals(200, sut.screenWidth());
			assertEquals(300, sut.screenHeight());
		}

		@Test
		void test_wOption_Default() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertEquals(2539, sut.doublePageThreshold());
		}

		@Test
		void test_wOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-w", "2000");
			// Verify
			assertEquals(2000, sut.doublePageThreshold());
		}

		@Test
		void test_QOption_Default() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertEquals(50, sut.quality());
		}

		@Test
		void test_QOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-Q", "85");
			// Verify
			assertEquals(85, sut.quality());
		}

		@Test
		void test_bOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-b", "1,3-5");
			// Verify
			assertTrue(sut.isBleachPage(1));
			assertFalse(sut.isBleachPage(2));
			assertTrue(sut.isBleachPage(3));
			assertTrue(sut.isBleachPage(4));
			assertTrue(sut.isBleachPage(5));
			assertFalse(sut.isBleachPage(6));
			assertFalse(sut.isBleachPage(10000));
		}

		@Test
		void test_bOption_All() throws Exception {
			// Exercise
			var sut = new OptionHandler("-b", "all");
			// Verify
			assertTrue(sut.isBleachPage(1));
			assertTrue(sut.isBleachPage(2));
			assertTrue(sut.isBleachPage(3));
			assertTrue(sut.isBleachPage(4));
			assertTrue(sut.isBleachPage(5));
			assertTrue(sut.isBleachPage(6));
			assertTrue(sut.isBleachPage(10000));
		}

		@Test
		void test_bOption_Errors() throws Exception {
			// Exercise & Verify
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "a"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "0"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "3-"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "-5"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "1-3-5"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "5-3"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "-"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", "1,,3-5"));
			assertThrows(IllegalArgumentException.class, () -> new OptionHandler("-b", ","));
		}

		@Test
		void test_nOption_Default() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertFalse(sut.isDryRun());
		}

		@Test
		void test_nOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-n");
			// Verify
			assertTrue(sut.isDryRun());
		}

		@Test
		void test_qOption_Default() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertFalse(sut.isQuiet());
		}

		@Test
		void test_qOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-q");
			// Verify
			assertTrue(sut.isQuiet());
		}

		@Test
		void test_tOption_Default() throws Exception {
			// Exercise
			var sut = new OptionHandler();
			// Verify
			assertEquals(8, sut.numberOfThreads());
		}

		@Test
		void test_tOption() throws Exception {
			// Exercise
			var sut = new OptionHandler("-t", "4");
			// Verify
			assertEquals(4, sut.numberOfThreads());
		}
	}
}