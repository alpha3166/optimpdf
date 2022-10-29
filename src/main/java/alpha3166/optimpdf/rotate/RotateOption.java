package alpha3166.optimpdf.rotate;

import java.nio.file.Path;

import picocli.CommandLine.Option;

public class RotateOption {
    @Option(names = { "-h", "--help" }, usageHelp = true, //
            description = "Show this help message and exit.")
    boolean help;

    @Option(names = { "-D", "--degree" }, paramLabel = "<degree>", //
            description = "Set all page's rotation to the <degree>. Must be one of 0, 90, 180, or 270. (Default: ${DEFAULT-VALUE})")
    int degree = 0;

    @Option(names = { "-R", "--ref-pdf" }, paramLabel = "<pdf>", //
            description = "Match the rotation of each page to the referenced <pdf> file.")
    Path refPdf;
}
