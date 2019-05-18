package com.gallantrealm.webworld.worlds;

import java.io.Serializable;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.OldPhysicsThread;
import com.gallantrealm.myworld.model.PhysicsThread;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

public class BaseWebWorld extends WWWorld {

	protected float thrust;
	protected float torque;
	protected float lift;
	protected float lean;
	protected float tilt;

	public class ChangeViewAction extends WWAction implements Serializable {

		private int view;

		@Override
		public String getName() {
			return "View";
		}

		@Override
		public void start() {
			view++;
			if (view == 1) {
				AndroidClientModel.getClientModel().setViewpoint(1);
			} else if (view == 2) {
				AndroidClientModel.getClientModel().birdsEyeHeight = 15;
				AndroidClientModel.getClientModel().setViewpoint(2);
			} else if (view == 3) {
				AndroidClientModel.getClientModel().setViewpoint(3);
			} else {
				AndroidClientModel.getClientModel().setViewpoint(0);
				view = 0;
			}
		}

	};

	WWAction[] avatarActions = new WWAction[0];
	WWAction[] worldActions = new WWAction[] { new PauseAction(), new ChangeViewAction() };

	public BaseWebWorld() {
		super(true, true, null, 15, true);
		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = true;
		AndroidClientModel.getClientModel().cameraDampRate = 0;
		AndroidClientModel.getClientModel().calibrateSensors();
		AndroidClientModel.getClientModel().cameraDampRate = 0.5f;
		AndroidClientModel.getClientModel().behindDistance = 3;
		AndroidClientModel.getClientModel().behindTilt = 10;
	}

	public BaseWebWorld(String saveWorldFileName) {
		super(true, true, saveWorldFileName, 15, true);
		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = true;
		AndroidClientModel.getClientModel().cameraDampRate = 0;
		AndroidClientModel.getClientModel().calibrateSensors();
		AndroidClientModel.getClientModel().cameraDampRate = 0.5f;
// TODO		AndroidClientModel.getClientModel().dampCameraTranslations = true;
		AndroidClientModel.getClientModel().behindDistance = 3;
		AndroidClientModel.getClientModel().behindTilt = 10;
	}

	@Override
	public void displayed() {
		showBannerAds();
	}

	protected void setAvatarActions(WWAction[] actions) {
		avatarActions = actions;
		AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_AVATAR_ACTIONS_CHANGED);
	}

	@Override
	public WWAction[] getAvatarActions() {
		return avatarActions;
	}

	protected void setWorldActions(WWAction[] actions) {
		worldActions = actions;
		AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WORLD_ACTIONS_CHANGED);
	}

	@Override
	public WWAction[] getWorldActions() {
		return worldActions;
	}

	public WWSimpleShape makeAvatar(String avatarName) {
		WWSimpleShape avatar = new WWSphere();
		avatar.setColor(WWObject.SIDE_ALL, new WWColor(1, 1, 1));
		avatar.setName(avatarName);
		avatar.setSize(new WWVector(1, 1, 1));
		avatar.setTaperX(0.25f);
		avatar.setTaperY(0.25f);
		avatar.setPhysical(true);
		avatar.setDensity(0.1f);
		avatar.setFreedomMoveZ(true);
		avatar.setFreedomRotateX(false);
		avatar.setFreedomRotateY(false);
		avatar.setFreedomRotateZ(true);
		int avatarId = addObject(avatar);
		if (avatarName.equals("mongo")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "mongo_skin");
			makeMongoHair(avatarId);
		} else if (avatarName.equals("jill")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "jill_skin");
			makejillHair(avatarId);
		} else if (avatarName.equals("patch")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "patch_skin");
			makePatchHat(avatarId);
		} else if (avatarName.equals("robot")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "robot_skin");
			avatar.setShininess(SIDE_ALL, 0.25f);
			makeRobotAppendages(avatarId);
		} else if (avatarName.equals("bugs")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "bugs_skin");
			makeBunnyAppendages(avatarId);
		} else if (avatarName.equals("lucky")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "lucky_skin");
			makeLuckyHat(avatarId);
		} else if (avatarName.equals("cupid")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "cupid_skin");
			makeCupidAppendages(avatarId);
		} else if (avatarName.equals("casper")) {
			avatar.setTransparency(WWObject.SIDE_ALL, 1);
			makeCasper(avatarId);
		} else if (avatarName.equals("gobble")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "gobble_skin");
			makeTurkeyParts(avatarId);
		} else if (avatarName.equals("santa")) {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, "santa_skin");
			makeSantaHat(avatarId);
		} else {
			avatar.setTextureURL(WWSimpleShape.SIDE_ALL, avatarName + "_skin");
		}
		if (AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar) {
			WWBehavior avatarCameraBehavior = new WWBehavior() {
				@Override
				public boolean timerEvent() {
					AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint()); // to force view from viewpoint after some time
					return true;
				}
			};
			avatar.addBehavior(avatarCameraBehavior);
			avatarCameraBehavior.setTimer(5000);
		}
		return avatar;
	}

	public void makeMongoHair(int avatarId) {
		makeHairSpike(avatarId, 0, -30, 0);
		makeHairSpike(avatarId, 0, 0, 0);
		makeHairSpike(avatarId, 0, 30, 0);
		makeHairSpike(avatarId, 30, 0, 0);
		makeHairSpike(avatarId, 30, -60, 0);
		makeHairSpike(avatarId, 30, -30, 0);
		makeHairSpike(avatarId, 30, 30, 0);
		makeHairSpike(avatarId, 30, 60, 0);
		makeHairSpike(avatarId, 60, -60, 0);
		makeHairSpike(avatarId, 60, -30, 0);
		makeHairSpike(avatarId, 60, 0, 0);
		makeHairSpike(avatarId, 60, 30, 0);
		makeHairSpike(avatarId, 60, 60, 0);
		makeHairSpike(avatarId, 90, -30, 0);
		makeHairSpike(avatarId, 90, 0, 0);
		makeHairSpike(avatarId, 90, 30, 0);
	}

	public void makeHairSpike(int avatarId, float rotx, float roty, float rotz) {
		WWBox spike = new WWBox();
		spike.setPhantom(true);
		spike.setFixed(true);
		spike.setColor(WWBox.SIDE_ALL, new WWColor(0, 0, 0));
		spike.setShininess(SIDE_ALL, 0.8f);
		spike.setSize(new WWVector(0.5f, 0.5f, 0.5f));
		spike.setRotation(new WWVector(rotx, roty, rotz));
		spike.setPosition(-roty / 180.0f, rotx / 180.0f, 0.5f - Math.abs(rotx) / 360.0f - Math.abs(roty) / 360.0f);
		spike.setTaperX(1.0f);
		spike.setTaperY(1.0f);
		addObject(spike);
		spike.setParent(avatarId);
	}

	public void makejillHair(int avatarId) {
		makeHairWave(avatarId, 0, -30, 0);
		//makeHairWave(avatarId, 0, 0, 0);
		makeHairWave(avatarId, 0, 30, 0);
		makeHairWave(avatarId, 30, -60, 0);
		makeHairWave(avatarId, 30, -30, 0);
		makeHairWave(avatarId, 60, 0, 0);
		makeHairWave(avatarId, 30, 30, 0);
		makeHairWave(avatarId, 30, 60, 0);
		makeHairWave(avatarId, 60, -90, 0);
		makeHairWave(avatarId, 60, -60, 0);
		makeHairWave(avatarId, 90, -30, 0);
		//makeHairWave(avatarId, 120, 0, 0);
		makeHairWave(avatarId, 90, 30, 0);
		makeHairWave(avatarId, 60, 60, 0);
		makeHairWave(avatarId, 60, 90, 0);
		makeHairWave(avatarId, 90, 90, 0);
		makeHairWave(avatarId, 90, -60, 0);
		makeHairWave(avatarId, 120, -30, 0);
		//makeHairWave(avatarId, 150, 0, 0);
		makeHairWave(avatarId, 120, 30, 0);
		makeHairWave(avatarId, 90, 60, 0);
		makeHairWave(avatarId, 90, 90, 0);
	}

	public void makeHairWave(int avatarId, float rotx, float roty, float rotz) {
		WWSphere wave = new WWSphere();
		wave.setPhantom(true);
		wave.setFixed(true);
		wave.setColor(WWSphere.SIDE_ALL, new WWColor(0.4f, 0.2f, 0.2f));
		wave.setShininess(SIDE_ALL, 0.25f);
		wave.setSize(new WWVector(0.5f, 0.5f, 0.5f));
		wave.setRotation(new WWVector(rotx, roty, rotz));
		wave.setPosition(-roty / 270.0f, rotx / 270.0f, 0.5f - Math.abs(rotx) / 270.0f - Math.abs(roty) / 270.0f);
		addObject(wave);
		wave.setParent(avatarId);
	}

	public void makePatchHat(int avatarId) {
		WWColor hatcolor = new WWColor(0.3f, 0.3f, 0.2f);
		WWSimpleShape flap = new WWCylinder();
		flap.setPhantom(true);
		flap.setFixed(true);
		flap.setColor(WWSphere.SIDE_ALL, hatcolor);
		flap.setSize(new WWVector(0.5f, 1.0f, 0.7f));
		flap.setCutoutStart(0.5f);
		flap.setCutoutEnd(1.0f);
		flap.setTaperX(1.0f);
		flap.setTaperY(1.0f);
		flap.setPosition(0, 0, 0.4f);
		flap.setRotation(new WWVector(0, 90, 30));
		addObject(flap);
		flap.setParent(avatarId);

		flap = new WWCylinder();
		flap.setPhantom(true);
		flap.setFixed(true);
		flap.setColor(WWSphere.SIDE_ALL, hatcolor);
		flap.setSize(new WWVector(0.5f, 1.0f, 0.7f));
		flap.setCutoutStart(0.5f);
		flap.setCutoutEnd(1.0f);
		flap.setTaperX(1.0f);
		flap.setTaperY(1.0f);
		flap.setPosition(0, 0, 0.4f);
		flap.setRotation(new WWVector(0, 90, 150));
		addObject(flap);
		flap.setParent(avatarId);

		flap = new WWCylinder();
		flap.setPhantom(true);
		flap.setFixed(true);
		flap.setColor(WWSphere.SIDE_ALL, hatcolor);
		flap.setSize(new WWVector(0.5f, 1.0f, 0.7f));
		flap.setCutoutStart(0.5f);
		flap.setCutoutEnd(1.0f);
		flap.setTaperX(1.0f);
		flap.setTaperY(1.0f);
		flap.setPosition(0, 0, 0.4f);
		flap.setRotation(new WWVector(0, 90, -90));
		addObject(flap);
		flap.setParent(avatarId);

		WWSphere bowl = new WWSphere();
		bowl.setPhantom(true);
		bowl.setFixed(true);
		bowl.setColor(WWSphere.SIDE_ALL, hatcolor);
		bowl.setSize(new WWVector(0.5f, 0.5f, 0.5f));
		bowl.setCutoutStart(0.5f);
		bowl.setCutoutEnd(1.0f);
		bowl.setPosition(0, 0, 0.4f);
		bowl.setRotation(new WWVector(0, 90, 0));
		addObject(bowl);
		bowl.setParent(avatarId);
	}

	public void makeRobotAppendages(int avatarId) {
		//WWColor color = new WWColor(0.64f, 0.78f, 0.22f);
		WWColor color = new WWColor(0.6f, 0.75f, 0.2f);

		WWCylinder arm1 = new WWCylinder();
		arm1.setPhantom(true);
		arm1.setFixed(true);
		arm1.setColor(WWSimpleShape.SIDE_ALL, color);
		arm1.setShininess(SIDE_ALL, 0.25f);
		arm1.setSize(new WWVector(0.25f, 0.25f, 0.5f));
		arm1.setPosition(-0.4f, 0, -0.1f);
		addObject(arm1);
		arm1.setParent(avatarId);

		WWCylinder arm2 = new WWCylinder();
		arm2.setPhantom(true);
		arm2.setFixed(true);
		arm2.setColor(WWSimpleShape.SIDE_ALL, color);
		arm2.setShininess(SIDE_ALL, 0.25f);
		arm2.setSize(new WWVector(0.25f, 0.25f, 0.5f));
		arm2.setPosition(0.4f, 0, -0.1f);
		addObject(arm2);
		arm2.setParent(avatarId);

		WWCylinder ant1 = new WWCylinder();
		ant1.setPhantom(true);
		ant1.setFixed(true);
		ant1.setColor(WWSimpleShape.SIDE_ALL, color);
		ant1.setShininess(SIDE_ALL, 0.25f);
		ant1.setSize(new WWVector(0.05f, 0.05f, 0.7f));
		ant1.setPosition(0, 0, 0.25f);
		ant1.setRotation(0, 30, 0);
		addObject(ant1);
		ant1.setParent(avatarId);

		WWCylinder ant2 = new WWCylinder();
		ant2.setPhantom(true);
		ant2.setFixed(true);
		ant2.setColor(WWSimpleShape.SIDE_ALL, color);
		ant2.setShininess(SIDE_ALL, 0.25f);
		ant2.setSize(new WWVector(0.05f, 0.05f, 0.7f));
		ant2.setPosition(0, 0, 0.25f);
		ant2.setRotation(0, -30, 0);
		addObject(ant2);
		ant2.setParent(avatarId);
	}

	public void makeBunnyAppendages(int avatarId) {

		WWSimpleShape ear = new WWSphere();
		ear.setPhantom(true);
		ear.setFixed(true);
		ear.setSize(new WWVector(0.2f, 0.1f, 0.7f));
		ear.setPosition(-0.3f, 0, 0.5f);
		addObject(ear);
		ear.setParent(avatarId);

		ear = new WWSphere();
		ear.setPhantom(true);
		ear.setFixed(true);
		ear.setSize(new WWVector(0.2f, 0.1f, 0.7f));
		ear.setPosition(0.3f, 0, 0.5f);
		addObject(ear);
		ear.setParent(avatarId);

		WWSimpleShape tail = new WWSphere();
		tail.setPhantom(true);
		tail.setFixed(true);
		tail.setSize(new WWVector(0.2f, 0.2f, 0.2f));
		tail.setPosition(0, 0.4f, -0.2f);
		addObject(tail);
		tail.setParent(avatarId);
	}

	public void makeLuckyHat(int avatarId) {
		WWColor hatColor = new WWColor(0, 0.5f, 0);

		WWSimpleShape rim = new WWCylinder();
		rim.setPhantom(true);
		rim.setFixed(true);
		rim.setSize(new WWVector(1.0f, 1.0f, 0.1f));
		rim.setPosition(0, 0, 0.4f);
		rim.setColor(WWObject.SIDE_ALL, hatColor);
		addObject(rim);
		rim.setParent(avatarId);

		WWSimpleShape bowl = new WWCylinder();
		bowl.setPhantom(true);
		bowl.setFixed(true);
		bowl.setSize(new WWVector(0.5f, 0.5f, 0.5f));
		bowl.setPosition(0, 0, 0.65f);
		bowl.setColor(WWObject.SIDE_ALL, hatColor);
		bowl.setTaperX(0.2f);
		bowl.setTaperY(0.2f);
		addObject(bowl);
		bowl.setParent(avatarId);
	}

	public void makeCupidAppendages(int avatarId) {
		WWSimpleShape wing1 = new WWSphere();
		wing1.setPhantom(true);
		wing1.setFixed(true);
		wing1.setSize(0.1f, 0.6f, 0.5f);
		wing1.setPosition(0.3f, 0.2f, 0.25f);
		wing1.setRotation(0, 0, 30);
		wing1.setTaperY(1.0f);
		wing1.setTaperX(1.0f);
		wing1.setShearY(0.5f);
		addObject(wing1);
		wing1.setParent(avatarId);

		WWSimpleShape wing2 = new WWSphere();
		wing2.setPhantom(true);
		wing2.setFixed(true);
		wing2.setSize(0.1f, 0.6f, 0.5f);
		wing2.setPosition(-0.3f, 0.2f, 0.25f);
		wing2.setRotation(0, 0, -30);
		wing2.setTaperY(1.0f);
		wing2.setTaperX(1.0f);
		wing2.setShearY(0.5f);
		addObject(wing2);
		wing2.setParent(avatarId);

		WWSimpleShape bow = new WWCylinder();
		bow.setPhantom(true);
		bow.setFixed(true);
		bow.setColor(WWObject.SIDE_ALL, new WWColor(1, 0.7f, 0.5f));
		bow.setSize(0.5f, 0.25f, 0.05f);
		bow.setPosition(0.45f, -0.2f, 0);
		bow.setRotation(0, 90, 0);
		bow.setCutoutStart(0.25f);
		bow.setCutoutEnd(0.75f);
		bow.setHollow(0.9f);
		addObject(bow);
		bow.setParent(avatarId);

		WWSimpleShape arrow = new WWCylinder();
		arrow.setPhantom(true);
		arrow.setFixed(true);
		arrow.setColor(WWObject.SIDE_ALL, new WWColor(0.7f, 0.5f, 0.3f));
		arrow.setSize(0.02f, 0.02f, 0.5f);
		arrow.setPosition(0.47f, -0.2f, 0);
		arrow.setRotation(90, 0, 0);
		addObject(arrow);
		arrow.setParent(avatarId);

		WWSimpleShape arrowHead = new WWCylinder();
		arrowHead.setPhantom(true);
		arrowHead.setFixed(true);
		arrowHead.setColor(WWObject.SIDE_ALL, new WWColor(1, 0, 0));
		arrowHead.setSize(0.07f, 0.07f, 0.1f);
		arrowHead.setPosition(0.47f, -0.45f, 0);
		arrowHead.setRotation(-90, 0, 0);
		arrowHead.setTaperX(1);
		arrowHead.setTaperY(1);
		addObject(arrowHead);
		arrowHead.setParent(avatarId);
	}

	public void makeCasper(int avatarId) {
		WWSimpleShape casper = new WWSphere();
		casper.setPhantom(true);
		casper.setFixed(true);
		casper.setSize(new WWVector(1, 1, 1));
		casper.setTextureURL(WWObject.SIDE_ALL, "casper_skin");
		casper.setTransparency(WWObject.SIDE_ALL, 0.5f);
		casper.setTaperX(0.25f);
		casper.setTaperY(0.25f);
		casper.setPosition(0, 0, 0.5f);
		addObject(casper);
		casper.setParent(avatarId);
	}

	public void makeTurkeyParts(int avatarId) {
		WWSimpleShape beak = new WWBox();
		beak.setPhantom(true);
		beak.setFixed(true);
		beak.setColor(WWObject.SIDE_ALL, new WWColor(1, 1, 0));
		beak.setSize(0.5f, 0.5f, 0.4f);
		beak.setTaperX(1);
		beak.setTaperY(1);
		beak.setRotation(-90, 45, 0);
		beak.setPosition(0, -0.5f, -0.05f);
		addObject(beak);
		beak.setParent(avatarId);

		WWSimpleShape gobble = new WWSphere();
		gobble.setPhantom(true);
		gobble.setFixed(true);
		gobble.setColor(WWObject.SIDE_ALL, new WWColor(1, 0, 0));
		gobble.setSize(0.2f, 0.5f, 0.5f);
		gobble.setPosition(0, -0.25f, -0.25f);
		addObject(gobble);
		gobble.setParent(avatarId);

		WWColor featherColor = new WWColor(0.5f, 0.3f, 0.2f);

		WWSimpleShape feather = new WWCylinder();
		feather.setPhantom(true);
		feather.setFixed(true);
		feather.setColor(WWObject.SIDE_ALL, featherColor);
		feather.setSize(0.5f, 1, 0.01f);
		feather.setPosition(-0.15f, 0.4f, 0.25f);
		feather.setRotation(90, 15, 0);
		addObject(feather);
		feather.setParent(avatarId);

		feather = new WWCylinder();
		feather.setPhantom(true);
		feather.setFixed(true);
		feather.setColor(WWObject.SIDE_ALL, featherColor);
		feather.setSize(0.5f, 1, 0.01f);
		feather.setPosition(0.15f, 0.4f, 0.25f);
		feather.setRotation(90, -15, 0);
		addObject(feather);
		feather.setParent(avatarId);

		feather = new WWCylinder();
		feather.setPhantom(true);
		feather.setFixed(true);
		feather.setColor(WWObject.SIDE_ALL, featherColor);
		feather.setSize(0.5f, 1, 0.01f);
		feather.setPosition(-0.4f, 0.4f, 0.1f);
		feather.setRotation(90, 45, 0);
		addObject(feather);
		feather.setParent(avatarId);

		feather = new WWCylinder();
		feather.setPhantom(true);
		feather.setFixed(true);
		feather.setColor(WWObject.SIDE_ALL, featherColor);
		feather.setSize(0.5f, 1, 0.01f);
		feather.setPosition(0.4f, 0.4f, 0.1f);
		feather.setRotation(90, -45, 0);
		addObject(feather);
		feather.setParent(avatarId);
	}

	public void makeSantaHat(int avatarId) {
		WWSimpleShape hat = new WWCylinder();
		hat.setPhantom(true);
		hat.setFixed(true);
		hat.setColor(WWObject.SIDE_ALL, new WWColor(1, 0, 0));
		hat.setSize(0.5f, 0.5f, 0.5f);
		hat.setPosition(0, 0, 0.75f);
		hat.setTaperX(1);
		hat.setTaperY(1);
		addObject(hat);
		hat.setParent(avatarId);

		WWSimpleShape brim = new WWCylinder();
		brim.setPhantom(true);
		brim.setFixed(true);
		brim.setSize(0.6f, 0.6f, 0.2f);
		brim.setPosition(0, 0, 0.4f);
		addObject(brim);
		brim.setParent(avatarId);

		WWSimpleShape ball = new WWSphere();
		ball.setPhantom(true);
		ball.setFixed(true);
		ball.setSize(0.2f, 0.2f, 0.2f);
		ball.setPosition(0, 0, 0.9f);
		addObject(ball);
		ball.setParent(avatarId);
	}

	public PhysicsThread makePhysicsThread() {
		return new OldPhysicsThread(this, 10);
	}

	@Override
	public boolean dampenCamera() {
		return AndroidClientModel.getClientModel().getViewpoint() != 1;
	}

}
