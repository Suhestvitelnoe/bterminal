/**
 * BTerminal - Android terminal emulator
 *
 *  Copyright 2014 by Borislav Sapundzhiev <bsapundjiev@gmail.com>
 * 
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 */
package com.bsapundzhiev.console;

public interface IConsoleCommandExecuterCallback {
	/**
	 * New line on screen
	 * @param {@link String} output
	 */
    void onOutput(String output);
    /**
     * Send output to console 
     * @param {@link String} line
     */
    void onProcessOutput(String line);
    /**
     * Command finish
     * @param {@link String} newPath
     */
    void onProcessExit(String workingDirectory);
    /**
     * Clear screen 
     */
    void onClearScreen();
}
