package alpha3166.optimpdf.reduce;

import java.nio.file.Path;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "java -jar OPTIMPDF_JAR", description = "Optimizes PDFs for handheld devices")
public class OptionParser {
	@Option(names = "-h", usageHelp = true, description = "display this help and exit")
	boolean help;

	@Option(names = "-s", description = "output file suffix (Default: ${DEFAULT-VALUE})")
	String suffix = "_r";

	@Option(names = "-d", description = "output directory")
	String directory;

	@Option(names = "-o", description = "output file name (for single input only). disable -d")
	String outputFile;

	@Option(names = "-u", description = "process only when the source PDF is newer than the destination PDF or when the destination PDF is missing. enable -f")
	boolean update;

	@Option(names = "-l", description = "display the list of PDFs to be processed and exit")
	boolean list;

	@Option(names = "-f", description = "overwrite output file")
	boolean force;

	@Option(names = "-p", description = "process specified pages only (eg: 1,3-5)")
	String pages;

	@Option(names = "-x", description = "output screen size (Default: ${DEFAULT-VALUE})")
	String screenSize = "1536x2048";

	@Option(names = "-w", description = "double-page size threshold. halve output screen size if source JPEG is smaller than this (Default: ${DEFAULT-VALUE})")
	Integer doublePageThreshold = 2539;

	@Option(names = "-Q", description = "JPEG quality (Default: ${DEFAULT-VALUE})")
	Integer quality = 50;

	@Option(names = "-b", description = "bleach specified pages (eg: 1,3-5 or all)")
	String bleachPages;

	@Option(names = "-n", description = "dry-run (skip saving new PDFs)")
	boolean dryRun;

	@Option(names = "-q", description = "suppress displaying info of each page")
	boolean quiet;

	@Option(names = "-t", description = "number of threads to use (Default: Number of CPU cores)")
	Integer numberOfThreads;

	@Parameters(paramLabel = "PDF_OR_DIR", description = "target PDFs or directories")
	Path[] paths = new Path[0];
}
