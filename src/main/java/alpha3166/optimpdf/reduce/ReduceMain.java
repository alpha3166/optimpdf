package alpha3166.optimpdf.reduce;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "reduce", description = "Optimizes PDFs for handheld devices")
public class ReduceMain implements Callable<Integer> {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Mixin
	OptionParser arg;

	@Override
	public Integer call() throws Exception {
		var opt = new OptionHandler(arg);
		if (opt.abort()) {
			return 0;
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
		return 0;
	}
}
