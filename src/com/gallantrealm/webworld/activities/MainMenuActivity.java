package com.gallantrealm.webworld.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.SelectWorldActivityNew;

public class MainMenuActivity extends com.gallantrealm.myworld.android.MainMenuActivity {

	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		clientModel.setContext(this);
		clientModel.setAllWorldsUnlocked();  // override default behavior.  Eggworld users don't like unlocking games one by one.
		super.onCreate(savedInstanceState);
		chooseAvatarButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, SelectAvatarActivity.class);
				startActivity(intent);
			}
		});
		chooseWorldButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, SelectWorldActivityNew.class);
				startActivity(intent);
			}
		});
	}

}
