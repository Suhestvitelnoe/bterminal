package com.bsapundzhiev.console;

import java.util.HashMap;

/**
 * The Command interface.<br/>
 */
interface IBtermCommand {

	public void exec(String params[], StringBuffer output);
}

public class ConsoleCommandFactory {
	//private String DEBUG_TAG = "ConsoleCommandFactory";
	private final HashMap<String, IBtermCommand> commands;
		
	public ConsoleCommandFactory() {
		this.commands = new HashMap<String, IBtermCommand>();
	}
	
	public void addCommand(String name, IBtermCommand command) {
		this.commands.put(name, command);
	}
	
	public IBtermCommand getCommand(String name) {
		if ( this.commands.containsKey(name) ) {
			return this.commands.get(name);
		}
		return null;
	}
	
	public boolean executeCommand(String name, String params[], StringBuffer output) {
		if ( this.commands.containsKey(name) ) {
			this.commands.get(name).exec(params, output);
			return true;
		}
		return false;
	}
	
	public String listCommands() {
		StringBuilder buildStr = new StringBuilder();
		//Log.d(DEBUG_TAG,"Commands enabled :");
		buildStr.append("Commands enabled :");
		for (String key : commands.keySet()) {
		    //Log.d(DEBUG_TAG, key );
		    buildStr.append("\n"+ key);
		}
		
		return buildStr.toString();
	}
	
	public static ConsoleCommandFactory init() {
		ConsoleCommandFactory cf = new ConsoleCommandFactory();
		
		return cf;
	}
}
