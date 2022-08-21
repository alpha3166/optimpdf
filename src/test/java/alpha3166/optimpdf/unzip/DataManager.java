package alpha3166.optimpdf.unzip;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class DataManager {
  public static Path makeTestDir() throws IOException {
    return Files.createTempDirectory(Paths.get(""), "junit");
  }

  public static void removeDir(Path dir) throws IOException {
    Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  public static void generateZip(Path path) throws IOException, URISyntaxException {
    Map<String, Object> env = new HashMap<>();
    env.put("create", "true");
    // In case of Java 17, you can write simply as "FileSystems.newFileSystem(path, env)"
    try (var zipFs = FileSystems.newFileSystem(URI.create("jar:file:" + path.toAbsolutePath()), env)) {
      writeImage(zipFs, "sample4.gif", "gif");
      Files.createDirectories(zipFs.getPath("dir1"));
      writeImage(zipFs, "dir1/sample3.png", "png");
      writeImage(zipFs, "dir1/sample2.jpeg", "jpeg");
      writeImage(zipFs, "dir1/sample1.jpg", "jpg");
      try (var out = Files.newOutputStream(zipFs.getPath("dir1/sample0.txt"))) {
        out.write("Hello, World!".getBytes());
      }
    }
  }

  private static void writeImage(FileSystem zipFs, String path, String formatName) throws IOException {
    try (var out = Files.newOutputStream(zipFs.getPath(path))) {
      var img = new BufferedImage(300, 240, BufferedImage.TYPE_3BYTE_BGR);
      var g = img.createGraphics();
      g.drawString(path, 10, 10);
      g.dispose();
      ImageIO.write(img, formatName, out);
    }
  }
}
