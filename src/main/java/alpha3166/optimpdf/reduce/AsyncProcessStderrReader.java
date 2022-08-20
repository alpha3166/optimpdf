package alpha3166.optimpdf.reduce;

import java.io.IOException;
import java.io.InputStream;

public class AsyncProcessStderrReader implements Runnable {
	private InputStream pErr;

	public AsyncProcessStderrReader(InputStream pErr) {
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
