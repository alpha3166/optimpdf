package alpha3166.optimpdf.reduce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AsyncProcessOutputReader implements Runnable {
  private InputStream pOut;
  private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

  public AsyncProcessOutputReader(InputStream pOut) {
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
