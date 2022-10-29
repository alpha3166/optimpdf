package alpha3166.optimpdf.framework;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Mixin;

public abstract class AbstractCommandMain implements Callable<Integer> {
  Logger logger = LoggerFactory.getLogger(getClass());

  @Mixin
  CommonOption cOpt;

  @Override
  public Integer call() throws Exception {
    var srcSet = decideSrcSet(cOpt.srcPaths, cOpt.recursive);
    var fileMap = decideDestNames(srcSet, cOpt.outfile, cOpt.suffix);

    adjustFileMap(fileMap);

    if (cOpt.outdir != null) {
      rewriteDestDir(fileMap, cOpt.outdir);
    }

    if (cOpt.update) {
      pruneNewerDest(fileMap);
      cOpt.force = true;
    }

    if (cOpt.list) {
      fileMap.entrySet().stream().forEach(e -> logger.info(e.getKey() + " -> " + e.getValue()));
      return 0;
    }

    handleLocalOption(fileMap);

    for (var src : fileMap.keySet()) {
      logger.info(src.toString());
      var dest = fileMap.get(src);

      var destDir = dest.toAbsolutePath().getParent(); // toAbsolutePath() prevents bare filename's parent be null
      var temp = Files.createTempFile(destDir, src.getFileName() + ".", ".pdf");

      var updated = processFile(src, temp, cOpt.dryRun, cOpt.quiet);

      logger.info("  -> " + dest);
      if (cOpt.dryRun) {
        Files.delete(temp);
      } else {
        if (Files.exists(dest)) {
          if (!cOpt.force) {
            Files.delete(temp);
            throw new FileAlreadyExistsException(dest.toString());
          }
          if (updated) {
            Files.move(temp, dest, StandardCopyOption.REPLACE_EXISTING);
          } else {
            Files.delete(temp); // keep original
          }
        } else {
          Files.move(temp, dest);
        }
      }
    }

    return 0;
  }

  Set<Path> decideSrcSet(Path[] srcPaths, boolean recursive) throws IOException {
    final Set<Path> srcSet = new TreeSet<Path>();
    if (recursive) {
      for (var src : srcPaths) {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            srcSet.add(file);
            return FileVisitResult.CONTINUE;
          }
        });
      }
    } else {
      for (var src : srcPaths) {
        if (!Files.isRegularFile(src)) {
          throw new NoSuchFileException(src.toString());
        }
        srcSet.add(src);
      }
    }
    return srcSet;
  }

  Map<Path, Path> decideDestNames(Set<Path> srcSet, Path outfile, String suffix) {
    Map<Path, Path> fileMap = new TreeMap<>();
    for (var src : srcSet) {
      var destName = src.getFileName().toString().replaceFirst("(\\.\\w+)?$", suffix + "$0");
      var dest = src.resolveSibling(destName);
      fileMap.put(src, dest);
    }
    if (outfile != null) {
      if (fileMap.size() > 1) {
        throw new IllegalArgumentException("--outfile is for single input only");
      }
      fileMap.replaceAll((k, v) -> outfile);
    }
    return fileMap;
  }

  protected void adjustFileMap(Map<Path, Path> fileMap) throws Exception {
  }

  void rewriteDestDir(Map<Path, Path> fileMap, Path outdir) throws NoSuchFileException {
    if (!Files.isDirectory(outdir)) {
      throw new NoSuchFileException(outdir.toString());
    }
    fileMap.replaceAll((k, v) -> outdir.resolve(v.getFileName()));
  }

  void pruneNewerDest(Map<Path, Path> fileMap) {
    fileMap.entrySet().removeIf(entry -> {
      try {
        return Files.exists(entry.getValue()) && Files.getLastModifiedTime(entry.getKey())
            .compareTo(Files.getLastModifiedTime(entry.getValue())) < 0;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  protected void handleLocalOption(Map<Path, Path> fileMap) throws Exception {
  }

  /**
   * @return `true` if the content has updated, `false` otherwise.
   */
  protected abstract boolean processFile(Path src, Path temp, boolean dryRun, boolean quiet) throws Exception;
}
