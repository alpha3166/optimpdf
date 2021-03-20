package alpha3166.optimpdf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfImageObject;

public class PdfHandler {
	private PdfReader reader;

	public PdfHandler(Path path) throws IOException {
		reader = new PdfReader(new FileInputStream(path.toFile()));
	}

	private PRStream getStream(int page) {
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
		var pageRes = reader.getPageResources(page);
		var pageXObj = pageRes.getAsDict(PdfName.XOBJECT);
		int imgObjNum = 0;
		for (var key : pageXObj.getKeys()) {
			var imgRef = pageXObj.getAsIndirectObject(key);
			imgObjNum = imgRef.getNumber();
			break;
		}
		return (PRStream) reader.getPdfObject(imgObjNum);
	}

	public synchronized byte[] extractJpeg(int page) throws IOException {
		var stream = getStream(page);
		var imgObj = new PdfImageObject(stream);
		return imgObj.getImageAsBytes();
	}

	public synchronized void replaceJpeg(int page, byte[] newJpeg, int newWidth, int newHeight, boolean isGray) {
		var stream = getStream(page);
		stream.setData(newJpeg, false, PRStream.NO_COMPRESSION);
		stream.put(PdfName.WIDTH, new PdfNumber(newWidth));
		stream.put(PdfName.HEIGHT, new PdfNumber(newHeight));
		stream.put(PdfName.FILTER, PdfName.DCTDECODE);
		if (isGray) {
			stream.put(PdfName.COLORSPACE, PdfName.DEVICEGRAY);
		}
	}

	public void save(Path path) throws IOException, DocumentException {
		var out = new FileOutputStream(path.toFile());
		var stamper = new PdfStamper(reader, out);
		stamper.close();
		reader.close();
	}

	public int getNumberOfPages() {
		return reader.getNumberOfPages();
	}
}
