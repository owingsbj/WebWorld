package com.gallantrealm.webworld;

import com.gallantrealm.android.FolderSelectorDialog;
import com.gallantrealm.myworld.android.GallantActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.client.model.ClientModelChangedListener;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends GallantActivity implements View.OnClickListener, ClientModelChangedListener, AdapterView.OnItemSelectedListener {

	View mainLayout;
	TextView titleText;
	CheckBox playMusicCheckBox;
	CheckBox playSoundEffectsCheckBox;
	CheckBox vibrationCheckBox;
	TextView controlTypeLabel;
	Spinner controlType;
	ArrayAdapter<CharSequence> controlTypeAdapter;
	SeekBar controlSensitivity;
	CheckBox viewIn3dCheckBox;
	CheckBox simpleRenderingCheckBox;
	CheckBox powerSaverCheckBox;
	TextView localFolderText;
	Button changeLocalFolderButton;
	CheckBox showDebugLoggingCheckBox;
	Button okButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.webworld_settings);

		mainLayout = findViewById(R.id.mainLayout);
		titleText = (TextView) findViewById(R.id.titleText);
		simpleRenderingCheckBox = (CheckBox) findViewById(R.id.simpleRenderingCheckBox);
		playMusicCheckBox = (CheckBox) findViewById(R.id.playMusicCheckBox);
		playSoundEffectsCheckBox = (CheckBox) findViewById(R.id.playSoundEffectsCheckBox);
		vibrationCheckBox = (CheckBox) findViewById(R.id.vibrationCheckBox);
		controlTypeLabel = (TextView) findViewById(R.id.controlTypeLabel);
		controlType = (Spinner) findViewById(R.id.controlType);
		controlSensitivity = (SeekBar) findViewById(R.id.controlSensitivity);
		viewIn3dCheckBox = (CheckBox) findViewById(R.id.viewIn3dCheckbox);
		simpleRenderingCheckBox = (CheckBox) findViewById(R.id.simpleRenderingCheckBox);
		powerSaverCheckBox = (CheckBox) findViewById(R.id.powerSaverCheckBox);
		localFolderText = (TextView) findViewById(R.id.localFolderText);
		changeLocalFolderButton = (Button) findViewById(R.id.changeLocalFolderButton);
		showDebugLoggingCheckBox = (CheckBox)findViewById(R.id.showDebugLoggingCheckbox);
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

		// only need to show option for simple rendering when there is GLES20
		clientModel.setWorld(null);
		if (!clientModel.supportsOpenGLES20()) {
			simpleRenderingCheckBox.setVisibility(View.GONE);
		}

		// power saver no longer featured
		powerSaverCheckBox.setVisibility(View.GONE);

		if (clientModel.useMoga(this) || clientModel.useGamepad(this) || !clientModel.canUseSensors()) {
			controlTypeLabel.setVisibility(View.GONE);
			controlType.setVisibility(View.GONE);
		} else {
			if (getString(R.string.supportsScreenController).equals("true") && getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) {
				controlTypeAdapter = new ArrayAdapter(this, R.layout.spinner_item, new String[] { "Screen Right", "Screen Left", "Tilt", "Gamepad" });
			} else {
				controlTypeAdapter = new ArrayAdapter(this, R.layout.spinner_item, new String[] { "Tilt", "Gamepad" });
			}
			controlTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			controlType.setAdapter(controlTypeAdapter);
			controlType.setOnItemSelectedListener(this);
		}

		controlSensitivity.setProgress((int) (clientModel.getControlSensitivity() * 100));
		controlSensitivity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				clientModel.setControlSensitivity(progress / 100.0f);
				clientModel.savePreferences(SettingsActivity.this);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});

		simpleRenderingCheckBox.setOnClickListener(this);
		playMusicCheckBox.setOnClickListener(this);
		playSoundEffectsCheckBox.setOnClickListener(this);
		vibrationCheckBox.setOnClickListener(this);
		viewIn3dCheckBox.setOnClickListener(this);
		simpleRenderingCheckBox.setOnClickListener(this);
		powerSaverCheckBox.setOnClickListener(this);
		changeLocalFolderButton.setOnClickListener(this);
		showDebugLoggingCheckBox.setOnClickListener(this);
		okButton.setOnClickListener(this);

		clientModel.loadPreferences(this);

		updateSettings();
		clientModel.addClientModelChangedListener(this);
	}

	public void onClick(View v) {
		if (v.equals(simpleRenderingCheckBox)) {
			clientModel.setSimpleRendering(simpleRenderingCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(playMusicCheckBox)) {
			clientModel.setPlayMusic(playMusicCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
			if (clientModel.isPlayMusic()) {
				clientModel.playSong(clientModel.getTheme().themeSongId);
			} else {
				clientModel.pauseSong();
			}
		} else if (v.equals(playSoundEffectsCheckBox)) {
			clientModel.setPlaySoundEffects(playSoundEffectsCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(vibrationCheckBox)) {
			clientModel.setVibration(vibrationCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(viewIn3dCheckBox)) {
			clientModel.setStereoscopic(viewIn3dCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(simpleRenderingCheckBox)) {
			clientModel.setSimpleRendering(simpleRenderingCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(powerSaverCheckBox)) {
			clientModel.setPowerSaver(powerSaverCheckBox.isChecked());
			clientModel.savePreferences(this);
			updateSettings();
		} else if (v.equals(changeLocalFolderButton)) {
			FolderSelectorDialog fileSelectorDialog = new FolderSelectorDialog(this, "Select local folder");
			String initialFolder = clientModel.getLocalFolder();
			if (!initialFolder.endsWith("/")) {
				initialFolder += "/";
			}
			fileSelectorDialog.show(initialFolder, new FolderSelectorDialog.SelectionListener() {
				public void onFolderSelected(final String folder) {
					SettingsActivity.this.runOnUiThread(new Runnable() {
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
		} else if (v.equals(okButton)) {
			this.finish();
		}
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
		playMusicCheckBox.setChecked(clientModel.isPlayMusic());
		playSoundEffectsCheckBox.setChecked(clientModel.isPlaySoundEffects());
		vibrationCheckBox.setChecked(clientModel.isVibration());
// SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
// if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
		if (controlTypeAdapter != null) {
			if (clientModel.useSensors()) {
				controlType.setSelection(controlTypeAdapter.getPosition("Tilt"));
			} else if (clientModel.useScreenControl()) {
				if (clientModel.isControlOnLeft()) {
					controlType.setSelection(controlTypeAdapter.getPosition("Screen Left"));
				} else {
					controlType.setSelection(controlTypeAdapter.getPosition("Screen Right"));
				}
			} else if (clientModel.useZeemote()) {
				controlType.setSelection(controlTypeAdapter.getPosition("Zeemote"));
			} else {
				controlType.setSelection(controlTypeAdapter.getPosition("Gamepad"));
			}
		}
// }
		viewIn3dCheckBox.setChecked(clientModel.isStereoscopic());
		simpleRenderingCheckBox.setChecked(clientModel.isSimpleRendering());
		powerSaverCheckBox.setChecked(clientModel.isPowerSaver());
		localFolderText.setText(clientModel.getLocalFolder());
		showDebugLoggingCheckBox.setChecked(clientModel.isShowDebugLogging());
	}

	public void onItemSelected(AdapterView av, View view, int arg1, long arg2) {
		String type = (String) controlType.getSelectedItem();
		// "Tilt", "Screen Left", "Screen Right", "Zeemote", "Gamepad"
		if (type == null) {
			return;
		} else if (type.equals("Tilt")) { // tilt
			clientModel.setUseScreenControl(false);
			clientModel.setControlOnLeft(false);
			clientModel.setUseSensors(true);
			clientModel.setUseZeemote(false);
			clientModel.savePreferences(this);
		} else if (type.equals("Screen Left")) { // left screen
			clientModel.setUseScreenControl(true);
			clientModel.setControlOnLeft(true);
			clientModel.setUseSensors(false);
			clientModel.setUseZeemote(false);
			clientModel.savePreferences(this);
		} else if (type.equals("Screen Right")) { // right screen
			clientModel.setUseScreenControl(true);
			clientModel.setControlOnLeft(false);
			clientModel.setUseSensors(false);
			clientModel.setUseZeemote(false);
			clientModel.savePreferences(this);
		} else if (type.equals("Zeemote")) { // zeemote
			clientModel.setUseScreenControl(false);
			clientModel.setControlOnLeft(false);
			clientModel.setUseSensors(false);
			clientModel.setUseZeemote(true);
			clientModel.savePreferences(this);
		} else if (type.equals("Gamepad")) { // gamepad
			clientModel.setUseScreenControl(false);
			clientModel.setControlOnLeft(false);
			clientModel.setUseSensors(false);
			clientModel.setUseZeemote(false);
			clientModel.savePreferences(this);
		}
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
