/**
 * Simple terminal emulation view
 * @version 0.1
 *
 *  Copyright 2014 by Borislav Sapundzhiev <bsapundjiev@gmail.com>
 * 
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 */
package com.bsapundzhiev.controls;

import java.util.ArrayList;
import java.util.List;

import com.bsapundzhiev.controls.ConsoleCommandListener.ConsoleBreak;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import android.widget.Toast;

public class ConsoleView extends EditText {

	public static String propmptSign = "$ ";
	private String DEBUG_TAG = "ConsoleView";
	private String _promptString;
	private static ArrayList<String> _history = new ArrayList<>();
    private static StringBuilder _commandBuilder = new StringBuilder();
	/**
	 * command interface
	 */
	protected List<ConsoleCommandListener> commandListeners = new ArrayList<>();

	public ConsoleView(Context context) {

		super(context);
		ConsoleViewInit();
	}

	public ConsoleView(Context context, AttributeSet attrs) {

		super(context, attrs);
		ConsoleViewInit();
	}

	public ConsoleView(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);
		ConsoleViewInit();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// TODO fixme
		if (_commandBuilder.length() > 0) {
			onCommand("clear");
			_commandBuilder.delete(0, _commandBuilder.length());
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		return new ConsoleInputConnection(
				super.onCreateInputConnection(outAttrs), true);
	}

	private class ConsoleInputConnection extends InputConnectionWrapper {

		public ConsoleInputConnection(InputConnection target, boolean mutable) {
			super(target, mutable);
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {

			if (event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
				// cancel the backspace if buffer is empty
				if (_commandBuilder.length() == 0) {
					return false;
				}
				_commandBuilder.deleteCharAt(_commandBuilder.length() - 1);
			}

			if(event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				Log.d(DEBUG_TAG, "Enter");
				if(!performCommand()) {
					return false;
				}
			}
			return super.sendKeyEvent(event);
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {

			//Log.d(DEBUG_TAG, "commitText:[" + text.toString() +"]");
			_commandBuilder.append(text);
			return super.commitText(text, newCursorPosition);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		setSelection(getText().length());
		super.onDraw(canvas);
	}

    private boolean performCommand() {

		if (_commandBuilder.length() > 0) {
			onCommand(_commandBuilder.toString());
			_commandBuilder.delete(0, _commandBuilder.length());
			return true;
		} else {
			// skip empty lines
			return false;
		}
	}

	private void ConsoleViewInit() {
		Log.d(DEBUG_TAG, "ConsoleViewInit");
		setGravity(Gravity.TOP);
		setBackgroundColor(Color.BLACK);
		setTextColor(Color.GREEN);
		setTextSize(11);
		setTypeface(Typeface.MONOSPACE);
        //setCursorVisible(false);

		setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
				| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

		// FIXME: this fix style changes when soft keyboard appears
		setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		setMovementMethod(new ScrollingMovementMethod());

		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				ArrayList<String> tmp = new ArrayList<String>(_history);
				tmp.add(0, "ctrl+c");
				tmp.add(1, "clear");
				final String[] items =tmp.toArray(new String[0]);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());    
				builder.setItems(items, new DialogInterface.OnClickListener() {
				            
					public void onClick(DialogInterface dialog, int which) {    
						
						switch (which) {
						case 0:
							onCommandBreak(ConsoleBreak.CTRLC);
							break;
						case 1:
							onCommand(items[which]);
						default:
							append(items[which]);
							_commandBuilder.append(items[which]); 
							break;
						}    
						
						Toast.makeText(getContext(), 
								"Send "+ items[which], Toast.LENGTH_SHORT).show();
					}       
				});
				
				return builder.show().isShowing();
			}
		});
	}

	public void appendLine(String text) {

		if (text.length() == 0) 
			return;

		if (!text.endsWith("\n")) {	
			text = "\n" + text;
		}

		append(text);
	}

	public void set_promptString(String prompt) {

		_promptString = prompt;
		append(String.format("%s%s", _promptString, ConsoleView.propmptSign));
	}

	public void addToHistory(String command) {
		_history.add(command);
	}
	
	/**
	 * {@link ConsoleCommandListener}
	 * 
	 * @param ConsoleCommandListener
	 */
	public void addConsoleCommandListener(ConsoleCommandListener listener) {

		commandListeners.add(listener);
	}

	public void removeConsoleCommandListener(ConsoleCommandListener listener) {

		commandListeners.remove(listener);
	}

	void onCommand(String command) {
		
		for (ConsoleCommandListener commandListener : commandListeners) {
			commandListener.onCommand(command);
		}
	}

	void onCommandBreak(ConsoleBreak type) {

		for (ConsoleCommandListener commandListener : commandListeners) {
			commandListener.onCommandBreak(type);
		}
	}
}
