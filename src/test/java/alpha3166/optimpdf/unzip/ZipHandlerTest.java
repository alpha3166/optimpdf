package alpha3166.optimpdf.unzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZipHandlerTest {
	Path base;

	@BeforeEach
	void setUp() throws Exception {
		base = DataManager.makeTestDir();
		DataManager.generateZip(base.resolve("sample.zip"));
	}

	@AfterEach
	void tearDown() throws Exception {
		DataManager.removeDir(base);
	}

	@Test
	void testZipHandler() throws Exception {
		// Exercise
		var sut = new ZipHandler(base.resolve("sample.zip"));
		// Verify
		assertNotNull(sut);
		// Teardown
		sut.close();
	}

	@Test
	void testGetImagePathList() throws Exception {
		// Setup
		var sut = new ZipHandler(base.resolve("sample.zip"));
		// Exercise
		var actual = sut.getImagePathList();
		// Verify
		assertEquals(4, actual.size());
		assertEquals("/dir1/sample1.jpg", actual.get(0).toString());
		assertEquals("/dir1/sample2.jpeg", actual.get(1).toString());
		assertEquals("/dir1/sample3.png", actual.get(2).toString());
		assertEquals("/sample4.gif", actual.get(3).toString());
		// Teardown
		sut.close();
	}
}
