package alpha3166.optimpdf;

import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main {
	public static void main(String... args) throws Exception {
		var logger = Logger.getLogger("");
		for (var handler : logger.getHandlers()) {
			handler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage() + "\n";
				}
			});
		}

		var opt = new OptionHandler(args);
		var pdfMap = opt.pdfMap();
		for (var pdf : pdfMap.keySet()) {
			logger.info(pdf.toString());
			var pdfHandler = new PdfHandler(pdf);
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

			var newPdf = pdfMap.get(pdf);
			if (!opt.isForceOverwrite() && Files.exists(newPdf)) {
				throw new IllegalStateException(newPdf + " already exists");
			}
			logger.info("  -> " + newPdf);
			if (!opt.isDryRun()) {
				pdfHandler.save(newPdf);
			}
		}
	}
}
