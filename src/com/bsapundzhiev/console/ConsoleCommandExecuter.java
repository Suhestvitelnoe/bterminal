/**
 * BTerminal - Android terminal emulator
 *
 *  Copyright 2014 by Borislav Sapundzhiev <bsapundjiev@gmail.com>
 * 
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 * 
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 */
package com.bsapundzhiev.console;
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
	/**
	 * UI callback
	 */
	public Object callback;
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
		handler.post(endTask);
	}
	
	/**
	 * Synch UI thread
	 */
	public void postCallback(Runnable task) {
		
		handler.post(task); 
	}
}

class ConsoleInputStreamConsumer extends Thread {
	private static int  BUFF_LEN = 1024 / 2; 
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

        } catch (IOException e) {
            Log.d("ConsoleInputStreamConsumer", e.getMessage());
        }
    }
}

public class ConsoleCommandExecuter {
	
	private String DEBUG_TAG = "CommandExecuter";
	private ConsoleProcessBuilder consoleProcBuilder = new ConsoleProcessBuilder();
	private ConsoleCommandTaskExecuter currentTask;
	private String[] commands = {"cd", "pwd", "echo", "clear"};
	  
	public ConsoleCommandExecuter() {
		
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
	 * Internal basic shell commands
	 * @param params
	 * @param cbCommand
	 */
	private void executeInternal(final String[] params, final IConsoleCommandExecuterCallback cbCommand) {
		
		final StringBuffer output = new StringBuffer();
		
		currentTask  = new ConsoleCommandTaskExecuter(new Runnable() {
			
			@Override
			public void run() {
				if(params[0].equalsIgnoreCase("cd")) {
					try {
						consoleProcBuilder.changeDir(params[1]);
					} catch (Exception e) {
						output.append(e.getMessage() + "\n");
					}
				}
				
				if(params[0].equalsIgnoreCase("pwd")) {
					output.append(consoleProcBuilder.getCurrentWorkingDir() + "\n");
				}
				
				if(params[0].equalsIgnoreCase("clear")) {
					cbCommand.onClearScreen();
				}
				
				if(params[0].equalsIgnoreCase("echo")) {
					
					for(int i=1; i < params.length; i++) {
						String out = (i == 1) ? String.format("%s",params[i]) 
								: String.format(" %s", params[i]);
						output.append(out);
					}
					output.append("\n");
				}
			}
			
		}, new Runnable() {
			
			@Override
			public void run() {
				cbCommand.onOutput(output.toString());
				cbCommand.onChangePath(consoleProcBuilder.getCurrentWorkingDir());
			}
		}, null);
	}

	/**
	 * Create console job 
	 * @param command
	 * @param cbCommand
	 */
	public void execute(String command, final IConsoleCommandExecuterCallback cbCommand) {
		
		final StringBuffer output = new StringBuffer();
		final String[] params = command.split(" ");
		
		for(int i=0; i < commands.length; i++) {
			
			if(params[0].equals(commands[i])) {
				executeInternal(params, cbCommand);
				return;
			}
		}	
		
		if(consoleProcBuilder.isRunning()) {
			command +="\n";
			writeToCurrentTaskOutput(command.getBytes());
			return;
		}
		
		currentTask  = new ConsoleCommandTaskExecuter(new Runnable() {
			
			@Override
			public void run() {
				try {
					consoleProcBuilder.command( params );
					consoleProcBuilder.start();
					InputStream is = consoleProcBuilder.getProcess().getInputStream();
					ConsoleInputStreamConsumer ic = new ConsoleInputStreamConsumer(is, currentTask);
		    		consoleProcBuilder.waitFor();
		    		ic.join();
	
		        } catch (Exception e) {
		        	
		        	if(e.getMessage() != null) {
		        		output.append(String.format("%s: command not found\n", params[0]));
		        	} else {
		        		e.printStackTrace();	
		        	}
		        	
		        } finally {
		        	consoleProcBuilder.destroy();
		        }
				
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				
				cbCommand.onOutput(output.toString());
				cbCommand.onChangePath(consoleProcBuilder.getCurrentWorkingDir());
			}
		}, cbCommand);
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
