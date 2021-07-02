package alpha3166.optimpdf;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {
	List<LogRecord> logs = new ArrayList<>();

	@Override
	public void publish(LogRecord record) {
		logs.add(record);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

	public int getLogCount() {
		return logs.size();
	}

	public String getLog(int lineNo) {
		return logs.get(lineNo).getMessage();
	}
}
