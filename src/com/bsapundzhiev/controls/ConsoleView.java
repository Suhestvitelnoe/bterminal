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

import android.content.Context;
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

	public static String propmptSign = "$";
	private String DEBUG_TAG = "ConsoleView";
	private StringBuilder _commandBuilder = new StringBuilder();
	private String _promptString;

	/**
	 * command interface
	 */
	protected List<ConsoleCommandListener> commandListeners = new ArrayList<ConsoleCommandListener>();

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

		// TODO Auto-generated method stub
		if (_commandBuilder.length() > 0) {
			_commandBuilder.delete(0, _commandBuilder.length() - 1);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		return new ConsoleInputConnection(
				super.onCreateInputConnection(outAttrs), true);
	}

	private class ConsoleInputConnection extends InputConnectionWrapper {

		private Boolean backTag;

		public ConsoleInputConnection(InputConnection target, boolean mutable) {
			super(target, mutable);
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {

			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_DEL) {

				// cancel the backspace if buffer is empty
				if (backTag == true) {
					return false;
				}
			}

			return super.sendKeyEvent(event);
		}

		@Override
		public boolean deleteSurroundingText(int beforeLength, int afterLength) {

			backTag = (_commandBuilder.length() == 0) ? true : false;

			if (_commandBuilder.length() > 0) {
				_commandBuilder.deleteCharAt(_commandBuilder.length() - 1);
			}

			// deleteSurroundingText(1, 0) will be called for backspace
			if (beforeLength == 1 && afterLength == 0) {
				// backspace
				return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DEL))
						&& sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
								KeyEvent.KEYCODE_DEL));
			}

			return super.deleteSurroundingText(beforeLength, afterLength);
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {

			// Log.d(DEBUG_TAG, "commitText:[" + text.toString() +"]");

			if (text.toString().equalsIgnoreCase("\n")) {

				if (_commandBuilder.length() > 0) {
					onCommand(_commandBuilder.toString());
					_commandBuilder.delete(0, _commandBuilder.length());
				} else {
					// skip empty lines
					return false;
				}
			} else {

				_commandBuilder.append(text);
			}

			return super.commitText(text, newCursorPosition);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		setSelection(getText().length());
		super.onDraw(canvas);
	}

	private void ConsoleViewInit() {
		Log.d(DEBUG_TAG, "ConsoleViewInit");
		setGravity(Gravity.TOP);
		setBackgroundColor(Color.BLACK);
		setTextColor(Color.GREEN);
		setTextSize(11);
		setTypeface(Typeface.MONOSPACE);

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
				// TODO: pop up command menu
				Toast.makeText(getContext(), "Send Ctrl+C", Toast.LENGTH_SHORT)
						.show();
				onCommandBreak(ConsoleBreak.CTRLC);
				return true;
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

	public String get_promptString() {
		return _promptString;
	}

	/**
	 * {@link ConsoleCommandListener}
	 * 
	 * @param evt
	 */
	public void addConsoleCommandListener(ConsoleCommandListener listener) {

		commandListeners.add(listener);
	}

	public void removeConsoleCommandListener(ConsoleCommandListener listener) {

		commandListeners.remove(listener);
	}

	void onCommand(String command) {

		for (ConsoleCommandListener commandlistener : commandListeners) {
			commandlistener.onCommand(command);
		}
	}

	void onCommandBreak(ConsoleBreak type) {

		for (ConsoleCommandListener commandlistener : commandListeners) {
			commandlistener.onCommandBreak(type);
		}
	}
}
