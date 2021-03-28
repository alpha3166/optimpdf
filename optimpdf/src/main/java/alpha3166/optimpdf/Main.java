package alpha3166.optimpdf;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main {
	Logger logger;

	public static void main(String... args) throws Exception {
		var self = new Main();
		self.logger = setupLogger();
		self.execute(args);
	}

	public void execute(String... args) throws Exception {
		var opt = new OptionHandler(args);
		if (opt.abort()) {
			return;
		}
		var pdfMap = opt.pdfMap();
		for (var pdf : pdfMap.keySet()) {
			var newPdf = pdfMap.get(pdf);
			if (!opt.isForceOverwrite() && Files.exists(newPdf)) {
				throw new FileAlreadyExistsException(newPdf.toString());
			}
			logger.info(pdf.toString());
			try (var pdfHandler = opt.isDryRun() ? new PdfHandler(pdf) : new PdfHandler(pdf, newPdf)) {
				var threadPool = Executors.newFixedThreadPool(opt.numberOfThreads());
				for (int page = 1; page <= pdfHandler.getNumberOfPages(); page++) {
					if (opt.isTargetPage(page)) {
						var pageRunner = new PageRunner(pdfHandler, page, opt);
						threadPool.execute(pageRunner);
					}
				}
				threadPool.shutdown();
				while (!threadPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				}
				logger.info("  -> " + newPdf);
			}
		}
	}

	public static Logger setupLogger() {
		var logger = Logger.getLogger("");
		for (var handler : logger.getHandlers()) {
			logger.removeHandler(handler); // Remove default ConsoleHandler
		}
		logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				System.out.println(record.getMessage());
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {
			}
		});
		return logger;
	}
}
