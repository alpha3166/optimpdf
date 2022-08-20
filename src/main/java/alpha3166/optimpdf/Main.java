package alpha3166.optimpdf;

import java.util.concurrent.Callable;

import alpha3166.optimpdf.reduce.ReduceMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "java -jar OPTIMPDF_JAR", version = "OptimPDF 2.0.0", subcommands = {
    ReduceMain.class }, mixinStandardHelpOptions = true)
public class Main implements Callable<Integer> {
  public static void main(String... args) {
    System.exit(new CommandLine(new Main()).execute(args));
  }

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
