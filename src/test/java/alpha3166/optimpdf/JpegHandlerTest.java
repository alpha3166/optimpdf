package alpha3166.optimpdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

class JpegHandlerTest {
	byte[] generateJpeg480x640() throws Exception {
		var pb = new ProcessBuilder("magick", "wizard:", "-");
		var p = pb.start();
		var pOut = p.getInputStream();
		var jpeg = new ByteArrayOutputStream();
		var buf = new byte[1024];
		int cnt = 0;
		while ((cnt = pOut.read(buf)) > 0) {
			jpeg.write(buf, 0, cnt);
		}
		return jpeg.toByteArray();
	}

	@Test
	void testJpegHandler() throws Exception {
		// Setup
		byte[] srcJpeg = generateJpeg480x640();
		// Exercise
		var sut = new JpegHandler(srcJpeg);
		// Verify
		assertArrayEquals(srcJpeg, sut.getBytes());
		assertEquals(480, sut.getWidth());
		assertEquals(640, sut.getHeight());
		assertFalse(sut.isGray());
		assertEquals(String.format("480x640 %dK", srcJpeg.length / 1024), sut.desc());
	}

	@Test
	void testResize_FitToMaxWidth() throws Exception {
		// Setup
		byte[] srcJpeg = generateJpeg480x640();
		var sut = new JpegHandler(srcJpeg);
		// Exercise
		var newJpeg = sut.resize(50, 240, 1000, false);
		// Verify
		assertEquals(240, newJpeg.getWidth());
		assertEquals(320, newJpeg.getHeight());
		assertFalse(newJpeg.isGray());
	}

	@Test
	void testResize_FitToMaxHeight() throws Exception {
		// Setup
		byte[] srcJpeg = generateJpeg480x640();
		var sut = new JpegHandler(srcJpeg);
		// Exercise
		var newJpeg = sut.resize(50, 1000, 320, false);
		// Verify
		assertEquals(240, newJpeg.getWidth());
		assertEquals(320, newJpeg.getHeight());
		assertFalse(newJpeg.isGray());
	}

	@Test
	void testResize_NoShrink() throws Exception {
		// Setup
		byte[] srcJpeg = generateJpeg480x640();
		var sut = new JpegHandler(srcJpeg);
		// Exercise
		var newJpeg = sut.resize(50, 1000, 1000, false);
		// Verify
		assertEquals(480, newJpeg.getWidth());
		assertEquals(640, newJpeg.getHeight());
		assertFalse(newJpeg.isGray());
	}

	@Test
	void testResize_Bleach() throws Exception {
		// Setup
		byte[] srcJpeg = generateJpeg480x640();
		var sut = new JpegHandler(srcJpeg);
		// Exercise
		var newJpeg = sut.resize(50, 240, 320, true);
		// Verify
		assertEquals(240, newJpeg.getWidth());
		assertEquals(320, newJpeg.getHeight());
//		assertTrue(newJpeg.isGray());
	}
}