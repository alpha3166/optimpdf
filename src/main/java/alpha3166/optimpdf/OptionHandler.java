package alpha3166.optimpdf;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class OptionHandler {
	private static final String VERSION = "1.0.0";
	private static final String COMMAND = String.format("java -jar optimpdf-%s.jar", VERSION);

	private Map<Path, Path> pdfMap;
	private boolean forceOverwrite;
	private BitSet targetPages;
	private int screenWidth;
	private int screenHeight;
	private int doublePageThreshold;
	private int quality;
	private boolean bleachAll;
	private BitSet bleachPages;
	private boolean dryRun;
	private boolean quiet;
	private int numberOfThreads;

	public OptionHandler(String... args) throws IOException, ParseException {
		var options = new Options();
		options.addOption("h", "display this help and exit");
		options.addOption("s", true, "output file suffix (Default: _r)");
		options.addOption("d", true, "output directory");
		options.addOption("o", true, "output file name (for single input only)");
		options.addOption("u", "process only when the source PDF is newer than the"
				+ " destination PDF or when the destination PDF is missing. enable -f");
		options.addOption("l", "display the list of PDFs to be processed and exit");
		options.addOption("f", "overwrite output file");
		options.addOption("p", true, "process specified pages only (eg: 1,3-5)");
		options.addOption("x", true, "output screen size (Default: 1536x2048)");
		options.addOption("w", true, "double-page size threshold. halve output screen"
				+ " size if source JPEG is smaller than this (Default: 2539)");
		options.addOption("Q", true, "JPEG quality (Default: 50)");
		options.addOption("b", true, "bleach specified pages (eg: 1,3-5 or all)");
		options.addOption("n", "dry-run (skip saving new PDFs)");
		options.addOption("q", "suppress displaying info of each page");
		options.addOption("t", true, "number of threads to use (Default: 8)");

		// Parse
		var cmd = new DefaultParser().parse(options, args);

		// Handle -h
		if (cmd.hasOption("h")) {
			new HelpFormatter().printHelp(COMMAND + " [OPTIONS] PDF...", options);
			System.exit(0);
		}

		// Handle arguments
		var pdfSet = new TreeSet<Path>();
		for (var arg : cmd.getArgs()) {
			Files.walkFileTree(Paths.get(arg), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (file.getFileName().toString().toLowerCase().endsWith(".pdf")) {
						pdfSet.add(file);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		// Make pdfMap with handling -s
		pdfMap = new TreeMap<>();
		var suffix = cmd.getOptionValue("s", "_r");
		for (var pdf : pdfSet) {
			var newPdfName = pdf.getFileName().toString().replaceFirst("(\\.\\w+)?$", suffix + "$0");
			var newPdf = pdf.resolveSibling(newPdfName);
			pdfMap.put(pdf, newPdf);
		}

		// Handle -d
		if (cmd.hasOption("d")) {
			var outDir = Paths.get(cmd.getOptionValue("d"));
			if (!Files.isDirectory(outDir)) {
				throw new IllegalArgumentException("-d " + cmd.getOptionValue("d"));
			}
			pdfMap.replaceAll((k, v) -> outDir.resolve(v));
		}

		// Handle -o
		if (cmd.hasOption("o")) {
			if (pdfMap.size() > 1) {
				throw new IllegalArgumentException("-o is for single input only");
			}
			var newPdf = Paths.get(cmd.getOptionValue("o"));
			pdfMap.replaceAll((k, v) -> newPdf);
		}

		// Handle -u
		if (cmd.hasOption("u")) {
			forceOverwrite = true;
			pdfMap.entrySet().removeIf(entry -> {
				try {
					return Files.exists(entry.getValue()) && Files.getLastModifiedTime(entry.getKey())
							.compareTo(Files.getLastModifiedTime(entry.getValue())) < 0;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}

		// Handle -l
		if (cmd.hasOption("l")) {
			pdfMap.entrySet().stream().forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));
			System.exit(1);
		}

		// Handle -f
		if (!cmd.hasOption("u")) {
			forceOverwrite = cmd.hasOption("f");
			if (!forceOverwrite) {
				for (var newPdf : pdfMap.values()) {
					if (Files.exists(newPdf)) {
						throw new IllegalArgumentException(newPdf + " already exists.");
					}
				}
			}
		}

		// Handle -p
		if (cmd.hasOption("p")) {
			try {
				targetPages = parsePageDesignator(cmd.getOptionValue("p"));
			} catch (Exception e) {
				throw new IllegalArgumentException("-p " + cmd.getOptionValue("p"));
			}
		}

		// Handle -x
		var screenSize = cmd.getOptionValue("x", "1536x2048");
		var tokens = screenSize.split("x", -1);
		if (tokens.length != 2) {
			throw new IllegalArgumentException("-x " + cmd.getOptionValue("x"));
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
			throw new IllegalArgumentException("-x " + cmd.getOptionValue("x"), e);
		}

		// Handle -w
		try {
			doublePageThreshold = Integer.parseInt(cmd.getOptionValue("w", "2539"));
		} catch (Exception e) {
			throw new IllegalArgumentException("-w " + cmd.getOptionValue("w"), e);
		}

		// Handle -Q
		try {
			quality = Integer.parseInt(cmd.getOptionValue("Q", "50"));
		} catch (Exception e) {
			throw new IllegalArgumentException("-Q " + cmd.getOptionValue("Q"), e);
		}

		// Handle -b
		if (cmd.hasOption("b")) {
			try {
				if (cmd.getOptionValue("b").toLowerCase().equals("all")) {
					bleachAll = true;
				} else {
					bleachPages = parsePageDesignator(cmd.getOptionValue("b"));
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("-b " + cmd.getOptionValue("b"), e);
			}
		}

		// Handle -n
		dryRun = cmd.hasOption("n");

		// Handle -q
		quiet = cmd.hasOption("q");

		// Handle -t
		try {
			numberOfThreads = Integer.parseInt(cmd.getOptionValue("t", "8"));
		} catch (Exception e) {
			throw new IllegalArgumentException("-t " + cmd.getOptionValue("t"), e);
		}
	}

	private BitSet parsePageDesignator(String designator) {
		var pages = new BitSet();
		var ranges = designator.split(",", -1);
		for (var range : ranges) {
			var tokens = range.split("-", -1);
			if (tokens.length == 1) {
				targetPages.set(Integer.parseInt(tokens[0]));
			} else if (tokens.length == 2) {
				targetPages.set(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]) + 1);
			} else {
				throw new NumberFormatException(designator);
			}
		}
		return pages;
	}

	public Map<Path, Path> pdfMap() {
		return pdfMap;
	}

	public boolean isForceOverwrite() {
		return forceOverwrite;
	}

	public boolean isTargetPage(int page) {
		return targetPages == null || targetPages.get(page);
	}

	public int screenWidth() {
		return screenWidth;
	}

	public int screenHeight() {
		return screenHeight;
	}

	public int doublePageThreshold() {
		return doublePageThreshold;
	}

	public int quality() {
		return quality;
	}

	public boolean isBleachPage(int page) {
		return bleachAll || (bleachPages != null && bleachPages.get(page));
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public int numberOfThreads() {
		return numberOfThreads;
	}
}
