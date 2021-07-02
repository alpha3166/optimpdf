package alpha3166.optimpdf;

import java.io.IOException;
import java.nio.file.Path;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfWriter;

public class PdfHandler implements AutoCloseable {
	private PdfDocument pdfDoc;

	public PdfHandler(Path src) throws IOException {
		var pdfReader = new PdfReader(src.toFile());
		pdfDoc = new PdfDocument(pdfReader);
	}

	public PdfHandler(Path src, Path dest) throws IOException {
		var pdfReader = new PdfReader(src.toFile());
		var pdfWriter = new PdfWriter(dest.toFile());
		pdfDoc = new PdfDocument(pdfReader, pdfWriter);
	}

	private PdfStream getStream(int page) {
		// Assume this kind of object structure for each page:
		// - /ColorSpace:Dictionary
		// - /Font:Dictionary
		// - /ProcSet:[/PDF, /Text, /ImageC]
		// - /XObject:Dictionary
		// - - /Im0:181 0 R
		// - - - /BitsPerComponent:8
		// - - - /ColorSpace:182 0 R
		// - - - /Filter:/DCTDecode
		// - - - /Height:1776
		// - - - /Length:278791
		// - - - /Subtype:/Image
		// - - - /Type:/XObject
		// - - - /Width:1200
		var pdfPage = pdfDoc.getPage(page);
		var pdfRes = pdfPage.getResources();
		var pdfXObj = pdfRes.getResource(PdfName.XObject);
		var imageName = pdfXObj.keySet().iterator().next(); // pick the sole element
		return pdfXObj.getAsStream(imageName);
	}

	public synchronized byte[] extractJpeg(int page) {
		return getStream(page).getBytes();
	}

	public synchronized void replaceJpeg(int page, byte[] newJpeg, int newWidth, int newHeight, boolean isGray) {
		var stream = getStream(page);
		stream.setData(newJpeg);
		stream.put(PdfName.Width, new PdfNumber(newWidth));
		stream.put(PdfName.Height, new PdfNumber(newHeight));
		stream.put(PdfName.Filter, PdfName.DCTDecode);
		if (isGray) {
			stream.put(PdfName.ColorSpace, PdfName.DeviceGray);
		}
	}

	public void close() {
		pdfDoc.close();
	}

	public int getNumberOfPages() {
		return pdfDoc.getNumberOfPages();
	}
}
