package com.gallantrealm.webworld;

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
import com.gallantrealm.myworld.android.MessageDialog;
import com.gallantrealm.myworld.android.StartWorldActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.client.model.ClientModelChangedListener;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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

public class SelectWorldActivity extends GallantActivity implements View.OnClickListener, ClientModelChangedListener, OnGestureListener {

	int nworlds;
	int currentWorldNum = 1;

	View mainLayout;
	TextView titleText;
	TextView countText;
	Button previousButton;
	Button nextButton;
	View selectedView;
	TextView worldNameText;
	ImageView worldImage;
	TextView worldDescriptionText;
	TextView worldScoreText;
	Button okButton;
	Button resetButton;
	Button customizeButton;

	GestureDetector gestureDetector;

	ArrayList<String> worldFolders;
	Properties worldProps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_world2);

		mainLayout = findViewById(R.id.mainLayout);
		titleText = (TextView) findViewById(R.id.titleText);
		countText = (TextView) findViewById(R.id.countText);
		selectedView = findViewById(R.id.selectedView);
		worldNameText = (TextView) findViewById(R.id.avatarNameText);
		worldImage = (ImageView) findViewById(R.id.avatarImage);
		worldDescriptionText = (TextView) findViewById(R.id.avatarDescriptionText);
		worldScoreText = (TextView) findViewById(R.id.worldScoreText);
		previousButton = (Button) findViewById(R.id.previousButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		okButton = (Button) findViewById(R.id.okButton);
		resetButton = (Button) findViewById(R.id.resetScoreButton);
		customizeButton = (Button) findViewById(R.id.customizeButton);

		mainLayout.setBackgroundResource(clientModel.getTheme().themeBackgroundId);
		songId = clientModel.getTheme().themeSongId;

		Typeface typeface = clientModel.getTypeface(this);
		if (typeface != null) {
			titleText.setTypeface(typeface);
			countText.setTypeface(typeface);
			worldNameText.setTypeface(typeface);
			okButton.setTypeface(typeface);
			resetButton.setTypeface(typeface);
			customizeButton.setTypeface(typeface);
		}

		int styleId = clientModel.getTheme().buttonStyleId;
		if (styleId != 0) {
			okButton.setBackgroundResource(styleId);
			resetButton.setBackgroundResource(styleId);
			customizeButton.setBackgroundResource(styleId);
		}

		previousButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		okButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);
		customizeButton.setOnClickListener(this);

		worldFolders = new ArrayList<String>();
		AsyncTask.execute(new Runnable() {
			public void run() {
				HttpURLConnection connection = null;
				InputStream inputStream = null;
				try {
					System.out.println(">> http://gallantrealm.com/webworld/listWorlds.jsp");
					connection = (HttpURLConnection) (new URL("http://gallantrealm.com/webworld/listWorlds.jsp")).openConnection();
					inputStream = connection.getInputStream();
					Reader reader = new InputStreamReader(inputStream, "UTF-8");
					StreamTokenizer tokenizer = new StreamTokenizer(reader);
					while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
						if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
							worldFolders.add(tokenizer.sval);
						}
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				}
				System.out.println(worldFolders);
				nworlds = worldFolders.size();
				currentWorldNum = 1;
				for (int i = 0; i < worldFolders.size(); i++) {
					if (worldFolders.get(i).equals(clientModel.getWorldName())) {
						currentWorldNum = i + 1;
					}
				}
				String worldName = worldFolders.get(currentWorldNum - 1);
				clientModel.setWorldName(worldName, "com.gallantrealm.webworld.model.World");
				getWorldProperties(worldName);
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
			if (!clientModel.isWorldUnlocked(currentWorldNum)) {
				(new MessageDialog(this, null, "Sorry, can't select because it is locked.", new String[] { "OK" })).show();
			} else {
				this.finish();
			}
		} else if (v.equals(resetButton)) {
			String worldName = clientModel.getWorldName();
			if (worldName != null) {
				this.resetScore();
			}
		} else if (v.equals(customizeButton)) {
			System.out.println("launching world for customization");
			clientModel.setCustomizeMode(true);
			Intent intent = new Intent(SelectWorldActivity.this, StartWorldActivity.class);
			startActivity(intent);
		}
		clientModel.savePreferences(this);
		updateUI();
	}

	@Override
	public void onNext() {
		if (currentWorldNum < nworlds) {
			Animation animation = new TranslateAnimation(selectedView.getWidth() / 4, 0, 0, 0);
			animation.setDuration(250);
			selectedView.startAnimation(animation);
			currentWorldNum += 1;
			if (clientModel.isWorldUnlocked(currentWorldNum)) {
				String worldName = worldFolders.get(currentWorldNum - 1);
				clientModel.setWorldName(worldName, "com.gallantrealm.webworld.model.World");
				getWorldProperties(worldName);
			}
		}
	}

	@Override
	public void onPrevious() {
		if (currentWorldNum > 1) {
			Animation animation = new TranslateAnimation(-selectedView.getWidth() / 4, 0, 0, 0);
			animation.setDuration(250);
			selectedView.startAnimation(animation);
			currentWorldNum -= 1;
			if (clientModel.isWorldUnlocked(currentWorldNum)) {
				String worldName = worldFolders.get(currentWorldNum - 1);
				clientModel.setWorldName(worldName, "com.gallantrealm.webworld.model.World");
				getWorldProperties(worldName);
			}
		}
	}

	void resetScore() {
		if (!clientModel.isWorldUnlocked(currentWorldNum)) {
			return;
		}
		final MessageDialog messageDialog = new MessageDialog(this, null, "Reset level or score for " + clientModel.getWorldName() + "?", new String[] { "Yes", "No" }, null);
		messageDialog.show();
		messageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if (messageDialog.getButtonPressed() == 0) {
					
					// TODO: Allow scores and levels to be maintained by world name, to avoid inserted worlds messing up scoring.
					clientModel.setScore(currentWorldNum, 0);
					clientModel.setLevel(currentWorldNum, 0);
					SelectWorldActivity.this.updateUI();
				}
			}
		});
	}

	@SuppressLint("NewApi")
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

	public void getWorldProperties(final String worldName) {
		worldProps = new Properties();
		AsyncTask.execute(new Runnable() {
			public void run() {
				HttpURLConnection connection = null;
				InputStream inputStream = null;
				try {
					URL url = new URL("http://gallantrealm.com/webworld/worlds/" + worldName + "/world.properties");
					System.out.println(">> " + url);
					connection = (HttpURLConnection) (url.openConnection());
					inputStream = connection.getInputStream();
					worldProps.load(inputStream);
					worldProps.list(System.out);
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
		if (worldFolders == null || worldProps == null) {
			return;
		}
		int n = currentWorldNum;
		worldNameText.setText(worldProps.getProperty("name", ""));
		worldDescriptionText.setText(worldProps.getProperty("description", ""));
		worldScoreText.setText(getScoreString(n));
		worldImage.setImageBitmap(null);
		AsyncTask.execute(new Runnable() {
			public void run() {
				HttpURLConnection connection = null;
				InputStream inputStream = null;
				try {
					String worldName = worldFolders.get(currentWorldNum - 1);
					URL url = new URL("http://gallantrealm.com/webworld/worlds/" + worldName + "/" + worldProps.getProperty("picture"));
					System.out.println(">> " + url);
					connection = (HttpURLConnection) (url.openConnection());
					inputStream = connection.getInputStream();
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					runOnUiThread(new Runnable() {
						public void run() {
							worldImage.setImageBitmap(bitmap);
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
		if (n >= nworlds) {
			nextButton.setVisibility(View.INVISIBLE);
		} else {
			nextButton.setVisibility(View.VISIBLE);
		}
		countText.setText("" + n + " of " + nworlds);
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
