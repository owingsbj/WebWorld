package com.gallantrealm.webworld;

import com.gallantrealm.myworld.android.GallantActivity;
import com.gallantrealm.myworld.android.ShowWorldActivity;
import com.gallantrealm.myworld.android.StartWorldActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuActivity extends GallantActivity {

	public View mainLayout;
	public TextView titleText;
	public Button chooseAvatarButton;
	public Button chooseWorldButton;
	public Button playButton;
	public Button settingsButton;
	public Button quitButton;
	public Button helpButton;
	public Button buyButton;
	public View buyFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webworld_main_menu);

		clientModel.setContext(this);
		clientModel.loadPreferences(this);

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mainLayout = findViewById(R.id.mainLayout);
		titleText = (TextView) findViewById(R.id.titleText);
		// plusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
		chooseAvatarButton = (Button) findViewById(R.id.chooseAvatarButton);
		chooseWorldButton = (Button) findViewById(R.id.chooseWorldButton);
		playButton = (Button) findViewById(R.id.playButton);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		quitButton = (Button) findViewById(R.id.quitButton);
		helpButton = (Button) findViewById(R.id.helpButton);
		buyButton = (Button)findViewById(R.id.buyButton);
		buyFrame = findViewById(R.id.buyFrame);

		Typeface typeface = clientModel.getTypeface(this);
		if (typeface != null) {
			titleText.setTypeface(typeface);
			chooseAvatarButton.setTypeface(typeface);
			chooseWorldButton.setTypeface(typeface);
			playButton.setTypeface(typeface);
			settingsButton.setTypeface(typeface);
			quitButton.setTypeface(typeface);
			helpButton.setTypeface(typeface);
			if (buyButton != null) {
				buyButton.setTypeface(typeface);
			}
		}

		mainLayout.setBackgroundResource(clientModel.getTheme().themeBackgroundId);
		songId = clientModel.getTheme().themeSongId;

		int styleId = clientModel.getTheme().buttonStyleId;
		if (styleId != 0) {
			chooseAvatarButton.setBackgroundResource(styleId);
			chooseWorldButton.setBackgroundResource(styleId);
			playButton.setBackgroundResource(styleId);
			settingsButton.setBackgroundResource(styleId);
			quitButton.setBackgroundResource(styleId);
			helpButton.setBackgroundResource(styleId);
			if (buyButton != null) {
				buyButton.setBackgroundResource(styleId);
			}
		}

		chooseAvatarButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Class activityClass = MainMenuActivity.this.getClassLoader().loadClass(getString(R.string.selectAvatarClassName));
					Intent intent = new Intent(MainMenuActivity.this, activityClass);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		chooseWorldButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Class activityClass = MainMenuActivity.this.getClassLoader().loadClass(getString(R.string.selectWorldClassName));
					Intent intent = new Intent(MainMenuActivity.this, activityClass);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				clientModel.setCustomizeMode(false);
				try {
					Class activityClass = MainMenuActivity.this.getClassLoader().loadClass("com.gallantrealm.myworld.ShowWorldActivity");
					Intent intent = new Intent(MainMenuActivity.this, activityClass);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		settingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Class activityClass = MainMenuActivity.this.getClassLoader().loadClass("com.gallantrealm.webworld.SettingsActivity");
					Intent intent = new Intent(MainMenuActivity.this, activityClass);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		helpButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Class activityClass = MainMenuActivity.this.getClassLoader().loadClass("com.gallantrealm.myworld.android.HelpActivity");
					Intent intent = new Intent(MainMenuActivity.this, activityClass);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		quitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MainMenuActivity.this.finish();
			}
		});
		if (buyButton != null) {
			if (clientModel.isFullVersion()) {
				buyFrame.setVisibility(View.GONE);
			} else {
				buyButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						try {
							clientModel.buyFullVersion();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
		if (buyFrame != null) {
			if (!clientModel.isGoggleDogPass()) {
				buyFrame.setVisibility(View.GONE); // not working well yet
			}
		}

		clientModel.setAllWorldsUnlocked();  // override default behavior.  webworld users don't like unlocking games one by one.
		chooseAvatarButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, SelectAvatarActivity.class);
				startActivity(intent);
			}
		});
		chooseWorldButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, SelectWorldActivity.class);
				startActivity(intent);
			}
		});
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, StartWorldActivity.class);
				startActivity(intent);
			}
		});
		settingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static boolean showPopupAd;

	@Override
	protected void onStart() {
		super.onStart();
		if (showPopupAd) {
			clientModel.showPopupAd();
			showPopupAd = false;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// plusOneButton.initialize("https://play.google.com/store/apps/details?id=" + getPackageName(), 1);
	}

}
