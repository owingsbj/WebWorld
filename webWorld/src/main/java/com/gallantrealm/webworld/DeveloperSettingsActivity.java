package com.gallantrealm.webworld;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.gallantrealm.android.FolderSelectorDialog;
import com.gallantrealm.myworld.android.GallantActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.client.model.ClientModelChangedListener;

public class DeveloperSettingsActivity extends GallantActivity implements View.OnClickListener, ClientModelChangedListener {

	View mainLayout;
	TextView titleText;
	TextView localFolderText;
	Button changeLocalFolderButton;
	EditText webserverEdit;
	CheckBox showDebugLoggingCheckBox;
	CheckBox displayFrameRateCheckBox;
	Button okButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.webworld_developer_settings);

		mainLayout = findViewById(R.id.mainLayout);
		titleText = (TextView) findViewById(R.id.titleText);
		localFolderText = (TextView) findViewById(R.id.localFolderText);
		changeLocalFolderButton = (Button) findViewById(R.id.changeLocalFolderButton);
		webserverEdit = (EditText) findViewById(R.id.webserverEdit);
		showDebugLoggingCheckBox = (CheckBox)findViewById(R.id.showDebugLoggingCheckbox);
		displayFrameRateCheckBox = (CheckBox)findViewById(R.id.displayFrameRateCheckbox);
		okButton = (Button) findViewById(R.id.okButton);

		// shadows aren't working yet.
		// showShadowsCheckBox.setVisibility(View.GONE);

		mainLayout.setBackgroundResource(clientModel.getTheme().themeBackgroundId);
		songId = clientModel.getTheme().themeSongId;

		Typeface typeface = clientModel.getTypeface(this);
		if (typeface != null) {
			titleText.setTypeface(typeface);
			okButton.setTypeface(typeface);
		}

		int styleId = clientModel.getTheme().buttonStyleId;
		if (styleId != 0) {
			okButton.setBackgroundResource(styleId);
		}

		changeLocalFolderButton.setOnClickListener(this);
		showDebugLoggingCheckBox.setOnClickListener(this);
		displayFrameRateCheckBox.setOnClickListener(this);
		okButton.setOnClickListener(this);

		clientModel.loadPreferences(this);

		updateSettings();
		clientModel.addClientModelChangedListener(this);
	}

	public void onClick(View v) {
		if (v.equals(changeLocalFolderButton)) {
			FolderSelectorDialog fileSelectorDialog = new FolderSelectorDialog(this, "Select local folder");
			String initialFolder = clientModel.getLocalFolder();
			if (!initialFolder.endsWith("/")) {
				initialFolder += "/";
			}
			fileSelectorDialog.show(initialFolder, new FolderSelectorDialog.SelectionListener() {
				public void onFolderSelected(final String folder) {
					DeveloperSettingsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							localFolderText.setText(folder);
							clientModel.setLocalFolder(folder);
						}
					});
				}
			});
		} else if (v.equals(showDebugLoggingCheckBox)) {
			clientModel.setShowDebugLogging(showDebugLoggingCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(displayFrameRateCheckBox)) {
			clientModel.setDisplayActualFrameRate(displayFrameRateCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(okButton)) {
			this.finish();
		}
	}

	@Override
	public void finish() {
		clientModel.setSharedServer(webserverEdit.getText().toString());
		clientModel.savePreferences(this);
		super.finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void clientModelChanged(ClientModelChangedEvent event) {
	}

	public void updateSettings() {
		localFolderText.setText(clientModel.getLocalFolder());
		showDebugLoggingCheckBox.setChecked(clientModel.isShowDebugLogging());
		displayFrameRateCheckBox.setChecked(clientModel.isDisplayActualFrameRate());
		webserverEdit.setText(clientModel.getSharedServer());
	}

}
