package com.gallantrealm.myworld.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gallantrealm.myworld.client.model.ClientModel;

public class SettingsDialog extends Dialog {
	ClientModel clientModel = AndroidClientModel.getClientModel();

	TextView titleText;
	TextView messageText;
	EditText inputText;
	Button option1Button;
	Button option2Button;
	Button option3Button;
	int buttonPressed = -1;
	String title;
	String message;
	String initialValue;
	String[] options;
	Activity activity;

	public SettingsDialog(Context context, String title, String message, String initialValue, String[] options) {
		super(context, R.style.Theme_Dialog);
		activity = (Activity) context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.title = title;
		this.message = message;
		this.initialValue = initialValue;
		this.options = options;
		setContentView(R.layout.input_dialog);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		titleText = (TextView) findViewById(R.id.titleText);
		messageText = (TextView) findViewById(R.id.messageText);
		inputText = (EditText) findViewById(R.id.inputText);
		option1Button = (Button) findViewById(R.id.option1Button);
		option2Button = (Button) findViewById(R.id.option2Button);
		option3Button = (Button) findViewById(R.id.option3Button);

		Typeface typeface = clientModel.getTypeface(getContext());
		if (typeface != null) {
			titleText.setTypeface(typeface);
			option1Button.setTypeface(typeface);
			option2Button.setTypeface(typeface);
			option3Button.setTypeface(typeface);
		}

		int styleId = clientModel.getTheme().buttonStyleId;
		if (styleId != 0) {
			option1Button.setBackgroundResource(styleId);
			option2Button.setBackgroundResource(styleId);
			option3Button.setBackgroundResource(styleId);
		}

		if (title != null) {
			titleText.setText(title);
			titleText.setVisibility(View.VISIBLE);
		} else {
			titleText.setVisibility(View.GONE);
		}

		messageText.setText(message);
		inputText.setText(initialValue);

		option1Button.setVisibility(View.GONE);
		option2Button.setVisibility(View.GONE);
		option3Button.setVisibility(View.GONE);
		if (options == null) {
			option1Button.setText("OK");
			option1Button.setVisibility(View.VISIBLE);
		} else {
			if (options.length > 0) {
				option1Button.setText(options[0]);
				option1Button.setVisibility(View.VISIBLE);
				if (options.length > 1) {
					option2Button.setText(options[1]);
					option2Button.setVisibility(View.VISIBLE);
					if (options.length > 2) {
						option3Button.setText(options[2]);
						option3Button.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		option1Button.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonPressed = 0;
				SettingsDialog.this.dismiss();
				SettingsDialog.this.cancel();
				return true;
			}

		});
		option2Button.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonPressed = 1;
				SettingsDialog.this.dismiss();
				SettingsDialog.this.cancel();
				return true;
			}

		});
		option3Button.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonPressed = 2;
				SettingsDialog.this.dismiss();
				SettingsDialog.this.cancel();
				return true;
			}

		});
	}

	@Override
	public void show() {
		super.show();
	}

	public int getButtonPressed() {
		return buttonPressed;
	}

	public String getValue() {
		return inputText.getText().toString();
	}

}
