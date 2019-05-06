package com.gallantrealm.webworld.worlds;

import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWTorus;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

public class ObstacleWorld extends BaseEggWorld {

	WWSimpleShape avatar1;
	ArrayList<WWObject> obstacles = new ArrayList<WWObject>();
	boolean playing;

	public ObstacleWorld(String saveWorldFileName, String avatarName) {
		setLevel(Math.max(1, AndroidClientModel.getClientModel().getScore(3)));

		// create a world, add ground, some objects, and one "egg" user
		setName("Obstacle");
		setGravity(9.8f); // earth gravity

		// the ground
		WWSimpleShape ground = new WWCylinder(); // for the ground
		ground.setMonolithic(true);
		ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x008000)); // green, like grass
		ground.setPosition(new WWVector(0, 0, -50.5f));
		ground.setSize(new WWVector(1000, 1000, 100));
		addObject(ground);

		// platforms
		WWSimpleShape platform1 = new WWBox();
		platform1.setMonolithic(true);
		platform1.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		platform1.setSize(new WWVector(5, 5, 5));
		platform1.setPosition(new WWVector(0, -50, 100));
		platform1.setImpactSound("wood");
		addObject(platform1);

		WWSimpleShape platform2 = new WWBox();
		platform2.setMonolithic(true);
		platform2.setPickable(true);
		platform2.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		platform2.setSize(new WWVector(5, 5, 5));
		platform2.setPosition(new WWVector(0, 50, 100));
		platform2.setImpactSound("wood");
		platform2.addBehavior(new FinishLineBehavior());
		addObject(platform2);

		// path between the platforms
		WWSimpleShape path = new WWBox();
		path.setMonolithic(true);
		path.setElasticity(-1);
		path.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		path.setSize(new WWVector(2, 100, 1));
		path.setPosition(new WWVector(0, 0, 101.99f));
		//path.setImpactSound("wood");  too woodpeckery
		addObject(path);

		// a surface that if hit is "out"
		WWSimpleShape outLine = new WWBox();
		outLine.setSolid(false);
		outLine.setPenetratable(true);
		outLine.setTransparency(WWBox.SIDE_ALL, 1);
		outLine.setSize(new WWVector(20, 120, 1));
		outLine.setPosition(new WWVector(0, 0, 95));
		outLine.addBehavior(new OutLineBehavior());
		addObject(outLine);

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar1 = makeAvatar(avatarName);
		avatar1.setPosition(new WWVector(0, -50, 110));
		avatar1.setRotation(new WWVector(0, 0, 180));
		user.setAvatarId(avatar1.getId());

		resetPlay();
	}

	public void clearObstacles() {
		for (int i = 0; i < obstacles.size(); i++) {
			removeObject(obstacles.get(i).getId());
		}
		obstacles.clear();
		AndroidRenderer.clearRenderings();
	}

	public void createObstacles() {
		// the obstacles
		for (int i = 0; i < 10; i++) {

			int obstacleType = (int) (Math.random() * getLevel());

			if (obstacleType == 0) {

				// box
				WWSimpleShape box = new WWBox();
				box.setMonolithic(true);
				box.setColor(WWSimpleShape.SIDE_ALL, new WWColor(FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f));
				box.setSize(new WWVector(2.5f, 2.5f, 2.5f));
				box.setPosition(new WWVector(0, i * 8 - 40, 101.125f));
				box.setAMomentum(new WWVector(0, -10, 0));
				box.setImpactSound("wood");
				addObject(box);
				obstacles.add(box);

			} else if (obstacleType == 1) {

				// faster box
				WWSimpleShape box = new WWBox();
				box.setMonolithic(true);
				box.setColor(WWSimpleShape.SIDE_ALL, new WWColor(FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f));
				box.setSize(new WWVector(5.0f, 2.5f, 2.5f));
				box.setPosition(new WWVector(0, i * 8 - 40, 101.125f));
				box.setAMomentum(new WWVector(0, 30, 0));
				box.setImpactSound("wood");
				addObject(box);
				obstacles.add(box);

			} else if (obstacleType == 2) {

				// donut
				WWSimpleShape donut = new WWTorus();
				donut.setMonolithic(true);
				donut.setColor(WWSimpleShape.SIDE_ALL, new WWColor(FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f));
				donut.setSize(new WWVector(5, 5, 2));
				donut.setRotation(new WWVector(90, 0, 0));
				donut.setPosition(new WWVector(0, i * 8 - 40, 103));
				donut.setAMomentum(new WWVector(0, 0, 30));
				donut.setImpactSound("wood");
				addObject(donut);
				obstacles.add(donut);

			} else {

				// faster donut
				WWSimpleShape donut = new WWTorus();
				donut.setMonolithic(true);
				donut.setColor(WWSimpleShape.SIDE_ALL, new WWColor(FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f, FastMath.random() / 2.0f + 0.5f));
				donut.setSize(new WWVector(5, 5, 2));
				donut.setRotation(new WWVector(90, 0, 0));
				donut.setPosition(new WWVector(0, i * 8 - 40, 103));
				donut.setAMomentum(new WWVector(0, 0, 60));
				donut.setImpactSound("wood");
				addObject(donut);
				obstacles.add(donut);

			}
		}
	}

	@Override
	public float[] getMoveXTurn() {
		if (!playing) {
			return new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		}
		return new float[] { -15, -10, -5, 0, 5, 10, 15 };
	}

	@Override
	public float[] getMoveYThrust() {
		if (!playing) {
			return new float[] { 0, 0, 0, 0, 0 };
		}
		return new float[] { -2, -1, 0, 2, 4 };
	}

	class OutLineBehavior extends WWBehavior {
		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			stopPlaying();
			playSound("loosingSound", 0.25f);
			ClientModel clientModel = AndroidClientModel.getClientModel();
			int rc = clientModel.alert("Sorry, you fell off!\nDo you want to play again?", new String[] { "Play Again", "Quit" });
			if (rc == 0) {
				resetPlay();
			}
			if (rc == 1) {
				clientModel.disconnect();
			}
			return true;
		}
	}

	private class FinishLineBehavior extends WWBehavior {

		@Override
		public synchronized boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (playing) {
				playing = false;
				stopPlayingSong();
				playSound("winningSound", 0.125f);
				if (AndroidClientModel.getClientModel().isPlayMusic()) {
					playSound("winningSound2", 0.1f);
				}
				AndroidClientModel clientModel = AndroidClientModel.getClientModel();
				int rc = clientModel.alert(null, "Congratulations, you made it!", new String[] { "Next Level", "Quit" }, "I made it through level " + getLevel() + " of Obstacle world!");
				if (rc == 0) {
					setLevel(getLevel() + 1);
					clientModel.setScore(3, getLevel());
					clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
				} else {
					clientModel.disconnect();
				}
				resetPlay();
			}
			return true;
		}

	}

	public void startPlaying() {
		AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint()); // to force view from viewpoint
		playSong("obstacle_music", 0.2f);
		setAvatarActions(new WWAction[] {});
		playing = true;
		hideBannerAds();
	}

	public void stopPlaying() {
		playing = false;
		stopPlayingSong();
	}

	public void resetPlay() {
		showBannerAds();
		avatar1.setVelocity(new WWVector(0, 0, 0));
		avatar1.setPosition(new WWVector(0, -50, 110));
		avatar1.setRotation(new WWVector(0, 0, 180));
		clearObstacles();
		createObstacles();
		setAvatarActions(new WWAction[] { new StartAction() });
	}

	class StartAction extends WWAction {

		@Override
		public String getName() {
			return "Start";
		}

		@Override
		public void start() {
			startPlaying();
		}

	}
}
