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
     * Set prompt
     * @param {@link String} newPath
     */
    void onChangePath(String newPath);
    /**
     * Clear screen 
     */
    void onClearScreen();
}
