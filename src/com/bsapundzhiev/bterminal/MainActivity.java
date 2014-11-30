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
package com.bsapundzhiev.bterminal;/**
 */


import com.bsapundzhiev.console.ConsoleCommandExecuter;
import com.bsapundzhiev.console.IConsoleCommandExecuterCallback;
import com.bsapundzhiev.controls.ConsoleCommandListener;
import com.bsapundzhiev.controls.ConsoleView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
	private String DEBUG_TAG = "MainActivity";
	private static ConsoleCommandExecuter commandExecuter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
      final ConsoleView console = (ConsoleView) findViewById(R.id.ConsoleView);
     
      final IConsoleCommandExecuterCallback iccec = new IConsoleCommandExecuterCallback() {
    	    
    	  /**
    	   * {@inheritDoc ICommandExecuterCallback}
    	   */
    	  @Override
    		
    	  public void onOutput(String output) {
    		  console.appendLine(output);	
    	  }
    	  @Override
    	  public void onProcessOutput(String line) {
    		  console.append(line);
    	  }
    		
    	  @Override
    		
    	  public void onChangePath(String newPath) {
    			
    		  console.set_promptString(newPath);
    		
    	  }
    	  @Override
    	  public void onClearScreen() {
    		  console.getText().clear();	
    	  }
      };
      Log.d(DEBUG_TAG, "OnCreate");
      
      if(commandExecuter == null) { 
    	  commandExecuter = new ConsoleCommandExecuter(); 
    	  String hello = String.format(getString(R.string.Hello),
    			  getString(R.string.app_name), getString(R.string.version));
    	  commandExecuter.execute("echo " + hello, iccec);
      }
      
      console.addConsoleCommandListener(new ConsoleCommandListener() {
    	  
    	  @Override
    	  public void onCommand(String newCommand) {   
    		  super.onCommand(newCommand);	   
    		  commandExecuter.execute(newCommand, iccec);
    	  }
    	  
    	  @Override
    	  public void onCommandBreak(ConsoleBreak type) {
    		  super.onCommandBreak(type);	
    		  commandExecuter.Break();
    	  } 
      });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
