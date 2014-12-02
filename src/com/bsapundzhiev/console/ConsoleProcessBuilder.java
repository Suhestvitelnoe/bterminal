/**
 * BTerminal - Android terminal emulator
 *
 *  Copyright 2014 by Borislav Sapundzhiev <bsapundjiev@gmail.com>
 * 
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 */
package com.bsapundzhiev.console;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import android.util.Log;

interface IConsoleBuilder {
	void start(String[] params) throws IOException;
	int waitFor() throws InterruptedException;
	void destroy();
}

public class ConsoleProcessBuilder extends Object implements IConsoleBuilder {

	private String DEBUG_TAG = "ConsoleProcessBuilder";
	private ProcessBuilder processBuilder = new ProcessBuilder();
	private Process process;
	private boolean isRunning = false;
	
	public ConsoleProcessBuilder() {
		String initPath ="/data/local/tmp"; //System.getProperty("user.dir");
		processBuilder.directory(new File(initPath));
		processBuilder.redirectErrorStream(true);
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	private void set_isRunning(boolean runnig) {
		  isRunning = runnig;
	}
	
	String getCurrentWorkingDir() {
		
		try {
			String cwd = processBuilder.directory().getCanonicalPath();
			return (cwd.length()  == 0) ?  File.separator: cwd;
		}catch (Exception e) {
			Log.d(DEBUG_TAG, e.getMessage());
			return null;
		}
	}
	
	void changeDir(String path) throws Exception {
	
		if(!path.startsWith(File.separator)) { 	
			path = String.format("%s/%s", getCurrentWorkingDir(), path);
			Log.d(DEBUG_TAG, path);
		}
		
		File dir = new File(path);
		
		if(dir.exists()) {
			processBuilder.directory(dir);
		} else {
			throw new Exception(String.format("%s: no such file or directory", path));
		}
	}
	
	public String getEnv(String key) {

		Map<String, String> environ = processBuilder.environment();
		for (Map.Entry<String, String> e : environ.entrySet()) {
		    
		    if( e.getKey().equalsIgnoreCase(key) ){
		    	
		    	return e.getValue();	
		    }
		}
		return null;
	}
	
	public void setEnv(String key, String val) {

		Map<String, String> environ = processBuilder.environment();
		for (Map.Entry<String, String> e : environ.entrySet()) {
			
			if( e.getKey().equalsIgnoreCase(key) ) {
				e.setValue(val);
				Log.d(DEBUG_TAG, "setEnv: "+ e.getKey().toString() +" = "+ e.getValue().toString() );
				break;
		    }
		    
		}
	}
	
	public Process getProcess () {
		return process;
	}
	
	@Override
	public void start(String[] params) throws IOException {
		processBuilder.command(params);
		process = processBuilder.start();
		set_isRunning(true);
	}

	@Override
	public void destroy() {
		
		if(isRunning) {
			set_isRunning(false);
			process.destroy();
		}
	}

	@Override
	public int waitFor() throws InterruptedException {
		
		return process.waitFor();
	}	
}
