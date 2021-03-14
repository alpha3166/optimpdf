package alpha3166.optimpdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class JpegHandler {
	private byte[] bytes;
	private BufferedImage bufferedImage;

	public JpegHandler(byte[] bytes) throws IOException {
		this.bytes = bytes;
		// ImageIO.read() is NOT thread safe
		synchronized (ImageIO.class) {
			bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
		}
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getWidth() {
		return bufferedImage.getWidth();
	}

	public int getHeight() {
		return bufferedImage.getHeight();
	}

	public boolean isGray() {
		return bufferedImage.getType() == BufferedImage.TYPE_BYTE_GRAY;
	}

	public String desc() {
		return String.format("%dx%d %dK%s", getWidth(), getHeight(), bytes.length / 1024, isGray() ? " gray" : "");
	}

	public JpegHandler resize(int quality, int maxWidth, int maxHeight, boolean bleach) throws IOException {
		var cmd = new ArrayList<String>();
		cmd.add("magick");
		cmd.add("-"); // STDIN
		cmd.add("-quality");
		cmd.add(Integer.toString(quality));
		cmd.add("-resize");
		cmd.add(String.format("%dx%d>", maxWidth, maxHeight));
		if (bleach) {
			cmd.add("-channel");
			cmd.add("Red");
			cmd.add("-separate");
			cmd.add("-modulate");
			cmd.add("110");
		}
		cmd.add("-"); // STDOUT

		var pb = new ProcessBuilder(cmd);
		var p = pb.start();

		var pErrReader = new AsynchronousProcessStderrReader(p.getErrorStream());
		var pErrReaderThread = new Thread(pErrReader);
		pErrReaderThread.start();

		var pOutReader = new AsynchronousProcessOutputReader(p.getInputStream());
		var pOutReaderThread = new Thread(pOutReader);
		pOutReaderThread.start();

		var pIn = p.getOutputStream();
		pIn.write(bytes);
		pIn.close();

		try {
			p.waitFor();
			pOutReaderThread.join();
			pErrReaderThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return new JpegHandler(pOutReader.getBytes());
	}
}

class AsynchronousProcessOutputReader implements Runnable {
	private InputStream pOut;
	private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

	public AsynchronousProcessOutputReader(InputStream pOut) {
		this.pOut = pOut;
	}

	@Override
	public void run() {
		try {
			byte[] buf = new byte[1024 * 1024];
			int cnt = 0;
			while ((cnt = pOut.read(buf)) > 0) {
				bytes.write(buf, 0, cnt);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] getBytes() {
		return bytes.toByteArray();
	}
}

class AsynchronousProcessStderrReader implements Runnable {
	private InputStream pErr;

	public AsynchronousProcessStderrReader(InputStream pErr) {
		this.pErr = pErr;
	}

	@Override
	public void run() {
		try {
			byte[] buf = new byte[1024];
			int cnt = 0;
			while ((cnt = pErr.read(buf)) > 0) {
				System.err.write(buf, 0, cnt);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
