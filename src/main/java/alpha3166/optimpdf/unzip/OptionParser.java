package alpha3166.optimpdf.unzip;

import java.nio.file.Path;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class OptionParser {
  @Option(names = "-h", usageHelp = true, description = "display this help and exit")
  boolean help;

  @Option(names = "-d", description = "output directory")
  Path outDir;

  @Option(names = "-r", description = "set direction to right-to-left")
  boolean rightToLeft;

  @Parameters(paramLabel = "TARGET_ZIP")
  Path[] paths = new Path[0];
}
