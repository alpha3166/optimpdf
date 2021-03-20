package alpha3166.optimpdf;

public class ImageMagickHelper {
	public static byte[] exec(String args) throws Exception {
		var pb = new ProcessBuilder(("magick " + args).split(" "));
		var p = pb.start();

		var pErrReader = new AsyncProcessStderrReader(p.getErrorStream());
		var pErrReaderThread = new Thread(pErrReader);
		pErrReaderThread.start();

		var pOutReader = new AsyncProcessOutputReader(p.getInputStream());
		var pOutReaderThread = new Thread(pOutReader);
		pOutReaderThread.start();

		try {
			p.waitFor();
			pOutReaderThread.join();
			pErrReaderThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return pOutReader.getBytes();
	}
}
