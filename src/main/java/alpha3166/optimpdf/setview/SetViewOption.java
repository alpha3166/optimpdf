package alpha3166.optimpdf.setview;

import picocli.CommandLine.Option;

public class SetViewOption {
  @Option(names = { "-h", "--help" }, usageHelp = true, //
      description = "Set PDF initial view.")
  boolean help;

  @Option(names = { "--clear" }, //
      description = "Remove OpenAction, PageLayout, ViewerPreferences from catalog befor editing.")
  boolean clear;

  @Option(names = { "-L", "--page-layout" }, paramLabel = "<layout>", //
      description = "Set page layout. <layout> must be one of SinglePage, OneColumn, TwoColumnLeft, TwoColumnRight, TwoPageLeft, TwoPageRight.")
  String pageLayout;

  @Option(names = { "-R", "--right-to-left" }, //
      description = "Set direction to right-to-left.")
  boolean rightToLeft;

  @Option(names = { "-F", "--fit" }, //
      description = "Zoom to show entire page.")
  boolean fit;
}
