package com.gallantrealm.webworld.worlds;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

/**
 * Jump from balloon to balloon, trying not to fall.
 * 
 * @author owingsbj
 * 
 */
public class TrampolineWorld extends BaseWebWorld {

	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	WWSimpleShape ground;
	WWObject egg;
	WWObject trampoline;
	int jumpCount;
	boolean playing;

	public TrampolineWorld(String saveWorldFileName, String avatarName) {
		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = false;
		setName("Trampoline");
		setGravity(9.8f); // earth gravity

		// the ground
		ground = new WWCylinder();
		ground.setMonolithic(true);
		ground.setFixed(true);
		ground.setElasticity(0.5f);
		ground.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		ground.setPosition(new WWVector(0, 0, -50.5f));
		ground.setSize(new WWVector(1000, 1000, 100));
		ground.setImpactSound("grass");
		addObject(ground);

		// the trampoline
		trampoline = new WWBox();
		trampoline.setTextureURL(SIDE_ALL, "ice");
		trampoline.setSize(5, 5, 1);
		trampoline.setPosition(0, 0, 1);
		trampoline.freedomMoveX = true;
		trampoline.freedomMoveY = true;
		trampoline.setElasticity(1.15f);
		addObject(trampoline);

		// walls
		WWSimpleShape walls = new WWBox();
		walls.setFixed(true);
		walls.setPenetratable(true);
		walls.shadowless = true;
		walls.setElasticity(1);
		walls.setColor(SIDE_ALL, new WWColor(0xF060F0));
		walls.setTextureURL(SIDE_ALL, "concrete");
		walls.setTransparency(SIDE_SIDE1, 1);
		walls.setTransparency(SIDE_SIDE2, 1);
		walls.setTransparency(SIDE_SIDE3, 1);
		walls.setTransparency(SIDE_SIDE4, 1);
		walls.setSize(new WWVector(100, 100, 100));
		walls.setPosition(new WWVector(0, 0, 25));
		walls.setHollow(0.9f);
		walls.setImpactSound("concrete");
		addObject(walls);

		egg = makeAvatar(avatarName);
		egg.setPhysical(false);
		egg.setPosition(0, 0, 4);
		egg.setElasticity(1.15f);
		egg.freedomMoveX = true;
		egg.freedomMoveY = true;
		egg.freedomMoveZ = true;
		egg.addBehavior(new EggBehavior());

		// user is trampoline
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		user.setAvatarId(trampoline.getId());

		worldActions = new WWAction[] { new PauseAction() };
		setAvatarActions(new WWAction[] { new StartAction() });

		resetPlay();

		AndroidClientModel.getClientModel().cameraDampRate = 5;
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
		playing = true;
		clientModel.calibrateSensors();
		AndroidClientModel.getClientModel().flashMessage("Jump!", false);
		egg.setPhysical(true);
		playSong("bounce_music", 0.25f);
		setAvatarActions(new WWAction[0]);
		jumpCount = 0;
		hideBannerAds();
	}

	public void stopPlaying() {
		playing = false;
		egg.setPhysical(false);
		egg.setVelocity(new WWVector());
		stopPlayingSong();
	}

	public void resetPlay() {
		showBannerAds();
		setAvatarActions(new WWAction[] { new StartAction() });
		jumpCount = 0;
		egg.setPosition(0, 0, 4);
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	float lastx, lasty, lastlastx, lastlasty;

	@Override
	public boolean controller(float deltaX, float deltaY) {
		if (playing) {
			float avgx = (deltaX + lastx + lastlastx) / 3.0f;
			float avgy = (deltaY + lasty + lastlasty) / 3.0f;
			WWVector newPosition = trampoline.getPosition().add(avgx * 0.01f, avgy * 0.01f, 0);
			newPosition.x = FastMath.min(40, FastMath.max(-40, newPosition.x));
			newPosition.y = FastMath.min(40, FastMath.max(-40, newPosition.y));
			trampoline.setPosition(newPosition);
			lastlastx = lastx;
			lastlasty = lasty;
			lastx = deltaX;
			lasty = deltaY;
		} else {
			trampoline.setPosition(0, 0, 1);
		}
		return true;
	}

	@Override
	public boolean allowCameraPositioning() {
		return false;
	}

	class EggBehavior extends WWBehavior {

		public EggBehavior() {
			setTimer(250);
		}

		@Override
		public boolean timerEvent() {
			clientModel.setCameraObject(trampoline);
			clientModel.setCameraDistance(20);
			clientModel.setCameraTilt(45.0f);
			clientModel.setCameraPan(180.0f);
			//setTimer(250);
			return true;
		}

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (nearObject == trampoline) {
				playSound("bonk4", 1);
				jumpCount += 1;
				float r = jumpCount * 1.0f;
				object.setVelocity(new WWVector(FastMath.random(-r, r), FastMath.random(-r, r), object.getVelocity().z));
				TrampolineWorld.this.setStatus("Jumps: " + jumpCount);
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
				AndroidClientModel.getClientModel().vibrate(50);
			} else if (nearObject == ground) {
				AndroidClientModel.getClientModel().vibrate(250);
				stopPlaying();
				int score = jumpCount;
				int rc;
				if (score > AndroidClientModel.getClientModel().getScore(12)) {
					AndroidClientModel.getClientModel().setScore(12, score);
					playSound("winningSound", 0.125f);
					if (clientModel.isPlayMusic()) {
						playSound("winningSound2", 0.1f);
					}
					rc = AndroidClientModel.getClientModel().alert("Congratulations!", "Your score is " + score + ".  That's your highest score!!", new String[] { "Play Again", "Quit" }, "I jumped " + score + " times in " + TrampolineWorld.this.getName() + "!");
				} else {
					playSound("loosingSound", 0.5f);
					rc = AndroidClientModel.getClientModel().alert("Game over.  Your score is " + score + "\nDo you want to play again?", new String[] { "Play Again", "Quit" });
				}
				if (rc == 0) {
					resetPlay();
				}
				if (rc == 1) {
					AndroidClientModel.getClientModel().disconnect();
					return true;
				}
			}
			return true;
		}

	}

}
