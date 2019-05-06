package com.gallantrealm.webworld.worlds;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.model.NewPhysicsThread;
import com.gallantrealm.myworld.model.PhysicsThread;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWMesh;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWTorus;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

public class SkateWorld extends BaseEggWorld {

	WWSimpleShape avatar;
	boolean rolling;

	public SkateWorld(String saveWorldFileName, String avatarName) {

		setName("Skate");
		setGravity(20);
		AndroidClientModel.getClientModel().setCameraDampRate(2);
// TODO		AndroidClientModel.getClientModel().dampCameraTranslations = false;

		// the ground
//		WWSimpleShape ground = new WWCylinder(); // for the ground
//		ground.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x008000)); // green, like grass
//		ground.setColor(WWSimpleShape.SIDE_BOTTOM, new WWColor(0x008000)); // green, like grass
//		ground.setColor(WWSimpleShape.SIDE_SIDE1, new WWColor(0x008000)); // green, like grass
//		ground.setPosition(new WWVector(0, 0, -50.5f));
//		ground.setSize(new WWVector(1000, 1000, 100));
//		ground.setFriction(0.1f);
//		addObject(ground);

		// the ice
		WWMesh rink = new WWMesh(100, 100, 50);
		rink.setMeshSize(200, 200);
		rink.setMonolithic(true);
		rink.setPosition(new WWVector(0, 0, 0));
		rink.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		rink.setTextureScaleX(WWSimpleShape.SIDE_ALL, 0.1f);
		rink.setTextureScaleY(WWSimpleShape.SIDE_ALL, 0.1f);
		rink.setFriction(0.001f);
		rink.setSlidingSound("skate");
		rink.addBehavior(new WallBehavior());
		addObject(rink);

		// walls
		WWSimpleShape wall = new WWBox();
		wall.setMonolithic(true);
		wall.setSize(5, 100, 25);
		wall.setPosition(50, 0, 0);
		wall.setTextureURL(SIDE_ALL, "brick");
		wall.setTextureScaleX(SIDE_ALL, 0.01f);
		wall.setTextureScaleY(SIDE_ALL, 0.1f);
		addObject(wall);

		wall = new WWBox();
		wall.setMonolithic(true);
		wall.setSize(5, 100, 25);
		wall.setPosition(-50, 0, 0);
		wall.setTextureURL(SIDE_ALL, "brick");
		wall.setTextureScaleX(SIDE_ALL, 0.01f);
		wall.setTextureScaleY(SIDE_ALL, 0.1f);
		addObject(wall);

		wall = new WWBox();
		wall.setMonolithic(true);
		wall.setSize(100, 5, 25);
		wall.setPosition(0, 50, 0);
		wall.setTextureURL(SIDE_ALL, "brick");
		wall.setTextureScaleX(SIDE_ALL, 0.01f);
		wall.setTextureScaleY(SIDE_ALL, 0.1f);
		addObject(wall);

		wall = new WWBox();
		wall.setMonolithic(true);
		wall.setSize(100, 5, 25);
		wall.setPosition(0, -50, 0);
		wall.setTextureURL(SIDE_ALL, "brick");
		wall.setTextureScaleX(SIDE_ALL, 0.01f);
		wall.setTextureScaleY(SIDE_ALL, 0.1f);
		addObject(wall);

		// carve bowls
		for (int i = 50; i < 200; i += 50) {
			for (int j = 50; j < 200; j += 50) {
				int bowlSize = (int) FastMath.random(25, 50);
				int bowlType = (int) FastMath.random(0, 1.9f);
				for (int x = -bowlSize; x <= bowlSize; x++) {
					for (int y = -bowlSize; y <= bowlSize; y++) {
						float d = (float) Math.sqrt(x * x + y * y);
						if (d <= bowlSize) {
							float depth;
							if (bowlType > 0) {
								depth = 0.1f * FastMath.cos(d * 360 / bowlSize * FastMath.TORADIAN);
							} else {
								depth = -0.1f * FastMath.cos(d * 180 / bowlSize * FastMath.TORADIAN);
							}
							rink.setMeshPoint(x + i, y + j, FastMath.min(0.5f + depth, rink.getMeshPoint(x + i, y + j)));
						}
					}
				}
			}
		}

		// draw the apparatus
//		for (int i = -5; i <= 5; i++) {
//			for (int j = -5; j <= 5; j++) {
//				int angle = (int) (Math.random() * 360);
//				int type = (int) (Math.random() * 5);
//				int size = ((int) (5 * Math.random())) * 15;
//				int x = 45 * i;
//				int y = 45 * j;
//				if (type < 1) {
//					drawRamp(x, y, angle, size);
//				} else if (type < 2) {
//					drawPipe(x, y, angle, size);
//				} else if (type < 3) {
//					drawBowl(x, y, size);
//				} else if (type < 4) {
//					drawRails(x, y, angle, size);
//				}
//			}
//		}

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		avatar.setPosition(new WWVector(1, 1, 10));
		avatar.setFreedomRotateX(false);
		avatar.setFreedomRotateY(false);
		avatar.setFreedomRotateZ(false);
		avatar.setFreedomMoveZ(true);
		avatar.setFreedomMoveY(true);
		// avatar1.setFreedomMoveX(false);
		avatar.setRotation(0, 0, 36000);
		int avatar1id = avatar.getId();
		user.setAvatarId(avatar1id);

		// the user's board
		WWSimpleShape board = new WWCylinder();
		board.setPhantom(true);
		board.setMonolithic(true);
		// board.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		board.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		board.setSize(new WWVector(0.5f, 2.0f, 0.25f));
		board.setPosition(new WWVector(0, 0, -0.45f));
		board.setSolid(false);
		board.setParent(avatar1id);
		addObject(board);

		// wheels
		WWCylinder wheel = new WWCylinder(0.2f, 0.2f, 0.1f);
		wheel.setPhantom(true);
		wheel.setMonolithic(true);
		wheel.setColor(SIDE_ALL, new WWColor(1, 0.7f, 0.5f));
		wheel.setRotation(0, 90, 0);
		wheel.setPosition(-0.25f, 0.7f, -0.2f);
		wheel.setParent(board);
		addObject(wheel);

		wheel = new WWCylinder(0.2f, 0.2f, 0.1f);
		wheel.setPhantom(true);
		wheel.setMonolithic(true);
		wheel.setColor(SIDE_ALL, new WWColor(1, 0.7f, 0.5f));
		wheel.setRotation(0, 90, 0);
		wheel.setPosition(0.25f, 0.7f, -0.2f);
		wheel.setParent(board);
		addObject(wheel);

		wheel = new WWCylinder(0.2f, 0.2f, 0.1f);
		wheel.setPhantom(true);
		wheel.setMonolithic(true);
		wheel.setColor(SIDE_ALL, new WWColor(1, 0.7f, 0.5f));
		wheel.setRotation(0, 90, 0);
		wheel.setPosition(-0.25f, -0.7f, -0.2f);
		wheel.setParent(board);
		addObject(wheel);

		wheel = new WWCylinder(0.2f, 0.2f, 0.1f);
		wheel.setPhantom(true);
		wheel.setMonolithic(true);
		wheel.setColor(SIDE_ALL, new WWColor(1, 0.7f, 0.5f));
		wheel.setRotation(0, 90, 0);
		wheel.setPosition(0.25f, -0.7f, -0.2f);
		wheel.setParent(board);
		addObject(wheel);

		WWBehavior skaterBehavior = new WWAnimation() {
			@Override
			public boolean timerEvent() {
				synchronized (SkateWorld.this) {
					if (avatar.getThrust().length() <= 0.1f && avatar.getTorque().length() <= 30) {
						// kick turn if current direction > 90 from board position
						WWVector v = avatar.getVelocity();
						float l = v.length();
						if (v.y != 0 && l > 0.05) {
							float theta = (float) Math.toDegrees(FastMath.atan2(-v.x, -v.y));
							if (FastMath.abs(theta) > 10) {
								WWVector avatarRotation = avatar.getRotation();
								float rz = (float) Math.floor((avatarRotation.z + 180) / 360) * 360;
								avatar.setRotation(avatarRotation.x, avatarRotation.y, theta + rz);
							}
						}
					} else {
						// Move in position of avatar
						WWVector v = avatar.getVelocity();
						float l = (float) Math.sqrt(v.x * v.x + v.y * v.y);
						if (l > 0.05) {
							float theta = avatar.getRotation(SkateWorld.this.getWorldTime()).z;
							float x = (float) -Math.sin(Math.toRadians(theta)) * l;
							float y = (float) -Math.cos(Math.toRadians(theta)) * l;
							avatar.setVelocity(new WWVector(x, y, v.z));
						}
					}
					setTimer(10);
				}
				return true;
			}

			@Override
			public void getAnimatedPosition(WWObject object, WWVector position, long time) {
				if (object == avatar) {
					position.z += 0.4f;
				}
			}

			@Override
			public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
			}

		};
		avatar.addBehavior(skaterBehavior);
		skaterBehavior.setTimer(100);

		setAvatarActions(new WWAction[] { new PushAction(), new JumpAction(), new StopAction() });

	}

	@Override
	public void displayed() {
		showBannerAds();
	}

	@Override
	public boolean controller(float x, float y) {
		AndroidClientModel.getClientModel().forceAvatar(thrust, x * 2.5f, lift, 0, 0);
		return true;
	}

	public class WallBehavior extends WWBehavior {
		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			adjustEgg(nearObject, proximity);
			return true;
		}

		@Override
		public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			adjustEgg(nearObject, proximity);
			return true;
		}

		private void adjustEgg(WWObject egg, WWVector proximity) {
			synchronized (SkateWorld.this) {
				WWVector eggRotation = egg.getRotation();
				WWVector proxnorm = proximity.normalize();
				float proxlen = (float) Math.sqrt(proxnorm.x * proxnorm.x + proxnorm.y * proxnorm.y);
				float bowlangle = (float) Math.atan2(proxnorm.y, proxnorm.x);
				float tilt = -FastMath.sin(bowlangle + FastMath.TORADIAN * eggRotation.z) * proxlen * 1.5f;
				float lean = FastMath.cos(bowlangle + FastMath.TORADIAN * eggRotation.z) * proxlen * 1.5f;
				egg.setRotation(FastMath.TODEGREES * tilt, FastMath.TODEGREES * lean, eggRotation.z);
			}
		}

	}

	public void drawRamp(int x, int y, int angle, int size) {
		WWSimpleShape ramp = new WWBox();
		ramp.setMonolithic(true);
		ramp.setSize(new WWVector(size, size * 2, size));
		ramp.setPosition(new WWVector(x, y, -6));
		ramp.setRotation(new WWVector(0, 60, angle));
		ramp.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		ramp.setColor(WWObject.SIDE_ALL, getRandomColor());
		ramp.setFriction(0.0f);
		ramp.setSlidingSound("carScrape");
		ramp.addBehavior(new WallBehavior());
		addObject(ramp);
	}

	public void drawPipe(int x, int y, int angle, int size) {
		WWSimpleShape pipe = new WWCylinder();
		pipe.setMonolithic(true);
		pipe.setSize(size, size, size * 2);
		pipe.setPosition(x, y, 3f);
		pipe.setRotation(90 - angle / 10.0f, 180, angle);
		pipe.setHollow(0.75f);
		pipe.setCutoutStart(0.25f);
		pipe.setCutoutEnd(0.75f);
		pipe.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		pipe.setColor(WWObject.SIDE_ALL, getRandomColor());
		pipe.setFriction(0.0f);
		pipe.setSlidingSound("carScrape");
		addObject(pipe);
	}

	public void drawBowl(int x, int y, int size) {
		WWColor color = getRandomColor();
		WWSimpleShape bowl = new WWTorus();
		bowl.setMonolithic(true);
		bowl.setSize(new WWVector(size, size, size / 4.0f));
		bowl.setPosition(new WWVector(x, y, -size / 8.0f));
		bowl.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		bowl.setColor(WWObject.SIDE_ALL, color);
		bowl.setFriction(0.0f);
		bowl.setSlidingSound("carScrape");
		bowl.addBehavior(new WallBehavior());
		addObject(bowl);
		bowl = new WWTorus();
		bowl.setMonolithic(true);
		bowl.setSize(new WWVector(size * 3 / 2.0f, size * 3 / 2.0f, size * 2 / 5.0f));
		bowl.setPosition(new WWVector(x, y, -size / 8.0f));
		bowl.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		bowl.setColor(WWObject.SIDE_ALL, color);
		bowl.setFriction(0.0f);
		bowl.setSlidingSound("carScrape");
		bowl.addBehavior(new WallBehavior());
		addObject(bowl);
	}

	public void drawRails(int x, int y, int angle, int size) {
		WWColor color = getRandomColor();
		WWSimpleShape rail = new WWBox();
		rail.setMonolithic(true);
		rail.setSize(new WWVector(0.5f, size, 1));
		rail.setPosition(new WWVector(x, y, -0));
		rail.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		rail.setColor(WWObject.SIDE_ALL, color);
		rail.setFriction(0.0f);
		rail.setSlidingSound("carScrape");
		addObject(rail);
		rail = new WWBox();
		rail.setMonolithic(true);
		rail.setSize(new WWVector(0.5f, size, 1));
		rail.setPosition(new WWVector(x + 1, y + 1, -0));
		rail.setTextureURL(WWSimpleShape.SIDE_ALL, "ice");
		rail.setColor(WWObject.SIDE_ALL, color);
		rail.setFriction(0.0f);
		rail.setSlidingSound("carScrape");
		addObject(rail);
	}

	public WWColor getRandomColor() {
		float x = (float) (Math.random() / 0.5 + 0.5);
		float y = (float) (Math.random() / 0.5 + 0.5);
		float z = (float) (Math.random() / 0.5 + 0.5);
		return new WWColor(x, y, z);
	}

	@Override
	public int getSensorXType() {
		return WWWorld.MOVE_TYPE_TURN;
	}

	@Override
	public int getMoveXType() {
		return WWWorld.MOVE_TYPE_TURN;
	}

	@Override
	public int getMoveYType() {
		return WWWorld.MOVE_TYPE_NONE;
	}

	@Override
	public int getSensorYType() {
		return WWWorld.MOVE_TYPE_NONE;
	}

	@Override
	public float[] getMoveXTurn() {
		return new float[] { -90, -75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75, 90 };
	}

	class PushAction extends WWAction {
		@Override
		public String getName() {
			return "Push";
		}

		@Override
		public void start() {

			if (!rolling) {
				playSong("skate_music", 0.1f);
				rolling = true;
			}

			thrust = 15;
			AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, 0, 0);
		}

		@Override
		public void stop() {
			thrust = 0.0001f;
			AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, 0, 0);
		}
	}

	class StopAction extends WWAction {
		@Override
		public String getName() {
			return "Stop";
		}

		@Override
		public void start() {
			if (rolling) {
				stopPlayingSong();
				rolling = false;
			}
			thrust = 0;
			AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, 0, 0);
			avatar.setVelocity(new WWVector());
		}

	}

	class JumpAction extends WWAction {
		@Override
		public String getName() {
			return "Jump";
		}

		@Override
		public void start() {
			lift = 10;
			WWObject avatar = AndroidClientModel.getClientModel().getAvatar();
			// AndroidClientModel.getClientModel().thrustObject(avatar.getId()
			WWVector velocity = avatar.getVelocity();
			velocity.z = 10;
			avatar.setVelocity(velocity);
		}

		@Override
		public void stop() {
			lift = 0;
			// thrust = 0;
			AndroidClientModel.getClientModel().forceAvatar(thrust, torque, lift, 0, 0);
		}
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	@Override
	public PhysicsThread makePhysicsThread() {
		return new NewPhysicsThread(world, 15, 2);
	}
}
