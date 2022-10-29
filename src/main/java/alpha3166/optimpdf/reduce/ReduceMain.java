package alpha3166.optimpdf.reduce;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import alpha3166.optimpdf.framework.AbstractCommandMain;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "reduce", description = "Reduce image size in PDF to fit handheld devices.")
public class ReduceMain extends AbstractCommandMain {
  @Mixin
  ReduceOption opt;

  BitSet targetPages;
  int screenWidth;
  int screenHeight;
  boolean bleachAll;
  BitSet bleachPages;

  @Override
  protected void handleLocalOption(Map<Path, Path> fileMap) throws Exception {
    // Handle --pages
    if (opt.pages != null) {
      try {
        targetPages = parsePageDesignator(opt.pages);
      } catch (Exception e) {
        throw new IllegalArgumentException("--pages " + opt.pages, e);
      }
    }

    // Handle --screen-size
    var tokens = opt.screenSize.split("x", -1);
    if (tokens.length != 2) {
      throw new IllegalArgumentException("--screen-size " + opt.screenSize);
    }
    try {
      screenWidth = Integer.parseInt(tokens[0]);
      screenHeight = Integer.parseInt(tokens[1]);
      // be always portrait, not landscape
      if (screenWidth > screenHeight) {
        var tmp = screenWidth;
        screenWidth = screenHeight;
        screenHeight = tmp;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("--screen-size " + opt.screenSize, e);
    }

    // Handle --bleach-pages
    if (opt.bleachPages != null) {
      try {
        if (opt.bleachPages.toLowerCase().equals("all")) {
          bleachAll = true;
        } else {
          bleachPages = parsePageDesignator(opt.bleachPages);
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("--bleach-pages " + opt.bleachPages, e);
      }
    }
  }

  BitSet parsePageDesignator(String designator) {
    var pages = new BitSet();
    var ranges = designator.split(",", -1);
    for (var range : ranges) {
      var tokens = range.split("-", -1);
      if (tokens.length == 1) {
        pages.set(Integer.parseInt(tokens[0]));
      } else if (tokens.length == 2) {
        pages.set(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]) + 1);
      } else {
        throw new NumberFormatException(designator);
      }
    }
    if (pages.get(0)) {
      throw new IndexOutOfBoundsException("PDF pages start from 1");
    }
    return pages;
  }

  @Override
  protected boolean processFile(Path src, Path temp, boolean dryRun, boolean quiet) throws Exception {
    try (var pdfHandler = dryRun ? new PdfHandler(src) : new PdfHandler(src, temp)) {
      var threadPool = Executors.newFixedThreadPool(opt.numberOfThreads);
      for (int page = 1; page <= pdfHandler.getNumberOfPages(); page++) {
        if (isTargetPage(page)) {
          var pageRunner = new PageRunner(pdfHandler, page, screenWidth, screenHeight, opt.doublePageThreshold,
              opt.jpegQuality, isBleachPage(page), quiet);
          threadPool.execute(pageRunner);
        }
      }
      threadPool.shutdown();
      while (!threadPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
      }
    }
    return true;
  }

  boolean isTargetPage(int page) {
    return targetPages == null || targetPages.get(page);
  }

  boolean isBleachPage(int page) {
    return bleachAll || (bleachPages != null && bleachPages.get(page));
  }
}
