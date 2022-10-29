package alpha3166.optimpdf.reduce;

import picocli.CommandLine.Option;

public class ReduceOption {
  @Option(names = { "-h", "--help" }, usageHelp = true, //
      description = "Show this help message and exit.")
  boolean help;

  @Option(names = { "-P", "--pages" }, //
      description = "Process specified pages only. (eg: 1,3-5)")
  String pages;

  @Option(names = { "-S", "--screen-size" }, //
      description = "Set output screen size. (Default: ${DEFAULT-VALUE})")
  String screenSize = "1536x2048";

  @Option(names = { "-W", "--double-page-threshold" }, //
      description = "double-page size threshold. halve output screen size if the source JPEG is smaller than this. (Default: ${DEFAULT-VALUE})")
  int doublePageThreshold = 2539;

  @Option(names = { "-Q", "--jpeg-quality" }, //
      description = "Set JPEG quality. (Default: ${DEFAULT-VALUE})")
  int jpegQuality = 50;

  @Option(names = { "-B", "--bleach-pages" }, paramLabel = "<pages>", //
      description = "Bleach specified pages. (eg: 1,3-5 or all)")
  String bleachPages;

  @Option(names = { "-T", "--number-of-threads" }, //
      description = "number of threads to use (Default: ${DEFAULT-VALUE})")
  int numberOfThreads = Runtime.getRuntime().availableProcessors();
}
