package com.enjin.officialplugin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class EnjinLogHandler extends Handler implements EnjinLogInterface {

	String lastline = "";
	
	@Override
	public String getLastLine() {
		return lastline;
	}

	@Override
	public void close() throws SecurityException {
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord record) {
		if(record.getMessage() == null) {
			return;
		}
		lastline = record.getMessage();
		//remove control characters
		lastline = lastline.replaceAll("\\p{Cntrl}.{2}", "");
		//lastline = lastline.substring(0, lastline.length()-3);
		lastline = lastline.replaceAll("\\p{Cntrl}", "");
	}
}
