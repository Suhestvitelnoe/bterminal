/**
 * BTerminal - Android terminal emulator
 *
 *  Copyright 2014 by Borislav Sapundzhiev <bsapundjiev@gmail.com>
 * 
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 */
package com.bsapundzhiev.controls;

import java.util.EventListener;
import java.util.EventObject;
import android.util.Log;

class ConsoleCommandEvent extends EventObject {
	/**
	 * TODO: generate UID
	 */
	private static final long serialVersionUID = 1L;

	public ConsoleCommandEvent(Object source) {
		super(source);
	}
}

public class ConsoleCommandListener implements EventListener {

	public enum ConsoleBreak {
		CTRLC, CTRLX,
	};

	/**
	 * Public ctor
	 */
	public ConsoleCommandListener() {
		super();
	}

	/**
	 * New console command
	 * 
	 * @param {@link String} command
	 * @return none
	 */
	public void onCommand(String newCommand) {
		Log.d("ConsoleCommandListener.onCommand", newCommand);
	}

	/**
	 * Break command
	 * 
	 * @param none
	 * @return none
	 */
	public void onCommandBreak(ConsoleBreak type) {
		Log.d("ConsoleCommandListener.onCommandBreak", type.toString());
	}
}
