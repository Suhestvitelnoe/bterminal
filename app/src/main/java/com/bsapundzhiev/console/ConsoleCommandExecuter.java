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
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;
import android.util.Log;

/**
 * Console job producer
 */
class ConsoleCommandTaskExecuter extends Thread {

	private final Runnable startTask;
	private final Runnable endTask;  
	public  final Object callback;
	private final Handler handler = new Handler();

	ConsoleCommandTaskExecuter(Runnable start, Runnable end, Object callback) {

		this.startTask = start;
		this.endTask = end;  
		this.callback = callback; 
		start();
	}

	@Override
	public void run() {
		startTask.run();	
		postCallback(endTask);
	}

	/**
	 * Synch UI thread
	 */
	public void postCallback(Runnable cb) {
		handler.post(cb); 
	}
}

class ConsoleInputStreamConsumer extends Thread {
	private final int  BUFF_LEN = 1024; 
	private final InputStream inputStream;
	private final IConsoleCommandExecuterCallback consumer;
	private final ConsoleCommandTaskExecuter runningTask;

	public ConsoleInputStreamConsumer(InputStream is, ConsoleCommandTaskExecuter task) {
		this.inputStream = is;
		this.consumer = (IConsoleCommandExecuterCallback)task.callback;
		this.runningTask = task;
		start();
	}

	private void Post(final String s) {

		runningTask.postCallback(new Runnable() {

			@Override
			public void run() {	
				consumer.onProcessOutput(s);
			}
		});
	}

	@Override
	public void run() {

		final StringBuilder output = new StringBuilder();
		try {

			for(int c= 0; (c = inputStream.read()) != -1; ) {

				output.append((char)c);

				if(inputStream.available() == 0 || output.length() == BUFF_LEN) {

					Post(output.toString());
					output.delete(0, output.length() - 1);
				}
			}
		} catch (Exception e) {
			Log.d("ConsoleInputStreamConsumer:", e.getMessage());
		}
	}   
}

public class ConsoleCommandExecuter {

	private String DEBUG_TAG = "CommandExecuter";
	private ConsoleProcessBuilder consoleProcBuilder = new ConsoleProcessBuilder();
	private ConsoleCommandTaskExecuter currentTask;
	
	private ConsoleCommandFactory _commandFactory = new ConsoleCommandFactory();
	
	public ConsoleCommandExecuter() {
		//TODO: init commands
		_commandFactory.addCommand("cd", new IBtermCommand() {
			@Override
			public void exec(String params[], StringBuffer output)  {
				String dir = (params.length == 1) ? File.separator : params[1];
				try {
					consoleProcBuilder.changeDir(dir);
				} catch (Exception e) {
					output.append(e.getMessage() + "\n");
				}
			}
		});
		_commandFactory.addCommand("pwd", new IBtermCommand() {
			@Override
			public void exec(String[] params, StringBuffer output) {
				output.append(consoleProcBuilder.getCurrentWorkingDir() + "\n");
			}
		});
		_commandFactory.addCommand("echo", new IBtermCommand() {
			@Override
			public void exec(String[] params, StringBuffer output) {
				for(int i=1; i < params.length; i++) {
					output.append(String.format("%s ", params[i]));
				}
				output.append("\n");
			}
		});
		_commandFactory.addCommand("help", new IBtermCommand() {
			
			@Override
			public void exec(String[] params, StringBuffer output) {
				output.append(_commandFactory.listCommands());
			}
		});
	}
	
	/**
	 * Add custom command to executer
	 * @param name
	 * @param command
	 */
	public void addCommand(String name, IBtermCommand command) {
		_commandFactory.addCommand(name, command);
	}
	
	/**
	 * Stop running task
	 */
	public void Break() {

		if(consoleProcBuilder.isRunning()) {
			consoleProcBuilder.destroy();
			currentTask.interrupt();
		}
	}
	
	/**
	 * Create console job 
	 * @param command
	 * @param cbCommand
	 */
	public void execute(String command,final IConsoleCommandExecuterCallback callback) {
		
		final StringBuffer output = new StringBuffer();
		final String[] params = command.split(" ");
		final IBtermCommand cmd = _commandFactory.getCommand(params[0]);
		
		if(consoleProcBuilder.isRunning()) {
			writeToCurrentTaskOutput((command+"\n").getBytes());
			return;
		}

		currentTask  = new ConsoleCommandTaskExecuter(new Runnable() {

			@Override
			public void run() {
				try {
					if(cmd != null) {
						cmd.exec(params, output);
					} else {
						consoleProcBuilder.start(params);
						InputStream is = consoleProcBuilder.getProcess().getInputStream();
						ConsoleInputStreamConsumer ic = new ConsoleInputStreamConsumer(is, currentTask);	
						consoleProcBuilder.waitFor();
						ic.join();
					}
	
				} catch (IOException e) {
					output.append(String.format("Error running exec(). Command:[%s]\n", params[0]));
				} catch (InterruptedException ie) {
					Log.d(DEBUG_TAG, "Intr: "+ ie.getMessage());
				}  
				finally {
					consoleProcBuilder.destroy();
				}

			}
		}, new Runnable() {

			@Override
			public void run() {
				if(params[0].equalsIgnoreCase("exit")){
					callback.onProcessExit();
				}
				else if(params[0].equalsIgnoreCase("clear")) {
					callback.onClearScreen();
				} else {
					callback.onOutput(output.toString());
				}
				callback.onProcessEnd(consoleProcBuilder.getCurrentWorkingDir());
			}
		}, callback);
	}

	/**
	 * Write to output stream of the working Job
	 * @param bytes
	 */
	private void writeToCurrentTaskOutput(byte [] bytes) {

		try {	
			OutputStream outStream = consoleProcBuilder.getProcess().getOutputStream();
			outStream.write(bytes);
			outStream.flush();
		} catch (Exception e) {
			Log.d(DEBUG_TAG, e.getMessage());
		}
	}
}
