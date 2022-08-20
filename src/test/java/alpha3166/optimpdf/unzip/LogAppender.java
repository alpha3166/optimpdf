package alpha3166.optimpdf.unzip;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogAppender extends AppenderBase<ILoggingEvent> {
	public static List<String> logs = new ArrayList<>();

	@Override
	protected void append(ILoggingEvent eventObject) {
		logs.add(eventObject.getMessage());
	}
}
