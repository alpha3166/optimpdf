package alpha3166.optimpdf.reduce;

public class ImageMagick {
  public static String COMMAND;

  static {
    COMMAND = "convert";
    try {
      var pb = new ProcessBuilder("magick", "-version");
      var p = pb.start();
      if (p.waitFor() == 0) {
        COMMAND = "magick";
      }
    } catch (Exception e) {
      // ignore
    }
  }
}
