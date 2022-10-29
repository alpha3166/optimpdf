package alpha3166.optimpdf.unzip;

import picocli.CommandLine.Option;

public class UnzipOption {
  @Option(names = { "-h", "--help" }, usageHelp = true, //
      description = "Show this help message and exit.")
  boolean help;

  @Option(names = { "-R", "--right-to-left" }, //
      description = "Set direction to right-to-left.")
  boolean rightToLeft;
}
