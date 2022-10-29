package alpha3166.optimpdf.framework;

import java.nio.file.Path;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class CommonOption {
  @Option(names = { "-o", "--outfile" }, paramLabel = "<file>", //
      description = "Write output to <file>. For single input only.")
  Path outfile;

  @Option(names = { "-s", "--suffix" }, //
      description = "Use source file name + <suffix> as destination file name.")
  String suffix = "";

  @Option(names = { "-d", "--outdir" }, paramLabel = "<dir>", //
      description = "Write destination files to <dir>.")
  Path outdir;

  @Option(names = { "-f", "--force" }, //
      description = "Overwrite destination files.")
  boolean force;

  @Option(names = { "-u", "--update" }, //
      description = "Process only when the source is newer than the destination or when the destination is missing.")
  boolean update;

  @Option(names = { "-n", "--dry-run" }, //
      description = "Dry run (skip saving destination files).")
  boolean dryRun;

  @Option(names = { "-r", "--recursive" }, //
      description = "Process recursively.")
  boolean recursive;

  @Option(names = { "-l", "--list" }, //
      description = "Show the list of files to be processed and exit.")
  boolean list;

  @Option(names = { "-q", "--quiet" }, //
      description = "Suppress displaying info on each page.")
  boolean quiet;

  @Parameters(paramLabel = "<source>", //
      description = "Target files.")
  Path[] srcPaths = new Path[0];
}
