package com.gallantrealm.webworld;

import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.GallantActivity;
import com.gallantrealm.myworld.android.MessageDialog;
import com.gallantrealm.myworld.android.StartWorldActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.client.model.ClientModelChangedListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SelectAvatarActivity extends GallantActivity implements View.OnClickListener, ClientModelChangedListener {

	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	TextView titleText;
	Button okButton;
	Button decorateButton;
	View happySelect;
	View jackSelect;
	View jillSelect;
	View patchSelect;
	View robotSelect;
	View mongoSelect;
	View cupidSelect;
	View luckySelect;
	View bugsSelect;
	View casperSelect;
	View gobbleSelect;
	View santaSelect;
	View custom1Select;
	View custom2Select;
	View custom3Select;
	View custom4Select;
	View custom5Select;
	View custom6Select;
	View custom7Select;
	View custom8Select;
	View custom9Select;
	View custom10Select;
	View custom11Select;
	View custom12Select;
	TextView custom1Text;
	TextView custom2Text;
	TextView custom3Text;
	TextView custom4Text;
	TextView custom5Text;
	TextView custom6Text;
	TextView custom7Text;
	TextView custom8Text;
	TextView custom9Text;
	TextView custom10Text;
	TextView custom11Text;
	TextView custom12Text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_avatar);

		titleText = (TextView) findViewById(R.id.titleText);
		happySelect = findViewById(R.id.happySelect);
		jackSelect = findViewById(R.id.jackSelect);
		jillSelect = findViewById(R.id.jillSelect);
		patchSelect = findViewById(R.id.patchSelect);
		robotSelect = findViewById(R.id.robotSelect);
		mongoSelect = findViewById(R.id.mongoSelect);
		cupidSelect = findViewById(R.id.cupidSelect);
		luckySelect = findViewById(R.id.luckySelect);
		bugsSelect = findViewById(R.id.bugsSelect);
		casperSelect = findViewById(R.id.casperSelect);
		gobbleSelect = findViewById(R.id.gobbleSelect);
		santaSelect = findViewById(R.id.santaSelect);
		custom1Select = findViewById(R.id.custom1Select);
		custom2Select = findViewById(R.id.custom2Select);
		custom3Select = findViewById(R.id.custom3Select);
		custom4Select = findViewById(R.id.custom4Select);
		custom5Select = findViewById(R.id.custom5Select);
		custom6Select = findViewById(R.id.custom6Select);
		custom7Select = findViewById(R.id.custom7Select);
		custom8Select = findViewById(R.id.custom8Select);
		custom9Select = findViewById(R.id.custom9Select);
		custom10Select = findViewById(R.id.custom10Select);
		custom11Select = findViewById(R.id.custom11Select);
		custom12Select = findViewById(R.id.custom12Select);
		custom1Text = (TextView) findViewById(R.id.custom1text);
		custom2Text = (TextView) findViewById(R.id.custom2text);
		custom3Text = (TextView) findViewById(R.id.custom3text);
		custom4Text = (TextView) findViewById(R.id.custom4text);
		custom5Text = (TextView) findViewById(R.id.custom5text);
		custom6Text = (TextView) findViewById(R.id.custom6text);
		custom7Text = (TextView) findViewById(R.id.custom7text);
		custom8Text = (TextView) findViewById(R.id.custom8text);
		custom9Text = (TextView) findViewById(R.id.custom9text);
		custom10Text = (TextView) findViewById(R.id.custom10text);
		custom11Text = (TextView) findViewById(R.id.custom11text);
		custom12Text = (TextView) findViewById(R.id.custom12text);
		okButton = (Button) findViewById(R.id.okButton);
		decorateButton = (Button) findViewById(R.id.customizeButton);

		Typeface typeface = clientModel.getTypeface(this);
		if (typeface != null) {
			titleText.setTypeface(typeface);
			okButton.setTypeface(typeface);
			decorateButton.setTypeface(typeface);
		}

		happySelect.setOnClickListener(this);
		jackSelect.setOnClickListener(this);
		jillSelect.setOnClickListener(this);
		patchSelect.setOnClickListener(this);
		robotSelect.setOnClickListener(this);
		mongoSelect.setOnClickListener(this);
		cupidSelect.setOnClickListener(this);
		luckySelect.setOnClickListener(this);
		bugsSelect.setOnClickListener(this);
		casperSelect.setOnClickListener(this);
		gobbleSelect.setOnClickListener(this);
		santaSelect.setOnClickListener(this);
		custom1Select.setOnClickListener(this);
		custom2Select.setOnClickListener(this);
		custom3Select.setOnClickListener(this);
		custom4Select.setOnClickListener(this);
		custom5Select.setOnClickListener(this);
		custom6Select.setOnClickListener(this);
		custom7Select.setOnClickListener(this);
		custom8Select.setOnClickListener(this);
		custom9Select.setOnClickListener(this);
		custom10Select.setOnClickListener(this);
		custom11Select.setOnClickListener(this);
		custom12Select.setOnClickListener(this);
		okButton.setOnClickListener(this);
		decorateButton.setOnClickListener(this);

		updateSelectedAvatar();
		clientModel.addClientModelChangedListener(this);
	}

	public void onClick(View v) {
		if (v.equals(happySelect)) {
			clientModel.setAvatarName("happy");
		} else if (v.equals(jackSelect)) {
			clientModel.setAvatarName("jack");
		} else if (v.equals(jillSelect)) {
			clientModel.setAvatarName("jill");
		} else if (v.equals(patchSelect)) {
			clientModel.setAvatarName("patch");
		} else if (v.equals(robotSelect)) {
			clientModel.setAvatarName("robot");
		} else if (v.equals(mongoSelect)) {
			clientModel.setAvatarName("mongo");
		} else if (v.equals(cupidSelect)) {
			clientModel.setAvatarName("cupid");
		} else if (v.equals(luckySelect)) {
			clientModel.setAvatarName("lucky");
		} else if (v.equals(bugsSelect)) {
			clientModel.setAvatarName("bugs");
		} else if (v.equals(casperSelect)) {
			clientModel.setAvatarName("casper");
		} else if (v.equals(gobbleSelect)) {
			clientModel.setAvatarName("gobble");
		} else if (v.equals(santaSelect)) {
			clientModel.setAvatarName("santa");
		} else if (v.equals(custom1Select)) {
			clientModel.setAvatarName("custom1");
		} else if (v.equals(custom2Select)) {
			clientModel.setAvatarName("custom2");
		} else if (v.equals(custom3Select)) {
			clientModel.setAvatarName("custom3");
		} else if (v.equals(custom4Select)) {
			clientModel.setAvatarName("custom4");
		} else if (v.equals(custom5Select)) {
			clientModel.setAvatarName("custom5");
		} else if (v.equals(custom6Select)) {
			clientModel.setAvatarName("custom6");
		} else if (v.equals(custom7Select)) {
			clientModel.setAvatarName("custom7");
		} else if (v.equals(custom8Select)) {
			clientModel.setAvatarName("custom8");
		} else if (v.equals(custom9Select)) {
			clientModel.setAvatarName("custom9");
		} else if (v.equals(custom10Select)) {
			clientModel.setAvatarName("custom10");
		} else if (v.equals(custom11Select)) {
			clientModel.setAvatarName("custom11");
		} else if (v.equals(custom12Select)) {
			clientModel.setAvatarName("custom12");
		} else if (v.equals(okButton)) {
			this.finish();
		} else if (v.equals(decorateButton)) {
			startDecorator();
		}
		clientModel.savePreferences(this);
	}

	@Override
	protected void onStart() {
		songId = R.raw.theme_song;
		super.onStart();
		updateSelectedAvatar();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void clientModelChanged(ClientModelChangedEvent event) {
		if (event.getEventType() == ClientModelChangedEvent.EVENT_TYPE_SELECTED_AVATAR_CHANGED) {
			updateSelectedAvatar();
		}
	}

	public void updateSelectedAvatar() {
		String avatarName = clientModel.getAvatarName();
		if (avatarName == null) {
			return;
		}
		happySelect.setBackgroundColor(avatarName.equals("happy") ? 0x8040f040 : 0x80404040);
		jackSelect.setBackgroundColor(avatarName.equals("jack") ? 0x8040f040 : 0x80404040);
		jillSelect.setBackgroundColor(avatarName.equals("jill") ? 0x8040f040 : 0x80404040);
		patchSelect.setBackgroundColor(avatarName.equals("patch") ? 0x8040f040 : 0x80404040);
		robotSelect.setBackgroundColor(avatarName.equals("robot") ? 0x8040f040 : 0x80404040);
		mongoSelect.setBackgroundColor(avatarName.equals("mongo") ? 0x8040f040 : 0x80404040);
		cupidSelect.setBackgroundColor(avatarName.equals("cupid") ? 0x8040f040 : 0x80404040);
		luckySelect.setBackgroundColor(avatarName.equals("lucky") ? 0x8040f040 : 0x80404040);
		bugsSelect.setBackgroundColor(avatarName.equals("bugs") ? 0x8040f040 : 0x80404040);
		casperSelect.setBackgroundColor(avatarName.equals("casper") ? 0x8040f040 : 0x80404040);
		gobbleSelect.setBackgroundColor(avatarName.equals("gobble") ? 0x8040f040 : 0x80404040);
		santaSelect.setBackgroundColor(avatarName.equals("santa") ? 0x8040f040 : 0x80404040);
		custom1Select.setBackgroundColor(avatarName.equals("custom1") ? 0x8040f040 : 0x80404040);
		custom2Select.setBackgroundColor(avatarName.equals("custom2") ? 0x8040f040 : 0x80404040);
		custom3Select.setBackgroundColor(avatarName.equals("custom3") ? 0x8040f040 : 0x80404040);
		custom4Select.setBackgroundColor(avatarName.equals("custom4") ? 0x8040f040 : 0x80404040);
		custom5Select.setBackgroundColor(avatarName.equals("custom5") ? 0x8040f040 : 0x80404040);
		custom6Select.setBackgroundColor(avatarName.equals("custom6") ? 0x8040f040 : 0x80404040);
		custom7Select.setBackgroundColor(avatarName.equals("custom7") ? 0x8040f040 : 0x80404040);
		custom8Select.setBackgroundColor(avatarName.equals("custom8") ? 0x8040f040 : 0x80404040);
		custom9Select.setBackgroundColor(avatarName.equals("custom9") ? 0x8040f040 : 0x80404040);
		custom10Select.setBackgroundColor(avatarName.equals("custom10") ? 0x8040f040 : 0x80404040);
		custom11Select.setBackgroundColor(avatarName.equals("custom11") ? 0x8040f040 : 0x80404040);
		custom12Select.setBackgroundColor(avatarName.equals("custom12") ? 0x8040f040 : 0x80404040);
		custom1Text.setText(clientModel.getAvatarDisplayName(13, "custom1"));
		custom2Text.setText(clientModel.getAvatarDisplayName(14, "custom2"));
		custom3Text.setText(clientModel.getAvatarDisplayName(15, "custom3"));
		custom4Text.setText(clientModel.getAvatarDisplayName(16, "custom4"));
		custom5Text.setText(clientModel.getAvatarDisplayName(17, "custom5"));
		custom6Text.setText(clientModel.getAvatarDisplayName(18, "custom6"));
		custom7Text.setText(clientModel.getAvatarDisplayName(19, "custom7"));
		custom8Text.setText(clientModel.getAvatarDisplayName(20, "custom8"));
		custom9Text.setText(clientModel.getAvatarDisplayName(21, "custom9"));
		custom10Text.setText(clientModel.getAvatarDisplayName(22, "custom10"));
		custom11Text.setText(clientModel.getAvatarDisplayName(23, "custom11"));
		custom12Text.setText(clientModel.getAvatarDisplayName(24, "custom12"));
	}

	public void startDecorator() {
		String avatarName = clientModel.getAvatarName();
		if (avatarName.startsWith("custom")) {
			Intent intent = new Intent(SelectAvatarActivity.this, StartWorldActivity.class);
			intent.setAction("com.gallantrealm.webworld.worlds.DecorateWorld");
			startActivity(intent);
		} else {
			final MessageDialog messageDialog = new MessageDialog(this, null, "Choose an avatar to customize.", new String[] { "OK" }, null);
			messageDialog.show();
		}
	}
}
