package alpha3166.optimpdf.reduce;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;

public class OptionHandler {
	Logger logger = LoggerFactory.getLogger(getClass());

	private boolean abort;
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

	public OptionHandler(String... args) throws IOException {
		// Parse
		var cmd = CommandLine.populateCommand(new OptionParser(), args);

		// Handle -h
		if (cmd.help) {
			CommandLine.usage(cmd, System.out);
			abort = true;
			return;
		}

		// Handle arguments
		var pdfSet = new TreeSet<Path>();
		for (var arg : cmd.paths) {
			Files.walkFileTree(arg, new SimpleFileVisitor<Path>() {
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
		var suffix = cmd.suffix;
		for (var pdf : pdfSet) {
			var newPdfName = pdf.getFileName().toString().replaceFirst("(\\.\\w+)?$", suffix + "$0");
			var newPdf = pdf.resolveSibling(newPdfName);
			pdfMap.put(pdf, newPdf);
		}

		// Handle -d
		if (cmd.directory != null) {
			var outDir = Paths.get(cmd.directory);
			if (!Files.isDirectory(outDir)) {
				throw new NoSuchFileException("-d " + cmd.directory);
			}
			pdfMap.replaceAll((k, v) -> outDir.resolve(v.getFileName()));
		}

		// Handle -o
		if (cmd.outputFile != null) {
			if (pdfMap.size() > 1) {
				throw new IllegalArgumentException("-o is for single input only");
			}
			var newPdf = Paths.get(cmd.outputFile);
			pdfMap.replaceAll((k, v) -> newPdf);
		}

		// Handle -u
		if (cmd.update) {
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
		if (cmd.list) {
			pdfMap.entrySet().stream().forEach(e -> logger.info(e.getKey() + " -> " + e.getValue()));
			abort = true;
			return;
		}

		// Handle -f
		if (!cmd.update) {
			forceOverwrite = cmd.force;
			if (!forceOverwrite) {
				for (var newPdf : pdfMap.values()) {
					if (Files.exists(newPdf)) {
						throw new FileAlreadyExistsException(newPdf.toString());
					}
				}
			}
		}

		// Handle -p
		if (cmd.pages != null) {
			try {
				targetPages = parsePageDesignator(cmd.pages);
			} catch (Exception e) {
				throw new IllegalArgumentException("-p " + cmd.pages, e);
			}
		}

		// Handle -x
		var tokens = cmd.screenSize.split("x", -1);
		if (tokens.length != 2) {
			throw new IllegalArgumentException("-x " + cmd.screenSize);
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
			throw new IllegalArgumentException("-x " + cmd.screenSize, e);
		}

		// Handle -w
		doublePageThreshold = cmd.doublePageThreshold;

		// Handle -Q
		quality = cmd.quality;

		// Handle -b
		if (cmd.bleachPages != null) {
			try {
				if (cmd.bleachPages.toLowerCase().equals("all")) {
					bleachAll = true;
				} else {
					bleachPages = parsePageDesignator(cmd.bleachPages);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("-b " + cmd.bleachPages, e);
			}
		}

		// Handle -n
		dryRun = cmd.dryRun;

		// Handle -q
		quiet = cmd.quiet;

		// Handle -t
		if (cmd.numberOfThreads == null) {
			numberOfThreads = Runtime.getRuntime().availableProcessors();
		} else {
			numberOfThreads = cmd.numberOfThreads;
		}
	}

	private BitSet parsePageDesignator(String designator) {
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

	public boolean abort() {
		return abort;
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
