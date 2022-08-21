package alpha3166.optimpdf.unzip;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZipHandler {
  FileSystem zipFs;

  public ZipHandler(Path zipPath) throws IOException {
    // In case of Java 17, you can write simply as "FileSystems.newFileSystem(zipPath)"
    zipFs = FileSystems.newFileSystem(zipPath, (ClassLoader) null);
  }

  public List<Path> getImagePathList() throws IOException {
    List<Path> imagePathList = new ArrayList<>();
    Files.walkFileTree(zipFs.getPath("/"), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (isImageFile(file)) {
          imagePathList.add(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
    Collections.sort(imagePathList);
    return imagePathList;
  }

  public void close() throws IOException {
    zipFs.close();
  }

  boolean isImageFile(Path path) {
    var fileName = path.getFileName().toString().toLowerCase();
    return fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".png")
        || fileName.endsWith(".gif");
  }
}
