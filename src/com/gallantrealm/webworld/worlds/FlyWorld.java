package com.gallantrealm.webworld.worlds;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.model.OldPhysicsThread;
import com.gallantrealm.myworld.model.PhysicsThread;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWMesh;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWTorus;
import com.gallantrealm.myworld.model.WWTranslucency;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

public class FlyWorld extends BaseWebWorld {
	ClientModel clientModel = AndroidClientModel.getClientModel();

	FlyBehavior flyBehavior;
	WWSimpleShape air;
	WWSimpleShape sky;
	WWSimpleShape avatar1;
	WWSimpleShape propellor;
	WWSimpleShape airport;
	boolean clearedAirport;
	boolean flying;

	public FlyWorld(String saveWorldFileName, String avatarName) {
		AndroidClientModel.getClientModel().behindDistance = 6;
		AndroidClientModel.getClientModel().behindTilt = 15;

		// The basic world (like earth)
		setName("Fly");
		setGravity(9.8f); // earth gravity
		setFogDensity(0.05f);
		long time = getWorldTime();
		setCreateTime(time);
		setLastModifyTime(time);

		// the ground
		WWMesh ground = new WWMesh(); // for the ground
		ground.setName("ground");
		ground.setImpactSound("carCrash");
		ground.setSlidingSound("movingGrass");
		ground.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x80F080)); // greener
		ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404000)); // sides brown
		ground.setSize(new WWVector(10000, 10000, 2000));
		ground.setPosition(new WWVector(0, 0, -10));
		int meshSize = 100;
		ground.setMeshSize(meshSize, meshSize);
		// - roughen the general terrain
		for (int i = 0; i <= meshSize; i++) {
			for (int j = 0; j <= meshSize; j++) {
				ground.setMeshPoint(i, j, 0.5f + FastMath.random() * 0.01f);
			}
		}
		// - add a few peaks
		for (int i = 0; i < 25; i++) {
			int x = (int) (meshSize * FastMath.random());
			int y = (int) (meshSize * FastMath.random());
			if (x >= 45 && x <= 55 && y >= 45 && y <= 55) {
				continue;
			}
			float z = FastMath.random() * 0.0005f;
			int baseSize = (int) (FastMath.random() * 30);
			for (int cx = x - baseSize; cx < x + baseSize; cx++) {
				for (int cy = y - baseSize; cy < y + baseSize; cy++) {
					if (cx >= 0 && cx <= meshSize && cy >= 0 && cy <= meshSize) {
						float d = (float) Math.sqrt((cx - x) * (cx - x) + (cy - y) * (cy - y));
						if (d < baseSize) {
							ground.setMeshPoint(cx, cy, ground.getMeshPoint(cx, cy) + (baseSize - d) * (baseSize - d) * z);
						}
					}
				}
			}
		}
		// - add a few gorges
		for (int i = 0; i < 100; i++) {
			int x = (int) (meshSize * FastMath.random());
			int y = (int) (meshSize * FastMath.random());
			float z = -FastMath.random() * 0.00001f;
			int baseSize = (int) (FastMath.random() * 20) + 5;
			for (int cx = x - baseSize; cx < x + baseSize; cx++) {
				for (int cy = y - baseSize; cy < y + baseSize; cy++) {
					if (cx >= 0 && cx <= meshSize && cy >= 0 && cy <= meshSize) {
						float d = (float) Math.sqrt((cx - x) * (cx - x) + (cy - y) * (cy - y));
						if (d < baseSize) {
							ground.setMeshPoint(cx, cy, ground.getMeshPoint(cx, cy) + baseSize * (baseSize - d) * z);
						}
					}
				}
			}
		}

		ground.setTextureURL(WWSimpleShape.SIDE_TOP, "land");
		ground.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.1f);
		ground.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.1f);
		addObject(ground);

		// the water
		WWTranslucency water = new WWTranslucency();
		water.setName("water");
		water.setPenetratable(true);
		water.setInsideLayerDensity(0.25f);
		water.setPosition(new WWVector(0, 0, -25));
		water.setSize(new WWVector(10000, 10000, 50));
		water.setSolid(false);
		water.setDensity(1);
		water.setFriction(0.1f);
		water.setImpactSound("water");
		water.setSlidingSound("movingWater");
		water.setInsideColor(0x202040);
		water.setInsideTransparency(0.7f);
		water.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x8080F0));
		water.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0xA0A0F0));
		water.setTransparency(WWSimpleShape.SIDE_TOP, 0.1f);
		water.setTextureURL(WWSimpleShape.SIDE_TOP, "water");
		water.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.001f);
		water.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.001f);
		water.setTextureVelocityX(WWSimpleShape.SIDE_TOP, 0.0001f);
		water.setColor(WWSimpleShape.SIDE_INSIDE_TOP, new WWColor(0xF0F0F0));
		water.setTransparency(WWSimpleShape.SIDE_INSIDE_TOP, 0.50f);
		water.setTextureURL(WWSimpleShape.SIDE_INSIDE_TOP, "water");
		water.setTextureScaleX(WWSimpleShape.SIDE_INSIDE_TOP, 0.001f);
		water.setTextureScaleY(WWSimpleShape.SIDE_INSIDE_TOP, 0.001f);
		water.setTextureVelocityX(WWSimpleShape.SIDE_INSIDE_TOP, 0.0001f);
		water.setColor(WWSimpleShape.SIDE_CUTOUT1, new WWColor(0x6060C0));
		water.setTransparency(WWSimpleShape.SIDE_CUTOUT1, 0.35f);
		addObject(water);

		// the sky
		sky = new WWSphere();
		sky.setFriction(0);
		sky.setPhantom(true);
		sky.setName("sky");
		sky.setPenetratable(true);
		sky.setPosition(new WWVector(0, 0, -800));
		sky.setSize(new WWVector(5000, 15000, 15000));
		sky.setCutoutStart(0.5f); // half dome
		sky.setSolid(false); // for now.. otherwise physical objects pushed out of world
		sky.setFriction(0); // for now.. otherwise physical objects slowed down in hollowed area
		sky.setRotation(new WWVector(0, 90, 0));
		sky.setHollow(0.99f);
		sky.setTextureURL(WWSimpleShape.SIDE_INSIDE1, "sky");
		sky.setTextureScaleX(WWSimpleShape.SIDE_INSIDE1, 0.25f);
		sky.setTextureScaleY(WWSimpleShape.SIDE_INSIDE1, 0.25f);
		// sky.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xc0c0f0));
		sky.setTextureVelocityY(WWSimpleShape.SIDE_INSIDE1, 0.0005f);
		// for future
		// sky.setTextureURL(Side.INSIDE1, "http://www.moonglow.net/latest");
		// sky.setTextureRefreshInterval(Side.INSIDE1,"60000");
		// TODO fog color should match average color of sky
		sky.setFullBright(WWSimpleShape.SIDE_INSIDE1, true); // bright sky
		sky.setColor(WWSimpleShape.SIDE_INSIDE1, new WWColor(0xd0d0ff));
		sky.setTextureURL(WWSimpleShape.SIDE_SIDE1, "sky");
		sky.setTransparency(WWSimpleShape.SIDE_SIDE1, 0.25f);
		addObject(sky);

		// the airport
		airport = new WWBox();
		airport.setTextureURL(SIDE_ALL, "track");
		airport.setTextureRotation(SIDE_ALL, 90);
		airport.setSize(new WWVector(50, 300, 1));
		airport.setPosition(new WWVector(0, 100, 9));
		airport.setFriction(0);
		airport.setImpactSound("carSlide");
		addObject(airport);

		// smooth land under airport
		int center = meshSize / 2;
		for (int i = center - 2; i <= center + 2; i++) {
			for (int j = center - 2; j <= center + 12; j++) {
				ground.setMeshPoint(i, j, 9.5f / 1000f + .5f);
			}
		}

		// first user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);

		avatar1 = makeAvatar(avatarName);
		avatar1.setFreedomMoveZ(false);
		avatar1.setFreedomMoveY(true);
		avatar1.setFreedomMoveX(false);
		avatar1.setPosition(new WWVector(0, 0, 10));
		avatar1.setRotation(new WWVector(0, 0, 180));
		int avatar1id = avatar1.getId();
		user.setAvatarId(avatar1id);

		flyBehavior = new FlyBehavior();
		avatar1.addBehavior(flyBehavior);

		// plane
		WWSimpleShape cockpit = new WWBox();
		cockpit.setMonolithic(true);
		cockpit.setPhantom(true);
		cockpit.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		cockpit.setShininess(SIDE_ALL, 0.25f);
		cockpit.setSize(new WWVector(1.0f, 1.0f, 0.5f));
		cockpit.setPosition(new WWVector(0, 0, -0.25f));
		cockpit.setSolid(false);
		cockpit.setParent(avatar1id);
		addObject(cockpit);

		WWSimpleShape engine = new WWBox();
		engine.setMonolithic(true);
		engine.setPhantom(true);
		engine.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		engine.setShininess(SIDE_ALL, 0.25f);
		engine.setSize(new WWVector(1.0f, 1.0f, 0.5f));
		engine.setTaperX(0.5f);
		engine.setTaperY(0.5f);
		engine.setRotation(new WWVector(-90, 0, 0));
		engine.setPosition(new WWVector(0, -0.75f, 0.25f));
		engine.setSolid(false);
		engine.setParent(cockpit.getId());
		addObject(engine);

		propellor = new WWTorus();
		propellor.setMonolithic(true);
		propellor.setPhantom(true);
		propellor.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x402020));
		propellor.setShininess(SIDE_ALL, 0.25f);
		propellor.setSize(new WWVector(0.15f, 0.01f, 1.5f));
		propellor.setTwist(0.1f);
		propellor.setRotation(new WWVector(0, 90, 0));
		propellor.setPosition(new WWVector(0, -1.1f, 0.25f));
		propellor.setSolid(false);
		propellor.setParent(cockpit.getId());
		propellor.setGluedToParent(false);
		addObject(propellor);

		WWSimpleShape tail = new WWBox();
		tail.setMonolithic(true);
		tail.setPhantom(true);
		tail.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		tail.setShininess(SIDE_ALL, 0.25f);
		tail.setSize(new WWVector(1.0f, 1.0f, 3.0f));
		tail.setTaperX(0.75f);
		tail.setTaperY(0.75f);
		tail.setRotation(new WWVector(90, 0, 0));
		tail.setPosition(new WWVector(0, 2.0f, 0.25f));
		tail.setSolid(false);
		tail.setParent(cockpit.getId());
		addObject(tail);

		WWSimpleShape wing = new WWCylinder();
		wing.setMonolithic(true);
		wing.setPhantom(true);
		wing.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		wing.setShininess(SIDE_ALL, 0.25f);
		wing.setSize(new WWVector(0.25f, 1.0f, 5f));
		wing.setRotation(new WWVector(0, 90, 0));
		wing.setPosition(new WWVector(0, 0f, 0f));
		wing.setParent(cockpit.getId());
		addObject(wing);

		WWSimpleShape backwing = new WWCylinder();
		backwing.setMonolithic(true);
		backwing.setPhantom(true);
		backwing.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		backwing.setShininess(SIDE_ALL, 0.25f);
		backwing.setSize(new WWVector(0.125f, 0.5f, 1.5f));
		backwing.setRotation(new WWVector(0, 90, 0));
		backwing.setPosition(new WWVector(0, 3.5f, 0.25f));
		backwing.setParent(cockpit.getId());
		addObject(backwing);

		WWSimpleShape rudder = new WWCylinder();
		rudder.setMonolithic(true);
		rudder.setPhantom(true);
		rudder.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		rudder.setShininess(SIDE_ALL, 0.25f);
		rudder.setSize(new WWVector(0.125f, 0.5f, 0.75f));
		rudder.setPosition(new WWVector(0, 3.5f, 0.5f));
		rudder.setParent(cockpit.getId());
		addObject(rudder);

		propellor.setAMomentum(new WWVector(0, -500, 0));

		FlyWorld.this.avatar1.setAMomentum(new WWVector(0, 0, 0));
		thrust = 0;
		torque = 0;
		lift = 0;
		clientModel.forceAvatar(0, 0, 0, 0, 0);

		setAvatarActions(new WWAction[] { new FlyAction() });

	}

	class FlyAction extends WWAction {
		@Override
		public String getName() {
			return "Fly";
		}

		@Override
		public void start() {
			flying = true;
			propellor.setAMomentum(new WWVector(0, -1000, 0));
			setAvatarActions(new WWAction[0]);
			AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint()); // to force view from viewpoint
			thrust = 10;
			lift = 10;
			AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, 0, 0);
			flyBehavior.setTimer(100);
			hideBannerAds();
		}
	}

	class StallAction extends WWAction {
		@Override
		public String getName() {
			return "Stall";
		}

		@Override
		public void start() {
			flying = false;
			thrust = 0;
			lift = 0;
			AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, 0, 0);
			propellor.setAMomentum(new WWVector(0, -0, 0));
			flyBehavior.setTimer(0);
			avatar1.setSound("car", 0, 0);
		}
	}

	public class FlyBehavior extends WWBehavior {

		@Override
		public boolean timerEvent() {
			if (flying) {
				thrust = 25;
				AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, tilt, lean);
				long time = FlyWorld.this.getWorldTime();
				WWVector rotation = FlyWorld.this.avatar1.getRotation(time);
				WWVector amomentum = FlyWorld.this.avatar1.getAMomentum();
				amomentum.z = (float) (100.0 * Math.sin(Math.toRadians(rotation.y)));
				FlyWorld.this.avatar1.setAMomentum(amomentum);
				if (avatar1.getPosition(time).y > 100 || avatar1.getPosition(time).y < -50) {
					clearedAirport = true;
				}
				float z = FastMath.abs(FlyWorld.this.avatar1.getVelocity().normalize().z);
				float v = (0.6f + z) * Math.min(1f, FlyWorld.this.avatar1.getVelocity().length() / 5f);
				FlyWorld.this.avatar1.setSound("car", 0.1f, v); // sounds like a plane too
			} else {
				FlyWorld.this.avatar1.setSound("car", 0.1f, 0.0f); // sounds like a plane too
			}
			setTimer(100);
			return true; // handled it
		}

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (!flying) {
				return true;
			}
			ClientModel clientModel = AndroidClientModel.getClientModel();
			if (nearObject != sky && nearObject != air) {
				if (nearObject == airport) {
					if (clearedAirport) {
						stopMovement();
						int rc = clientModel.alert("You landed!!", new String[] { "Restart", "Quit" });
						if (rc == 0) {
							showBannerAds();
							synchronized (FlyWorld.this) {
								avatar1.setVelocity(new WWVector(0, 0, 0));
								avatar1.setPosition(new WWVector(0, 0, 10));
								avatar1.setRotation(new WWVector(0, 0, 180));
								propellor.setAMomentum(new WWVector(0, -500, 0));
								setAvatarActions(new WWAction[] { new FlyAction() });
							}
							avatar1.setSound("car", 1, 0.1f);
						}
						if (rc == 1) {
							synchronized (FlyWorld.this) {
								avatar1.setVelocity(new WWVector(0, 0, 0));
								avatar1.setPosition(new WWVector(0, 0, 10));
								avatar1.setRotation(new WWVector(0, 0, 180));
							}
							clientModel.disconnect();
						}
						AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0);
						clearedAirport = false;
					}

				} else {
					stopMovement();
					AndroidClientModel.getClientModel().vibrate(250);
					int rc = clientModel.alert("You crashed!!", new String[] { "Airport", "Quit" });
					if (rc == 0) {
						showBannerAds();
						synchronized (FlyWorld.this) {
							avatar1.setVelocity(new WWVector(0, 0, 0));
							avatar1.setPosition(new WWVector(0, 0, 10));
							avatar1.setRotation(new WWVector(0, 0, 180));
							propellor.setAMomentum(new WWVector(0, -500, 0));
							setAvatarActions(new WWAction[] { new FlyAction() });
						}
						avatar1.setSound("car", 1, 0.1f);
					}
					if (rc == 1) {
						synchronized (FlyWorld.this) {
							avatar1.setVelocity(new WWVector(0, 0, 0));
							avatar1.setPosition(new WWVector(0, 0, 10));
							avatar1.setRotation(new WWVector(0, 0, 180));
						}
						clientModel.disconnect();
					}
					AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0);
					clearedAirport = false;
				}
			}
			return true;
		}

		public void stopMovement() {
			flying = false;
			ClientModel clientModel = AndroidClientModel.getClientModel();
			setTimer(0);
			FlyWorld.this.avatar1.setAMomentum(new WWVector(0, 0, 0));
			thrust = 0;
			torque = 0;
			lift = 0;
			tilt = 0;
			lean = 0;
			clientModel.forceAvatar(0, 0, 0, 0, 0);
			propellor.setAMomentum(new WWVector(0, -0, 0));
			FlyWorld.this.avatar1.setSound("car", 0, 0);
		}

	}

	@Override
	public int getSensorXType() {
		return WWWorld.MOVE_TYPE_LEAN;
	}

	@Override
	public int getSensorYType() {
		return WWWorld.MOVE_TYPE_TILT;
	}

	@Override
	public int getMoveXType() {
		return WWWorld.MOVE_TYPE_LEAN;
	}

	@Override
	public int getMoveYType() {
		return WWWorld.MOVE_TYPE_TILT;
	}

	@Override
	public float[] getMoveYTilt() {
		return new float[] { 100, 75, 50, 25, 0, -25, -50, -75, -100 };
	}

	@Override
	public float[] getMoveXLean() {
		return new float[] { -90, -60, -30, -15, 0, 15, 30, 60, 90 };
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	@Override
	public boolean controller(float x, float y) {
		lean = x * 2;
		tilt = -y * 2;
		return true;
	}

	@Override
	public PhysicsThread makePhysicsThread() {
		return new OldPhysicsThread(this, 10);
	}

	@Override
	public boolean dampenCamera() {
		return false;
	}

}
