package com.enjin.officialplugin;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

public class EnjinLogAppender implements Appender {

	String lastline = "";
	
	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void append(LogEvent event) {
		lastline = event.getMessage().getFormattedMessage();
		//remove control characters
		lastline = lastline.substring(0, lastline.length()-3);
		lastline = lastline.replaceAll("\\p{Cntrl}", "");
	}

	@Override
	public ErrorHandler getHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Layout<? extends Serializable> getLayout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "EnjinLogListener";
	}

	@Override
	public boolean ignoreExceptions() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHandler(ErrorHandler arg0) {
		// TODO Auto-generated method stub

	}
	
	public String getLastLine() {
		return lastline;
	}

}
