package com.gallantrealm.webworld.worlds;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

/**
 * Jump from balloon to balloon, trying not to fall.
 * 
 * @author owingsbj
 * 
 */
public class BounceWorld extends BaseWebWorld {

	WWObject avatar;
	float highestHeight = 0;
	long startTime;
	boolean playing;

	public BounceWorld(String saveWorldFileName, String avatarName) {
		setName("Bounce");
		setGravity(9.8f); // earth gravity
		
		// the ground
		WWSimpleShape ground = new WWCylinder(); // for the ground
		ground.setMonolithic(true);
		ground.setFixed(true);
		ground.setElasticity(0.5f);
		ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x400040));
		ground.setPosition(new WWVector(0, 0, -50.5f));
		ground.setSize(new WWVector(1000, 1000, 100));
		ground.setImpactSound("grass");
		addObject(ground);

		// walls
		WWSimpleShape walls = new WWBox();
		walls.shadowless = true;
		walls.setFixed(true);
		walls.setPenetratable(true);
		walls.setElasticity(1);
		walls.setColor(SIDE_ALL, new WWColor(0xF060F0));
		walls.setTextureURL(SIDE_ALL, "ivy");
		walls.setTransparency(SIDE_SIDE1, 1);
		walls.setTransparency(SIDE_SIDE2, 1);
		walls.setTransparency(SIDE_SIDE3, 1);
		walls.setTransparency(SIDE_SIDE4, 1);
		walls.setSize(new WWVector(100, 100, 100));
		walls.setPosition(new WWVector(0, 0, 25));
		walls.setHollow(0.9f);
		walls.setImpactSound("concrete");
		addObject(walls);

		// balloons
		for (int i = 0; i < 50; i++) {
			WWSimpleShape balloon = new WWSphere();
			balloon.setCircleVertices(32);
			//balloon.setFixed(true);
			balloon.setMonolithic(true);
			balloon.setPenetratable(true);
			balloon.setElasticity(3.0f);
			balloon.setColor(WWSimpleShape.SIDE_ALL, new WWColor(FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f));
			balloon.setSize(new WWVector(10, 10, 10));
			balloon.setPosition(FastMath.random(-40, 40), FastMath.random(-40, 40), FastMath.random(0, 50));
			balloon.setElasticity(0.9f);
			balloon.setImpactSound("bonk4");
			addObject(balloon);
		}

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		user.setAvatarId(avatar.getId());
		avatar.setElasticity(1);
		avatar.freedomMoveX = true;
		avatar.freedomMoveY = true;
		avatar.freedomMoveZ = true;
		TimerBehavior measureBehavior = new TimerBehavior();
		avatar.addBehavior(measureBehavior);
		measureBehavior.setTimer(100);

		setAvatarActions(new WWAction[] { new StartAction() });

		AndroidClientModel.getClientModel().cameraDampRate = 2;
	}

	class StartAction extends WWAction {
		@Override
		public String getName() {
			return "Start";
		}

		@Override
		public void start() {
			if (AndroidClientModel.getClientModel().paused) {
				AndroidClientModel.getClientModel().resumeWorld();
			}
			startPlaying();
		}
	}

	public void startPlaying() {
		AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint()); // to force view from viewpoint
		playing = true;
		AndroidClientModel.getClientModel().flashMessage("Start bouncing!", false);
		playSong("bounce_music", 0.25f);
		setAvatarActions(new WWAction[0]);
		startTime = getWorldTime();
		hideBannerAds();
	}

	public void stopPlaying() {
		playing = false;
		stopPlayingSong();
	}

	public void resetPlay() {
		showBannerAds();
		setAvatarActions(new WWAction[] { new StartAction() });
		highestHeight = 0;
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	@Override
	public boolean controller(float deltaX, float deltaY) {
		if (playing) {
			AndroidClientModel.getClientModel().forceAvatar(FastMath.min(20, FastMath.max(-20, deltaY)), deltaX, 0, 0, 0);
		} else {
			AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0);
		}
		return true;
	}

	class TimerBehavior extends WWBehavior {

		@Override
		public boolean timerEvent() {
			if (playing) {
				long time = (getWorldTime() - startTime) / 1000;
				float oldHighest = highestHeight;
				float height = avatar.getPosition().z;
				highestHeight = FastMath.max(height, highestHeight);
				BounceWorld.this.setStatus("Height: " + (int) (height * 10) / 10.0f + "  Highest: " + (int) (highestHeight * 10) / 10.0f + " Time: " + time);
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
				if (oldHighest > 10 && oldHighest < highestHeight) {
					playSound("winningSound", 0.05f);
					AndroidClientModel.getClientModel().flashMessage("New highest!!", true);
				}
				if (time >= 60) {
					stopPlaying();
					int score = (int) highestHeight;
					int rc;
					if (score > AndroidClientModel.getClientModel().getScore(9)) {
						AndroidClientModel.getClientModel().setScore(9, score);
						playSound("winningSound", 0.125f);
						if (AndroidClientModel.getClientModel().isPlayMusic()) {
							playSound("winningSound2", 0.1f);
						}
						rc = AndroidClientModel.getClientModel().alert("Congratulations!", "Your highest is " + score + ".  That's your highest level!!", new String[] { "Play Again", "Quit" }, "I bounced to level " + score + " on " + BounceWorld.this.getName() + "!");
					} else {
						rc = AndroidClientModel.getClientModel().alert("Game over.  Your highest is " + score + "\nDo you want to play again?", new String[] { "Play Again", "Quit" });
					}
					if (rc == 0) {
						resetPlay();
					}
					if (rc == 1) {
						AndroidClientModel.getClientModel().disconnect();
						return true;
					}
				}
				setTimer(250);
			} else {
				BounceWorld.this.setStatus(" ");
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
				setTimer(250);
			}
			return true;
		}
	}

}
