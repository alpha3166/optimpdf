package alpha3166.optimpdf;

import java.nio.file.FileAlreadyExistsException;
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
			PdfHandler pdfHandler = null;
			if (opt.isDryRun()) {
				pdfHandler = new PdfHandler(pdf);
			} else {
				pdfHandler = new PdfHandler(pdf, newPdf);
			}
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
			pdfHandler.close();
		}
	}
}
