module alpha3166.optimpdf {
	exports alpha3166.optimpdf;

	requires java.desktop;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires info.picocli;
	requires org.slf4j;
	requires io; // com.itextpdf
	requires kernel; // com.itextpdf
	requires layout; // com.itextpdf

	opens alpha3166.optimpdf.framework to info.picocli;
	opens alpha3166.optimpdf.reduce to info.picocli;
	opens alpha3166.optimpdf.rotate to info.picocli;
	opens alpha3166.optimpdf.setview to info.picocli;
	opens alpha3166.optimpdf.unzip to info.picocli;
}
