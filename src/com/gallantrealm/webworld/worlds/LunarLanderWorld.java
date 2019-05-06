package com.gallantrealm.webworld.worlds;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.android.StartWorldActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.NewPhysicsThread;
import com.gallantrealm.myworld.model.PhysicsThread;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;
import android.content.Intent;

public class LunarLanderWorld extends BaseEggWorld {
	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	WWSimpleShape avatar;
	WWSimpleShape exhaust;
	boolean landing = false;
	WWSphere moon;
	int level;

	public LunarLanderWorld(String saveWorldFileName, String avatarName) {
		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = false;

		setName("Lunar Lander");
		setSkyColor(new WWColor(0x000000));
		setRenderingThreshold(100000);

		level = Math.max(1, AndroidClientModel.getClientModel().getScore(11));
		setLevel(level);

		WWSimpleShape space = new WWSphere();
		space.setPhantom(true);
		space.setMonolithic(true);
		space.setSize(15000, 15000, 15000);
		space.setHollow(0.999f);
		space.setTextureURL(SIDE_ALL, "space");
		space.setTextureScaleX(SIDE_ALL, 0.1f);
		space.setTextureScaleY(SIDE_ALL, 0.1f);
		space.setFullBright(SIDE_ALL, true);
		addObject(space);

		moon = new WWSphere();
		moon.setCircleVertices(64);
		moon.setSize(25 * level * level, 25 * level * level, 25 * level * level);
		moon.setTextureURL(SIDE_ALL, "moon");
		moon.setPosition(0, 0, -12.5f * level * level);
		moon.setRotation(90, 0, 0);
		addObject(moon);

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		avatar.setPhysical(false);
		avatar.addBehavior(new AvatarBehavior());
		avatar.freedomRotateX = false;
		avatar.freedomRotateY = true;
		avatar.freedomRotateZ = true;
		avatar.freedomMoveX = true;
		avatar.freedomMoveY = true;
		avatar.freedomMoveZ = true;
		int avatarid = addObject(avatar);
		user.setAvatarId(avatarid);

		// space ship
		WWSimpleShape ship = new WWCylinder();
		ship.setColor(SIDE_ALL, new WWColor(1, 1, 0.9f));
		ship.setCircleVertices(8);
		ship.setPhantom(true);
		ship.setMonolithic(true);
		ship.setSize(3, 3, 0.8f);
		ship.setPosition(0, 0, -0.5f);
		ship.setParent(avatar);
		addObject(ship);

		exhaust = new WWSphere();
		exhaust.setPhantom(true);
		exhaust.setMonolithic(true);
		exhaust.setSize(2, 2, 4);
		exhaust.setColor(SIDE_ALL, new WWColor(1, 1, 0.25f));
		exhaust.setFullBright(SIDE_ALL, true);
		exhaust.setTransparency(SIDE_ALL, 1);
		exhaust.setTaperX(0.8f);
		exhaust.setTaperY(0.8f);
		exhaust.setRotation(180, 0, 0);
		exhaust.setPosition(0, 0, -2);
		addObject(exhaust);
		exhaust.setParent(ship);
		exhaust.addBehavior(new ExhaustAnimation());

		WWColor legColor = new WWColor(1, 0.25f, 0.25f);

		WWCylinder leg = new WWCylinder();
		leg.setColor(SIDE_ALL, legColor);
		leg.setPhantom(true);
		leg.setMonolithic(true);
		leg.setSize(0.2f, 0.2f, 2);
		leg.setRotation(-45, 0, -45);
		leg.setPosition(-1.2f, 1.2f, -0.5f);
		addObject(leg);
		leg.setParent(ship);
		WWCylinder foot = new WWCylinder();
		foot.setColor(SIDE_ALL, legColor);
		foot.setPhantom(true);
		foot.setMonolithic(true);
		foot.setSize(0.6f, 0.6f, 0.1f);
		foot.setPosition(-1.7f, 1.7f, -1.2f);
		addObject(foot);
		foot.setParent(ship);

		leg = new WWCylinder();
		leg.setColor(SIDE_ALL, legColor);
		leg.setPhantom(true);
		leg.setMonolithic(true);
		leg.setSize(0.2f, 0.2f, 2);
		leg.setRotation(-45, 0, 45);
		leg.setPosition(1.2f, 1.2f, -0.5f);
		addObject(leg);
		leg.setParent(ship);
		foot = new WWCylinder();
		foot.setColor(SIDE_ALL, legColor);
		foot.setPhantom(true);
		foot.setMonolithic(true);
		foot.setSize(0.6f, 0.6f, 0.1f);
		foot.setPosition(1.7f, 1.7f, -1.2f);
		addObject(foot);
		foot.setParent(ship);

		leg = new WWCylinder();
		leg.setColor(SIDE_ALL, legColor);
		leg.setPhantom(true);
		leg.setMonolithic(true);
		leg.setSize(0.2f, 0.2f, 2);
		leg.setRotation(-45, 0, 135);
		leg.setPosition(1.2f, -1.2f, -0.5f);
		addObject(leg);
		leg.setParent(ship);
		foot = new WWCylinder();
		foot.setColor(SIDE_ALL, legColor);
		foot.setPhantom(true);
		foot.setMonolithic(true);
		foot.setSize(0.6f, 0.6f, 0.1f);
		foot.setPosition(1.7f, -1.7f, -1.2f);
		addObject(foot);
		foot.setParent(ship);

		leg = new WWCylinder();
		leg.setColor(SIDE_ALL, legColor);
		leg.setPhantom(true);
		leg.setMonolithic(true);
		leg.setSize(0.2f, 0.2f, 2);
		leg.setRotation(-45, 0, 225);
		leg.setPosition(-1.2f, -1.2f, -0.5f);
		addObject(leg);
		leg.setParent(ship);
		foot = new WWCylinder();
		foot.setColor(SIDE_ALL, legColor);
		foot.setPhantom(true);
		foot.setMonolithic(true);
		foot.setSize(0.6f, 0.6f, 0.1f);
		foot.setPosition(-1.7f, -1.7f, -1.2f);
		addObject(foot);
		foot.setParent(ship);

		WWSimpleShape dome = new WWSphere();
		dome.setPhantom(true);
		dome.setMonolithic(true);
		dome.setSize(2, 2, 1.5f);
		dome.setPosition(0, 0, 0);
		dome.setTransparency(SIDE_ALL, 0.5f);
		dome.setParent(avatar);
		addObject(dome);

		worldActions = new WWAction[] { new PauseAction() };
		reset();

	}

	class LandAction extends WWAction {
		boolean instructionsShown;

		@Override
		public String getName() {
			return "Land";
		}

		@Override
		public void start() {
			hideBannerAds();
			AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint()); // to force view from viewpoint
			landing = true;
			//AndroidClientModel.getClientModel().forceAvatar(50, 0, 0, 0, 0, 0);
			ThrustAction thrustAction = new ThrustAction();
			setAvatarActions(new WWAction[] { thrustAction, null, null, null, null, null, null, thrustAction });
			avatar.setPhysical(true);
			avatar.setDensity(1);
			playSong("lunar_music", 1);
		}

		@Override
		public void stop() {
		}
	}

	boolean thrusting;
	int thrustingSoundId;

	class ThrustAction extends WWAction {

		@Override
		public String getName() {
			return "Thrust";
		}

		@Override
		public void start() {
			thrusting = true;
			thrustingSoundId = LunarLanderWorld.this.startSound("thruster", 1, 1);
			exhaust.setTransparency(SIDE_ALL, 0);
		}

		@Override
		public void stop() {
			thrusting = false;
			LunarLanderWorld.this.stopSound(thrustingSoundId);
			exhaust.setTransparency(SIDE_ALL, 1);
		}
	}

	public void reset() {
		landing = false;
		thrusting = false;
		AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0, 0);
		avatar.setVelocity(new WWVector(0, 0, 0));
		avatar.setPosition(0, 0, 12.5f * level * level);
		avatar.setDensity(0);
		setAvatarActions(new WWAction[] { new LandAction() });
		LunarLanderWorld.this.setStatus("Distance: " + (int) (avatar.getPosition().length()));
		AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
	}

	@Override
	public WWVector getGravityForce(float x, float y, float z) {
		z = z + 12.5f * level * level;
		float gd = (float) Math.sqrt(x * x + y * y + z * z);
		float gf = -25.0f * level * level * level / (1.0f + gd);
		float forcex = (x / gd) * gf;
		float forcey = (y / gd) * gf;
		float forcez = (z / gd) * gf;
		return new WWVector(forcex, forcey, forcez);
	}

	@Override
	public boolean dampenCamera() {
		return false;
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	@Override
	public boolean usesController() {
		return true;
	}

	float controllerX;
	float controllerY;

	@Override
	public boolean controller(float deltaX, float deltaY) {
		controllerX = deltaX;
		controllerY = deltaY;
		return true;
	}

	class ExhaustAnimation extends WWAnimation {

		@Override
		public void getAnimatedPosition(WWObject object, WWVector position, long time) {
			position.add(0, 0, FastMath.random(0, -0.5f));
		}

		@Override
		public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
		}

	}

	float impactVelocity;

	class AvatarBehavior extends WWBehavior {

		public AvatarBehavior() {
			setTimer(100);
		}

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (landing && nearObject == moon) {
				landing = false;
				thrusting = false;
				LunarLanderWorld.this.stopSound(thrustingSoundId);
				exhaust.setTransparency(SIDE_ALL, 1);
				avatar.setPhysical(false);
				avatar.setThrust(new WWVector(0, 0, 0));
				avatar.setVelocity(new WWVector(0, 0, 0));
				stopPlayingSong();
				int rc;
				if (impactVelocity > 10) {
					AndroidClientModel.getClientModel().vibrate(250);
					playSound("loosingSound", 1);
					rc = AndroidClientModel.getClientModel().alert("You crashed!", new String[] { "Play Again", "Quit" });
					if (rc == 0) {
						showBannerAds();
						reset();
					}
					if (rc == 1) {
						AndroidClientModel.getClientModel().disconnect();
					}
				} else {
					avatar.setPosition(avatar.getPosition().add(0, 0, 1));
					playSound("winningSound", 0.125f);
					if (clientModel.isPlayMusic()) {
						playSound("winningSound2", 0.1f);
					}
					rc = AndroidClientModel.getClientModel().alert("Hurrah! You landed safely!", new String[] { "Next Level", "Redo Level", "Quit" });
					if (rc == 0) {
						AndroidClientModel clientModel = AndroidClientModel.getClientModel();
						clientModel.setScore(11, level + 1);
						clientModel.disconnect();
						clientModel.setWorldName("com.gallantrealm.eggworld.worlds.LunarLanderWorld");
						Intent intent = new Intent(clientModel.getContext(), StartWorldActivity.class);
						clientModel.getContext().startActivity(intent);
					}
					if (rc == 1) {
						showBannerAds();
						reset();
					}
					if (rc == 2) {
						AndroidClientModel.getClientModel().disconnect();
					}
				}
			}
			return true;
		}

		@Override
		public boolean timerEvent() {
			clientModel.setCameraDistance(15);
			clientModel.setCameraTilt(30.0f);
			clientModel.setCameraPan(180.0f);

			if (landing) {
				float x = controllerX;
				float y = controllerY;
//				avatar.setTorque(new WWVector(-y, x, 0));
//				avatar.setTorqueVelocity(new WWVector(-y, x, 0));
				if (thrusting) {
					avatar.setThrust(new WWVector(0, 0, 5f * level));
					avatar.setThrustVelocity(new WWVector(0, 0, 100));
				} else {
					avatar.setThrust(new WWVector(0, 0, 0));
				}

				impactVelocity = avatar.getVelocity().length();

				LunarLanderWorld.this.setStatus("Distance: " + (int) (avatar.getPosition().length()));
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
			} else {
				AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0, 0);
				LunarLanderWorld.this.setStatus("");
			}
			setTimer(100);
			return true;
		}

	}

	@Override
	public boolean allowCameraPositioning() {
		return false;
	}

	@Override
	public PhysicsThread makePhysicsThread() {
		return new NewPhysicsThread(this, 10, 0);
	}

}
