package alpha3166.optimpdf;

import java.io.IOException;
import java.util.logging.Logger;

public class PageRunner implements Runnable {
	private PdfHandler pdfHandler;
	private int page;
	private OptionHandler opt;

	public PageRunner(PdfHandler pdfHandler, int page, OptionHandler opt) {
		this.pdfHandler = pdfHandler;
		this.page = page;
		this.opt = opt;
	}

	@Override
	public void run() {
		try {
			var logger = Logger.getLogger("");
			var log = new StringBuilder();
			log.append(String.format("  %d/%d", page, pdfHandler.getNumberOfPages()));

			var jpeg = pdfHandler.extractJpeg(page);
			var jpegHandler = new JpegHandler(jpeg);
			var width = jpegHandler.getWidth();
			var height = jpegHandler.getHeight();
			log.append(" " + jpegHandler.desc());

			var maxWidth = 0;
			var maxHeight = 0;
			if (width > opt.doublePageThreshold() || height > opt.doublePageThreshold()) {
				if (width < height) {
					maxWidth = opt.screenWidth();
					maxHeight = opt.screenHeight();
				} else {
					maxWidth = opt.screenHeight();
					maxHeight = opt.screenWidth();
				}
			} else {
				if (width < height) {
					maxWidth = opt.screenHeight() / 2;
					maxHeight = opt.screenWidth();
				} else {
					maxWidth = opt.screenWidth();
					maxHeight = opt.screenHeight() / 2;
				}
			}
			var bleach = opt.isBleachPage(page);
			log.append(String.format(" (fit %dx%d%s)", maxWidth, maxHeight, bleach ? " bleach" : ""));

			var newJpegHandler = jpegHandler.resize(opt.quality(), maxWidth, maxHeight, bleach);
			var newJpegWidth = newJpegHandler.getWidth();
			var newJpegHeight = newJpegHandler.getHeight();
			var newJpegBytes = newJpegHandler.getBytes();
			var newJpegIsGray = newJpegHandler.isGray();
			log.append(String.format(" > %s %d%%", newJpegHandler.desc(), newJpegBytes.length * 100 / jpeg.length));

			pdfHandler.replaceJpeg(page, newJpegBytes, newJpegWidth, newJpegHeight, newJpegIsGray);

			if (!opt.isQuiet()) {
				logger.info(log.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
