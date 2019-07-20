package com.gallantrealm.webworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import com.gallantrealm.myworld.android.GallantActivity;
import com.gallantrealm.myworld.android.StartWorldActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.client.model.ClientModelChangedListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectAvatarActivity extends GallantActivity implements View.OnClickListener, ClientModelChangedListener, OnGestureListener {

	int navatars;
	int currentAvatarNum;

	View mainLayout;
	TextView titleText;
	TextView countText;
	Button previousButton;
	Button nextButton;
	View selectedView;
	TextView avatarNameText;
	ImageView avatarImage;
	TextView avatarDescriptionText;
	Button okButton;
	Button customizeButton;

	GestureDetector gestureDetector;

	ArrayList<String> avatarFolders;
	Properties avatarProps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webworld_select_avatar);

		mainLayout = findViewById(R.id.mainLayout);
		titleText = (TextView) findViewById(R.id.titleText);
		countText = (TextView) findViewById(R.id.countText);
		selectedView = findViewById(R.id.selectedView);
		avatarNameText = (TextView) findViewById(R.id.avatarNameText);
		avatarImage = (ImageView) findViewById(R.id.avatarImage);
		avatarDescriptionText = (TextView) findViewById(R.id.avatarDescriptionText);
		previousButton = (Button) findViewById(R.id.previousButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		okButton = (Button) findViewById(R.id.okButton);
		customizeButton = (Button) findViewById(R.id.customizeButton);

		mainLayout.setBackgroundResource(clientModel.getTheme().themeBackgroundId);
		songId = clientModel.getTheme().themeSongId;

		Typeface typeface = clientModel.getTypeface(this);
		if (typeface != null) {
			titleText.setTypeface(typeface);
			countText.setTypeface(typeface);
			avatarNameText.setTypeface(typeface);
			okButton.setTypeface(typeface);
			customizeButton.setTypeface(typeface);
		}

		int styleId = clientModel.getTheme().buttonStyleId;
		if (styleId != 0) {
			okButton.setBackgroundResource(styleId);
			customizeButton.setBackgroundResource(styleId);
		}

		previousButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		okButton.setOnClickListener(this);
		customizeButton.setOnClickListener(this);

		avatarFolders = new ArrayList<String>();
		AsyncTask.execute(new Runnable() {
			public void run() {
				HttpURLConnection connection = null;
				InputStream inputStream = null;
				try {

					// First look in the local file system
					File avatarsDir = new File(clientModel.getLocalFolder() + "/avatars");
					System.out.println(">> " + avatarsDir);
					if (avatarsDir.exists() && avatarsDir.isDirectory()) {
						String[] fileNames = avatarsDir.list();
						if (fileNames != null) {
							for (String fileName : fileNames) {
								if (new File(avatarsDir, fileName).isDirectory()) {
									avatarFolders.add(fileName);
								}
							}
						}
					}

					// Next look in gallanrealm.com
					System.out.println(">> http://gallantrealm.com/webworld/listAvatars.jsp");
					connection = (HttpURLConnection) (new URL("http://gallantrealm.com/webworld/listAvatars.jsp")).openConnection();
					connection.setConnectTimeout(5000);
					inputStream = connection.getInputStream();
					Reader reader = new InputStreamReader(inputStream, "UTF-8");
					StreamTokenizer tokenizer = new StreamTokenizer(reader);
					while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
						if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
							avatarFolders.add(tokenizer.sval);
						}
					}
				} catch (final IOException e) {
					System.err.println(e.getMessage());
					SelectAvatarActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							avatarDescriptionText.setText("Couldn't connect to gallantrealm.com:\n" + e.getMessage());
						}
					});
					return;
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				}
				System.out.println(avatarFolders);
				navatars = avatarFolders.size();
				currentAvatarNum = 1;
				for (int i = 0; i < avatarFolders.size(); i++) {
					if (avatarFolders.get(i).equals(clientModel.getAvatarName())) {
						currentAvatarNum = i + 1;
					}
				}
				String avatarName = avatarFolders.get(currentAvatarNum - 1);
				clientModel.setAvatarName(avatarName);
				getAvatarProperties(avatarName);
			}
		});
		clientModel.addClientModelChangedListener(this);

		gestureDetector = new GestureDetector(this, this);
	}

	public void onClick(View v) {
		if (v.equals(nextButton)) {
			onNext();
		} else if (v.equals(previousButton)) {
			onPrevious();
		} else if (v.equals(okButton)) {
			this.finish();
		} else if (v.equals(customizeButton)) {
			System.out.println("launching avatar customization");
			clientModel.setCustomizeMode(true);
			Intent intent = new Intent(SelectAvatarActivity.this, StartWorldActivity.class);
			startActivity(intent);
		}
		clientModel.savePreferences(this);
		updateUI();
	}

	@Override
	public void onNext() {
		if (currentAvatarNum < navatars) {
			Animation animation = new TranslateAnimation(selectedView.getWidth() / 4, 0, 0, 0);
			animation.setDuration(250);
			selectedView.startAnimation(animation);
			currentAvatarNum += 1;
			if (clientModel.isWorldUnlocked(currentAvatarNum)) {
				String avatarName = avatarFolders.get(currentAvatarNum - 1);
				clientModel.setAvatarName(avatarName);
				getAvatarProperties(avatarName);
			}
		}
	}

	@Override
	public void onPrevious() {
		if (currentAvatarNum > 1) {
			Animation animation = new TranslateAnimation(-selectedView.getWidth() / 4, 0, 0, 0);
			animation.setDuration(250);
			selectedView.startAnimation(animation);
			currentAvatarNum -= 1;
			if (clientModel.isWorldUnlocked(currentAvatarNum)) {
				String avatarName = avatarFolders.get(currentAvatarNum - 1);
				clientModel.setAvatarName(avatarName);
				getAvatarProperties(avatarName);
			}
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
		if (event.getEventType() == ClientModelChangedEvent.EVENT_TYPE_SELECTED_GAME_CHANGED || event.getEventType() == ClientModelChangedEvent.EVENT_TYPE_FULLVERSION_CHANGED) {
			updateUI();
		}
	}

	public void getAvatarProperties(final String avatarName) {
		avatarProps = new Properties();
		AsyncTask.execute(new Runnable() {
			public void run() {
				HttpURLConnection connection = null;
				InputStream inputStream = null;
				try {
					// First try file
					File file = new File(clientModel.getLocalFolder() + "/avatars/" + avatarName + "/avatar.properties");
					System.out.println(">> " + file);
					if (file.exists()) {
						inputStream = new FileInputStream(file);
					} else {
						// Then try gallantrealm.com
						URL url = new URL("http://gallantrealm.com/webworld/avatars/" + avatarName + "/avatar.properties");
						System.out.println(">> " + url);
						connection = (HttpURLConnection) (url.openConnection());
						inputStream = connection.getInputStream();
					}
					avatarProps.load(inputStream);
					avatarProps.list(System.out);
					System.out.println();
				} catch (IOException e) {
					System.err.println(e);
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				}
				runOnUiThread(new Runnable() {
					public void run() {
						updateUI();
					}
				});
			}
		});
	}

	Bitmap oldBitmap;

	public void updateUI() {
		if (avatarFolders == null || avatarProps == null) {
			return;
		}
		int n = currentAvatarNum;
		final String avatarName = avatarFolders.get(currentAvatarNum - 1);
		avatarNameText.setText(avatarProps.getProperty("name", avatarName) + " by "+ avatarProps.getProperty("author", "unknown"));
		avatarDescriptionText.setText(avatarProps.getProperty("description", ""));
		avatarImage.setImageBitmap(null);
		AsyncTask.execute(new Runnable() {
			public void run() {
				HttpURLConnection connection = null;
				InputStream inputStream = null;
				try {
					// First try file
					File file = new File(clientModel.getLocalFolder() + "/avatars/" + avatarName + "/" + avatarProps.getProperty("picture"));
					System.out.println(">> " + file);
					if (file.exists()) {
						inputStream = new FileInputStream(file);
					} else {
						// Then try gallantrealm.com
						URL url = new URL("http://gallantrealm.com/webworld/avatars/" + avatarName + "/" + avatarProps.getProperty("picture"));
						System.out.println(">> " + url);
						connection = (HttpURLConnection) (url.openConnection());
						inputStream = connection.getInputStream();
					}
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					runOnUiThread(new Runnable() {
						public void run() {
							avatarImage.setImageBitmap(bitmap);
							if (oldBitmap != null) {
								oldBitmap.recycle();
								oldBitmap = null;
							}
							oldBitmap = bitmap;
						}
					});
				} catch (IOException e) {
					System.err.println(e);
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				}
			}
		});
		if (n <= 1) {
			previousButton.setVisibility(View.INVISIBLE);
		} else {
			previousButton.setVisibility(View.VISIBLE);
		}
		if (n >= navatars) {
			nextButton.setVisibility(View.INVISIBLE);
		} else {
			nextButton.setVisibility(View.VISIBLE);
		}
		countText.setText("" + n + " of " + navatars);
		if (clientModel.isCustomizable(n)) {
			customizeButton.setVisibility(View.VISIBLE);
		} else {
			customizeButton.setVisibility(View.GONE);
		}
	}

	public String getScoreString(int i) {
		if (clientModel.isWorldUnlocked(i)) {
			String scoreString = "";
			int level = clientModel.getLevel(i);
			if (level > 0) {
				scoreString += "Level: " + level + "  ";
			}
			int time = clientModel.getTime(i);
			if (time > 0) {
				scoreString += "Time: " + formatTime(time) + "  ";
			}
			int score = clientModel.getScore(i);
			if (score > 0) {
				scoreString += getString(R.string.scoreLabel) + " " + score;
			}
			return scoreString;
		} else {
			return "Locked";
		}
	}

	public static String formatTime(long millis) {
		Date time = new Date(millis);
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.S");
		String timeString = sdf.format(time);
		if (timeString.endsWith(".")) {
			timeString += "0";
		}
		timeString = timeString.substring(0, 7);
		return timeString;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(velocityX) > Math.abs(velocityY)) {
			if (velocityX < 0) {
				onNext();
			} else if (velocityX > 0) {
				onPrevious();
			}
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
