package com.gallantrealm.webworld.worlds;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.NewPhysicsThread;
import com.gallantrealm.myworld.model.PhysicsThread;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWTorus;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

public class RaceWorld extends BaseEggWorld {

	static final float GRIDSIZE = 60;
	static final float GUIDE_WALL_TRANSPARENCY = 1.0f;

	final WWSimpleShape avatar;
	final WWSimpleShape contender;
	final WWSimpleShape finish;
	final WWSimpleShape start;
	final ArrayList<WWObject> cars;
	final ArrayList<WWObject> carWheels;
	boolean racing;
	ArrayList<WWSimpleShape> tracks;
	boolean contenderCrossedFinish;

	// these values used for building the track
	int startGridx;
	int startGridy;
	int gridx;
	int gridy;
	int gridz;
	int rotation;
	int preTrackObjectCount;

	private WWObject lastTrackObject;
	private float lastTrackOffset;
	private long lastTrackTime;
	private float lastA;
	private float lastDelta;

	class ContenderBehavior extends WWBehavior {

		public static final int HORIZONTAL_RIGHT = 0;
		public static final int VERTICAL_UP = 1;
		public static final int HORIZONTAL_LEFT = 2;
		public static final int VERTICAL_DOWN = 3;
		public static final int CURVE_LEFT = 4;
		public static final int CURVE_RIGHT = 5;

		int type;
		float x, y, radius;

		ContenderBehavior(int type, float x, float y, float radius) {
			this.type = type;
			this.x = x;
			this.y = y;
			this.radius = radius;
		}

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			handleContender(object, nearObject, proximity);
			return true;
		}

		@Override
		public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			handleContender(object, nearObject, proximity);
			return true;
		}

		private void handleContender(WWObject object, WWObject nearObject, WWVector proximity) {
			if (nearObject == contender) {
				if (racing) {
					WWVector position = contender.getPosition();
					float offset = 0;
					synchronized (contender) {
						if (type == HORIZONTAL_RIGHT) {
							offset = position.y - y;
						} else if (type == VERTICAL_UP) {
							offset = position.x - x;
						} else if (type == HORIZONTAL_LEFT) {
							offset = y - position.y;
						} else if (type == VERTICAL_DOWN) {
							offset = x - position.x;
						} else if (type == CURVE_LEFT) {
							offset = (float) Math.sqrt((position.x - x) * (position.x - x) + (position.y - y) * (position.y - y)) - radius;
						} else if (type == CURVE_RIGHT) {
							offset = radius - (float) Math.sqrt((position.x - x) * (position.x - x) + (position.y - y) * (position.y - y));
						}
						float delta = 0;
						if (object == lastTrackObject) {
							delta = (offset - lastTrackOffset) * (getWorldTime() - lastTrackTime);
//						float sqrtdelta = FastMath.sign(delta) * Math.sqrt(Math.abs(delta));
							delta = Math.max(delta, -5);
							delta = Math.min(delta, 5);
						}
						float vl = contender.getVelocity().length() + 1f;

						float a;
						if (type == CURVE_LEFT) {
							a = vl * (1.0f * delta + 1.75f * (offset + 1));
						} else if (type == CURVE_RIGHT) {
							a = vl * (1.0f * delta + 1.75f * (offset - 1));
						} else { // straight
							a = vl * (0.25f * delta + 0.05f * offset);
						}
						lastA = (3 * lastA + a) / 4.0f;
						contender.setAMomentum(new WWVector(0, 0, lastA));
						lastDelta = delta;
						lastTrackObject = object;
						lastTrackOffset = offset;
						lastTrackTime = getWorldTime();
//						trackBehavior = this;
					}
				} else {
					contender.setAMomentum(new WWVector(0, 0, 0));
				}
			}
		}

		@Override
		public boolean stopSlideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (nearObject == contender) {
				lastTrackOffset = 0;
				contender.setAMomentum(new WWVector(0, 0, 0));
			}
			return true;
		}
	}

	boolean avatarStarted;
	boolean contenderStarted;

	class StartLineBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			ClientModel clientModel = AndroidClientModel.getClientModel();
			if (nearObject == avatar) {
				avatarStarted = true;
			} else if (nearObject == contender) {
				contenderStarted = true;
			}

			return true;
		}

	}

	class FinishLineBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			AndroidClientModel clientModel = AndroidClientModel.getClientModel();
			if (nearObject == avatar) {
				if (avatarStarted && contenderStarted) {
					stopRacing();
					clientModel.setCameraDistance(5);
					clientModel.setCameraPan(135);
					clientModel.setCameraTilt(10);
					setTimer(2000);
				}
			} else if (nearObject == contender && contenderStarted) {
				contenderCrossedFinish = true;
			}

			return true;
		}

		@Override
		public boolean timerEvent() {
			AndroidClientModel clientModel = AndroidClientModel.getClientModel();
			if (contenderCrossedFinish) {
				playSound("loosingSound", 0.25f);
				int rc = clientModel.alert("Sorry, your opponent won the race.", new String[] { "Retry", "Quit" });
				if (rc == 0) {
				} else {
					clientModel.disconnect();
				}
			} else {
				playSound("winningSound", 0.125f);
				if (clientModel.isPlayMusic()) {
					playSound("winningSound2", 0.1f);
				}
				int rc = clientModel.alert(null, "Congratulations, you won!", new String[] { "Next Level", "Quit" }, "I won at level " + getLevel() + " in EggWorld Race!");
				if (rc == 0) {
					destroyTrack();
					setLevel(getLevel() + 1);
					clientModel.setScore(4, getLevel());
					clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
					makeTrack(getLevel());
				} else {
					clientModel.disconnect();
				}
			}
			resetRace();
			return true;
		}

	}

	private int raceticks;

	class CarBehavior extends WWBehavior {

		WWObject egg;
		WWObject car;
		WWObject wheel;
		float lastHt;

		public CarBehavior(WWObject egg, WWObject car, WWObject wheel) {
			this.egg = egg;
			this.car = car;
			this.wheel = wheel;
		}

		@Override
		public boolean timerEvent() {

			raceticks++;
			if (racing && egg == contender && raceticks % 100 == 0) {
				WWObject c = (WWObject) contender.clone();
				float newThrust = -(5 + 1.5f * world.getLevel() - world.getLevel() * FastMath.random(0.0f, 0.0015f) * raceticks);
				c.setThrust(new WWVector(0, newThrust, 0));
				c.setThrustVelocity(new WWVector(0, newThrust, 0));
				AndroidClientModel.getClientModel().thrustObject(contender.getId(), c);
			}

			float ht = egg.getThrust().length() - 0.05f * egg.getAMomentum().length();
			ht = (ht + 2.0f * lastHt) / 3.0f;
			car.setSound("car", 0.03f, ht * 0.12f);
			lastHt = ht;

			float hr = egg.getAMomentum().length();
			float hv = egg.getVelocity().length();
			float slideVol = hr * hv / 1000.0f;
			if (slideVol > 2.5f) {
				wheel.setSound("carSlide", 0.025f, 1);
			} else {
				wheel.setSound("carSlide", 0, 0);
			}

			setTimer(100);
			return true;
		}
	}

	class CrashBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			WWVector v = nearObject.getVelocity();
			if (nearObject == avatar) {
				AndroidClientModel.getClientModel().vibrate(100);
				nearObject.setVelocity(nearObject.getVelocity().scale(0.75f));
				nearObject.setThrust(nearObject.getThrust().scale(0.75f));
			}
			return true;
		}
	}

	public RaceWorld(String saveWorldFileName, String avatarName) {
		setName("Race");
		setGravity(9.8f);
		setLevel(Math.max(1, AndroidClientModel.getClientModel().getScore(4)));

		cars = new ArrayList<WWObject>();
		carWheels = new ArrayList<WWObject>();

		// the ground
		WWSimpleShape ground = new WWCylinder(); // for the ground
		ground.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x408040)); // green, like grass
		ground.setColor(WWSimpleShape.SIDE_BOTTOM, new WWColor(0x008000)); // green, like grass
		ground.setColor(WWSimpleShape.SIDE_SIDE1, new WWColor(0x008000)); // green, like grass
		ground.setPosition(new WWVector(0, 0, -50.5f));
		ground.setSize(new WWVector(1000, 1000, 100));
		addObject(ground);

		// the track
		makeTrack(getLevel());

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		avatar.setPosition(new WWVector(5, GRIDSIZE / 2.0f - 2, 2));
		avatar.setRotation(new WWVector(0, 0, -90));
		avatar.setFriction(0.1f);
		avatar.setDensity(1.1f);
		avatar.setFreedomMoveX(false);
		avatar.setFreedomMoveY(true);
		avatar.setFreedomMoveZ(true);
		avatar.setFreedomRotateX(false);
		avatar.setFreedomRotateY(false);
		avatar.setFreedomRotateZ(false);
		user.setAvatarId(avatar.getId());

		// car
		makeCar(avatar);

		// contender
		contender = makeAvatar("robot");
		contender.setFriction(0.1f);
		contender.setFreedomMoveX(false);
		contender.setFreedomMoveY(true);
		contender.setFreedomMoveZ(true);
		contender.setFreedomRotateX(false);
		contender.setFreedomRotateY(false);
		contender.setFreedomRotateZ(false);
		contender.setPosition(new WWVector(5, GRIDSIZE / 2.0f, 2));
		contender.setRotation(new WWVector(0, 0, -90));

		// car
		makeCar(contender);

		// finish line
		finish = new WWBox();
		finish.setSolid(false);
		finish.setSize(new WWVector(2, GRIDSIZE / 4.0f, 1.1f));
		finish.setPosition(new WWVector(7.5f, GRIDSIZE / 2.0f, 0));
		finish.setTransparency(SIDE_ALL, 1);
		finish.setTransparency(SIDE_TOP, 0);
		finish.setTextureURL(WWSimpleShape.SIDE_TOP, "checker");
		finish.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.5f);
		finish.setTextureScaleY(WWSimpleShape.SIDE_TOP, 1.0f / 12.5f);
		finish.addBehavior(new FinishLineBehavior());
		addObject(finish);

		// start line
		start = new WWBox();
		start.setSolid(false);
		start.setSize(new WWVector(2, GRIDSIZE / 4.0f, 1.1f));
		start.setPosition(new WWVector(12, GRIDSIZE / 2.0f, 0));
		start.setTransparency(SIDE_ALL, 1);
		start.addBehavior(new StartLineBehavior());
		addObject(start);

		setAvatarActions(new WWAction[] { new RaceAction() });
	}

	public WWObject makeCar(WWObject egg) {

		float carshine = 0.5f;

		WWSimpleShape carNose = new WWSphere();
		carNose.setSize(0.75f, 0.75f, 1.25f);
		carNose.setRotation(-90, 0, 0);
		carNose.setPosition(0.0f, -0.75f, -0.1f);
		carNose.setTaperX(0.5f);
		carNose.setTaperY(0.75f);
		carNose.setShearY(-0.25f);
		carNose.setMonolithic(true);
		carNose.setPhantom(true);
		carNose.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		carNose.setShininess(SIDE_ALL, carshine);
		carNose.setParent(egg.getId());
		world.addObject(carNose);

		WWSimpleShape noseWing = new WWCylinder();
		noseWing.setSize(0.05f, 0.25f, 1.2f);
		noseWing.setRotation(0, 90, 0);
		noseWing.setPosition(0.0f, -1.25f, -0.3f);
		noseWing.setMonolithic(true);
		noseWing.setPhantom(true);
		noseWing.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		noseWing.setShininess(SIDE_ALL, carshine);
		noseWing.setParent(egg.getId());
		world.addObject(noseWing);

		WWSimpleShape car = new WWTorus();
		car.setMonolithic(true);
		car.setPhantom(true);
		car.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		car.setShininess(SIDE_ALL, carshine);
		car.setSize(new WWVector(0.75f, 1.5f, 0.5f));
		car.setPosition(new WWVector(0, 0, -0.2f));
		car.setParent(egg.getId());
		world.addObject(car);

		WWSimpleShape tailRudder1 = new WWCylinder();
		tailRudder1.setMonolithic(true);
		tailRudder1.setPhantom(true);
		tailRudder1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		tailRudder1.setShininess(SIDE_ALL, carshine);
		tailRudder1.setSize(new WWVector(0.1f, 0.25f, 0.5f));
		tailRudder1.setPosition(new WWVector(0.25f, 0.5f, -0.0f));
		tailRudder1.setShearY(0.75f);
		tailRudder1.setParent(egg.getId());
		world.addObject(tailRudder1);

		WWSimpleShape tailRudder2 = new WWCylinder();
		tailRudder2.setMonolithic(true);
		tailRudder2.setPhantom(true);
		tailRudder2.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		tailRudder2.setShininess(SIDE_ALL, carshine);
		tailRudder2.setSize(new WWVector(0.1f, 0.25f, 0.5f));
		tailRudder2.setPosition(new WWVector(-0.25f, 0.5f, -0.0f));
		tailRudder2.setShearY(0.75f);
		tailRudder2.setParent(egg.getId());
		world.addObject(tailRudder2);

		WWSimpleShape tailWing = new WWCylinder();
		tailWing.setMonolithic(true);
		tailWing.setPhantom(true);
		tailWing.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0xE00000));
		tailWing.setShininess(SIDE_ALL, carshine);
		tailWing.setSize(new WWVector(0.05f, 0.25f, 1.2f));
		tailWing.setRotation(0, 90, 0);
		tailWing.setPosition(new WWVector(0, 0.7f, 0.25f));
		tailWing.setParent(egg.getId());
		world.addObject(tailWing);

		WWSimpleShape wheel1 = new WWCylinder();
		wheel1.setPhantom(true);
		wheel1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		wheel1.setTextureURL(SIDE_SIDE1, "tread");
		wheel1.setTextureScaleX(SIDE_SIDE1, 0.3f);
		wheel1.setSize(new WWVector(0.5f, 0.5f, 0.2f));
		wheel1.setRotation(new WWVector(0, 90, 0));
		wheel1.setPosition(new WWVector(-0.5f, -0.75f, -0.05f));
		wheel1.setParent(car.getId());
		world.addObject(wheel1);

		WWSimpleShape wheel2 = new WWCylinder();
		wheel2.setPhantom(true);
		wheel2.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		wheel2.setTextureURL(SIDE_SIDE1, "tread");
		wheel2.setTextureScaleX(SIDE_SIDE1, 0.3f);
		wheel2.setSize(new WWVector(0.5f, 0.5f, 0.2f));
		wheel2.setRotation(new WWVector(0, 90, 0));
		wheel2.setPosition(new WWVector(-0.5f, 0.75f, -0.05f));
		wheel2.setParent(car.getId());
		world.addObject(wheel2);

		WWSimpleShape wheel3 = new WWCylinder();
		wheel3.setPhantom(true);
		wheel3.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		wheel3.setTextureURL(SIDE_SIDE1, "tread");
		wheel3.setTextureScaleX(SIDE_SIDE1, 0.3f);
		wheel3.setSize(new WWVector(0.5f, 0.5f, 0.2f));
		wheel3.setRotation(new WWVector(0, 90, 0));
		wheel3.setPosition(new WWVector(0.5f, -0.75f, -0.05f));
		wheel3.setParent(car.getId());
		world.addObject(wheel3);

		WWSimpleShape wheel4 = new WWCylinder();
		wheel4.setPhantom(true);
		wheel4.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		wheel4.setTextureURL(SIDE_SIDE1, "tread");
		wheel4.setTextureScaleX(SIDE_SIDE1, 0.3f);
		wheel4.setSize(new WWVector(0.5f, 0.5f, 0.2f));
		wheel4.setRotation(new WWVector(0, 90, 0));
		wheel4.setPosition(new WWVector(0.5f, 0.75f, -0.05f));
		wheel4.setParent(car.getId());
		world.addObject(wheel4);

		WWSimpleShape axil1 = new WWCylinder();
		axil1.setPhantom(true);
		axil1.setMonolithic(true);
		axil1.setParent(car.getId());
		axil1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		axil1.setSize(new WWVector(0.1f, 0.1f, 1.0f));
		axil1.setRotation(0, 90, 0);
		axil1.setPosition(0, 0.75f, -0.05f);
		world.addObject(axil1);

		WWSimpleShape axil2 = new WWCylinder();
		axil2.setPhantom(true);
		axil2.setMonolithic(true);
		axil2.setParent(car.getId());
		axil2.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		axil2.setSize(new WWVector(0.1f, 0.1f, 1.0f));
		axil2.setRotation(0, 90, 0);
		axil2.setPosition(0, -0.75f, -0.05f);
		world.addObject(axil2);

		CarBehavior carBehavior = new CarBehavior(egg, car, wheel2);
		car.addBehavior(carBehavior);
		carBehavior.setTimer(250);

		cars.add(car);
		carWheels.add(wheel2);

		return car;
	}

	public void destroyTrack() {
		for (int i = 0; i < tracks.size(); i++) {
			removeObject(tracks.get(i).getId());
		}
		// AndroidRenderer.clearRenderings();
	}

	public void makeTrack(int level) {
		tracks = new ArrayList<WWSimpleShape>();
		if (level <= 2) {
			makeLeftCurve(0, 0, 0);
			makeLeftCurve(0, 1, 1);
			makeLeftCurve(2, 0, 3);
			makeLeftCurve(2, 1, 2);
			makeStraight(1, 0, 0);
			makeStraight(1, 1, 2);
		} else if (level <= 4) {
			makeStraight(1, 0, 0);
			makeStraight(2, 0, 0);
			makeStraight(3, 0, 0);
			makeLeftCurve(4, 0, 3);
			makeStraight(4, 1, 1);
			makeLeftCurve(4, 2, 2);
			makeStraight(3, 2, 2);
			makeLeftCurve(2, 2, 1);
			makeRightCurve(2, 1, 3);
			makeStraight(1, 1, 2);
			makeLeftCurve(0, 1, 1);
			makeLeftCurve(0, 0, 0);
		} else {
			makeStraight(1, 0, 0);
			makeStraight(2, 0, 0);
			makeLeftCurve(3, 0, 3);
			makeLeftCurve(3, 1, 2);
			makeRightCurve(2, 1, 0);
			makeRightCurve(2, 2, 1);
			makeLeftCurve(3, 2, 3);
			makeLeftCurve(3, 3, 2);
			makeStraight(2, 3, 2);
			makeStraight(1, 3, 2);
			makeLeftCurve(0, 3, 1);
			makeLeftCurve(0, 2, 0);
			makeRightCurve(1, 2, 2);
			makeRightCurve(1, 1, 3);
			makeLeftCurve(0, 1, 1);
			makeLeftCurve(0, 0, 0);
		}
	}

	void moveForward() {
		if (rotation == 0) {
			gridx = gridx + 1;
		} else if (rotation == 1) {
			gridy = gridy + 1;
		} else if (rotation == 2) {
			gridx = gridx - 1;
		} else { // rotation == 3
			gridy = gridy - 1;
		}
	}

	void moveUp() {
		gridz = gridz + 1;
	}

	void moveDown() {
		gridz = gridz - 1;
	}

	void turnLeft() {
		if (rotation == 0) {
			gridy = gridy + 1;
		} else if (rotation == 1) {
			gridx = gridx - 1;
		} else if (rotation == 2) {
			gridy = gridy - 1;
		} else { // rotation == 3
			gridx = gridx + 1;
		}
		rotation = rotation + 1;
		if (rotation > 3) {
			rotation = 0;
		}
	}

	void turnRight() {
		if (rotation == 0) {
			gridy = gridy - 1;
		} else if (rotation == 1) {
			gridx = gridx + 1;
		} else if (rotation == 2) {
			gridy = gridy + 1;
		} else { // rotation == 3
			gridx = gridx - 1;
		}
		rotation = rotation - 1;
		if (rotation < 0) {
			rotation = 3;
		}
	}

	public void makeRightCurve(int gridx, int gridy, int rotation) {
		makeCurve(gridx, gridy, rotation, true);
	}

	public void makeLeftCurve(int gridx, int gridy, int rotation) {
		makeCurve(gridx, gridy, rotation, false);
	}

	public void makeCurve(int gridx, int gridy, int rotation, boolean right) {
		float cutoutStart;
		float cutoutEnd;
		float x = GRIDSIZE * gridx;
		float y = -GRIDSIZE * gridy;

		if (rotation == 0) {
			cutoutStart = 0f;
			cutoutEnd = 0.25f;
			x = x;
			y = y;
		} else if (rotation == 1) {
			cutoutStart = 0.25f;
			cutoutEnd = 0.5f;
			x = x;
			y = y + GRIDSIZE;
		} else if (rotation == 2) {
			cutoutStart = 0.5f;
			cutoutEnd = 0.75f;
			x = x - GRIDSIZE;
			y = y + GRIDSIZE;
		} else { // rotation == 3
			cutoutStart = 0.75f;
			cutoutEnd = 1.0f;
			x = x - GRIDSIZE;
			y = y;
		}
		WWSimpleShape curve1 = new WWCylinder();
		curve1.setCircleVertices(64);
		curve1.setName("track");
		curve1.setMonolithic(true);
		curve1.setTextureURL(SIDE_ALL, "track");
		// curve1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		curve1.setSize(new WWVector(GRIDSIZE + GRIDSIZE / 4.0f, GRIDSIZE + GRIDSIZE / 4.0f, 10));
		curve1.setPosition(new WWVector(x, y, -4.5f));
		curve1.setRotation(new WWVector(0, 0, 0));
		curve1.setCutoutStart(cutoutStart);
		curve1.setCutoutEnd(cutoutEnd);
		curve1.setHollow(0.6f);
		addObject(curve1);
		tracks.add(curve1);
		if (right) {
			curve1.addBehavior(new ContenderBehavior(ContenderBehavior.CURVE_RIGHT, x, y, GRIDSIZE / 2));
		} else {
			curve1.addBehavior(new ContenderBehavior(ContenderBehavior.CURVE_LEFT, x, y, GRIDSIZE / 2));
		}

		WWCylinder wall1a = new WWCylinder();
		wall1a.setPenetratable(true);
		wall1a.noTopPenetration = true;
		wall1a.noBottomPenetration = true;
		wall1a.setCircleVertices(64);
		wall1a.setMonolithic(true);
		wall1a.setSize(new WWVector(GRIDSIZE - GRIDSIZE / 4.0f + 2.0f, GRIDSIZE - GRIDSIZE / 4.0f + 2.0f, 3));
		wall1a.setPosition(new WWVector(x, y, 0));
		wall1a.setRotation(new WWVector(0, 0, 0));
		wall1a.setCutoutStart(cutoutStart);
		wall1a.setCutoutEnd(cutoutEnd);
		wall1a.setHollow(0.915f);
		// wall1a.setTextureURL(WWObject.SIDE_ALL, "redstripe");
		wall1a.setTextureScaleX(WWObject.SIDE_ALL, 0.1f);
		wall1a.setTransparency(WWObject.SIDE_BOTTOM, 1);
		wall1a.setImpactSound("carCrash");
		wall1a.setSlidingSound("carScrape");
		wall1a.addBehavior(new CrashBehavior());
		addObject(wall1a);
		tracks.add(wall1a);

		WWCylinder wall1b = new WWCylinder();
		wall1b.setPenetratable(true);
		wall1b.noTopPenetration = true;
		wall1b.noBottomPenetration = true;
		wall1b.setCircleVertices(64);
		wall1b.setMonolithic(true);
		wall1b.setSize(new WWVector(GRIDSIZE + GRIDSIZE / 4.0f + 2.0f, GRIDSIZE + GRIDSIZE / 4.0f + 2.0f, 3));
		wall1b.setPosition(new WWVector(x, y, 0));
		wall1b.setRotation(new WWVector(0, 0, 0));
		wall1b.setCutoutStart(cutoutStart);
		wall1b.setCutoutEnd(cutoutEnd);
		wall1b.setHollow(0.948f);
		wall1b.setTextureURL(WWObject.SIDE_ALL, "redstripe");
		wall1b.setTextureScaleX(WWObject.SIDE_ALL, 0.1f);
		wall1b.setTransparency(WWObject.SIDE_BOTTOM, 1);
		wall1b.setImpactSound("carCrash");
		wall1b.setSlidingSound("carScrape");
		wall1b.addBehavior(new CrashBehavior());
		addObject(wall1b);
		tracks.add(wall1b);

		WWSimpleShape smackwall = new WWCylinder();
		smackwall.setMonolithic(true);
		smackwall.setSize(new WWVector(GRIDSIZE + GRIDSIZE / 4.0f + 1.85f, GRIDSIZE + GRIDSIZE / 4.0f + 1.85f, 6));
		smackwall.setPosition(new WWVector(x, y, 4));
		smackwall.setRotation(new WWVector(0, 0, 0));
		smackwall.setCutoutStart(cutoutStart);
		smackwall.setCutoutEnd(cutoutEnd);
		smackwall.setHollow(0.92f);
		smackwall.setTransparency(WWObject.SIDE_ALL, 1);
		smackwall.addBehavior(new CrashBehavior());
		addObject(smackwall);
		tracks.add(smackwall);

		if (right) {
			turnRight();
		} else {
			turnLeft();
		}
	}

	public void makeStraight(int gridx, int gridy, int rotation) {
		float x = GRIDSIZE * gridx - GRIDSIZE / 2.0f;
		float y = -GRIDSIZE * gridy + GRIDSIZE / 2.0f;
		WWSimpleShape straight1 = new WWBox();
		straight1.setName("track");
		straight1.setMonolithic(true);
		straight1.setTextureURL(SIDE_ALL, "track");
		// straight1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		straight1.setSize(new WWVector(GRIDSIZE + 2, GRIDSIZE / 4.0f, 10));
		straight1.setPosition(new WWVector(x, y, -4.5f));
		if (rotation == 0 || rotation == 2) {
			straight1.setRotation(new WWVector(0, 0, 0));
		} else {
			straight1.setRotation(new WWVector(0, 0, 90));
		}
		addObject(straight1);
		tracks.add(straight1);
		straight1.addBehavior(new ContenderBehavior(rotation, x, y, 0));

		WWBox wall1a = new WWBox();
		wall1a.setPenetratable(true);
		wall1a.noXPenetration = true;
		wall1a.noZPenetration = true;
		wall1a.setMonolithic(true);
		wall1a.setSize(new WWVector(GRIDSIZE + 0.1f, 2, 2));
		if (rotation == 0 || rotation == 2) {
			wall1a.setPosition(new WWVector(x, y + GRIDSIZE / 8.0f, 0.5f));
		} else {
			wall1a.setRotation(new WWVector(0, 0, 90));
			wall1a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y, 0.5f));
		}
		wall1a.setImpactSound("carCrash");
		wall1a.setSlidingSound("carScrape");
		wall1a.addBehavior(new CrashBehavior());
		addObject(wall1a);
		tracks.add(wall1a);

		WWBox wall1b = new WWBox();
		wall1b.setPenetratable(true);
		wall1b.noXPenetration = true;
		wall1b.noZPenetration = true;
		wall1b.setMonolithic(true);
		wall1b.setSize(new WWVector(GRIDSIZE + 0.1f, 2, 2));
		if (rotation == 0 || rotation == 2) {
			wall1b.setPosition(new WWVector(x, y - GRIDSIZE / 8.0f, 0.5f));
		} else {
			wall1b.setRotation(new WWVector(0, 0, 90));
			wall1b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y, 0.5f));
		}
		wall1b.setImpactSound("carCrash");
		wall1b.setSlidingSound("carScrape");
		wall1b.addBehavior(new CrashBehavior());
		addObject(wall1b);
		tracks.add(wall1b);

		moveForward();
	}

	public void makeBridge(int gridx, int gridy, int rotation) {

		float x = GRIDSIZE * gridx - GRIDSIZE / 2.0f;
		float y = -GRIDSIZE * gridy + GRIDSIZE / 2.0f;

		WWSimpleShape straight1 = new WWBox();
		straight1.setName("track");
		straight1.setMonolithic(true);
		straight1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		straight1.setSize(new WWVector(GRIDSIZE / 3.0f + 2.0f, GRIDSIZE / 4.0f, 10));
		if (rotation == 0 || rotation == 2) {
			straight1.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y, 2 - 5.25f));
			straight1.setRotation(new WWVector(-10, 0, 0));
		} else {
			straight1.setPosition(new WWVector(x, y - GRIDSIZE / 3.0f, 2 - 5.25f));
			straight1.setRotation(new WWVector(0, -10, 90));
		}
		addObject(straight1);
		tracks.add(straight1);

		WWSimpleShape straight2 = new WWBox();
		straight2.setName("track");
		straight2.setMonolithic(true);
		straight2.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		straight2.setSize(new WWVector(GRIDSIZE / 3.0f, GRIDSIZE / 4.0f, 1));
		straight2.setPosition(new WWVector(x, y, 2.5f));
		if (rotation == 0 || rotation == 2) {
			straight2.setRotation(new WWVector(0, 0, 0));
		} else {
			straight2.setRotation(new WWVector(0, 0, 90));
		}
		addObject(straight2);
		tracks.add(straight2);

		WWSimpleShape straight3 = new WWBox();
		straight3.setName("track");
		straight3.setMonolithic(true);
		straight3.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		straight3.setSize(new WWVector(GRIDSIZE / 3.0f + 2.0f, GRIDSIZE / 4.0f, 10));
		if (rotation == 0 || rotation == 2) {
			straight3.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y, 2 - 5.25f));
			straight3.setRotation(new WWVector(10, 0, 0));
		} else {
			straight3.setPosition(new WWVector(x, y + GRIDSIZE / 3.0f, 2 - 5.25f));
			straight3.setRotation(new WWVector(0, 10, 90));
		}
		addObject(straight3);
		tracks.add(straight3);

		WWSimpleShape roof1 = new WWBox();
		roof1.setMonolithic(true);
		roof1.setPenetratable(true);
		roof1.setTransparency(WWSimpleShape.SIDE_ALL, 1);
		roof1.setSize(new WWVector(GRIDSIZE / 3.0f + 1.5f, GRIDSIZE / 4.0f, 1));
		if (rotation == 0 || rotation == 2) {
			roof1.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y, 4.25f));
			roof1.setRotation(new WWVector(-10, 0, 0));
		} else {
			roof1.setPosition(new WWVector(x, y - GRIDSIZE / 3.0f, 4.25f));
			roof1.setRotation(new WWVector(0, -10, 90));
		}
		addObject(roof1);
		tracks.add(roof1);

		WWSimpleShape roof2 = new WWBox();
		roof2.setMonolithic(true);
		roof2.setPenetratable(true);
		roof2.setTransparency(WWSimpleShape.SIDE_ALL, 1);
		roof2.setSize(new WWVector(GRIDSIZE / 3.0f, GRIDSIZE / 4.0f, 1));
		roof2.setPosition(new WWVector(x, y, 5.5f));
		if (rotation == 0 || rotation == 2) {
			roof2.setRotation(new WWVector(0, 0, 0));
		} else {
			roof2.setRotation(new WWVector(0, 0, 90));
		}
		addObject(roof2);
		tracks.add(roof2);

		WWSimpleShape roof3 = new WWBox();
		roof3.setMonolithic(true);
		roof3.setPenetratable(true);
		roof3.setTransparency(WWSimpleShape.SIDE_ALL, 1);
		roof3.setSize(new WWVector(GRIDSIZE / 3.0f + 1.5f, GRIDSIZE / 4.0f, 1));
		if (rotation == 0 || rotation == 2) {
			roof3.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y, 4.25f));
			roof3.setRotation(new WWVector(10, 0, 0));
		} else {
			roof3.setPosition(new WWVector(x, y + GRIDSIZE / 3.0f, 4.25f));
			roof3.setRotation(new WWVector(0, 10, 90));
		}
		addObject(roof3);
		tracks.add(roof3);

		WWSimpleShape wall1a = new WWBox();
		wall1a.setMonolithic(true);
		wall1a.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0 || rotation == 2) {
			wall1a.setRotation(new WWVector(-15, 0, 0));
			wall1a.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y + GRIDSIZE / 8.0f, 2.25f));
		} else {
			wall1a.setRotation(new WWVector(0, -15, 90));
			wall1a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y - GRIDSIZE / 3.0f, 2.25f));
		}
		wall1a.setImpactSound("carCrash");
		wall1a.setSlidingSound("carScrape");
		wall1a.addBehavior(new CrashBehavior());
		addObject(wall1a);
		tracks.add(wall1a);

		WWSimpleShape wall2a = new WWBox();
		wall2a.setMonolithic(true);
		wall2a.setSize(new WWVector(GRIDSIZE / 3.0f + 0.25f, 2, 3));
		if (rotation == 0 || rotation == 2) {
			wall2a.setPosition(new WWVector(x, y + GRIDSIZE / 8.0f, 4f));
		} else {
			wall2a.setRotation(new WWVector(0, 0, 90));
			wall2a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y, 4f));
		}
		wall2a.setImpactSound("carCrash");
		wall2a.setSlidingSound("carScrape");
		wall2a.addBehavior(new CrashBehavior());
		addObject(wall2a);
		tracks.add(wall2a);

		WWSimpleShape wall3a = new WWBox();
		wall3a.setMonolithic(true);
		wall3a.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0 || rotation == 2) {
			wall3a.setRotation(new WWVector(15, 0, 0));
			wall3a.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y + GRIDSIZE / 8.0f, 2.25f));
		} else {
			wall3a.setRotation(new WWVector(0, 15, 90));
			wall3a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y + GRIDSIZE / 3.0f, 2.25f));
		}
		wall3a.setImpactSound("carCrash");
		wall3a.setSlidingSound("carScrape");
		wall3a.addBehavior(new CrashBehavior());
		addObject(wall3a);
		tracks.add(wall3a);

		WWSimpleShape wall1b = new WWBox();
		wall1b.setMonolithic(true);
		wall1b.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0 || rotation == 2) {
			wall1b.setRotation(new WWVector(-15, 0, 0));
			wall1b.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y - GRIDSIZE / 8.0f, 2.25f));
		} else {
			wall1b.setRotation(new WWVector(0, -15, 90));
			wall1b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y - GRIDSIZE / 3.0f, 2.25f));
		}
		wall1b.setImpactSound("carCrash");
		wall1b.setSlidingSound("carScrape");
		wall1b.addBehavior(new CrashBehavior());
		addObject(wall1b);
		tracks.add(wall1b);

		WWSimpleShape wall2b = new WWBox();
		wall2b.setMonolithic(true);
		wall2b.setSize(new WWVector(GRIDSIZE / 3.0f + 0.25f, 2, 3));
		if (rotation == 0 || rotation == 2) {
			wall2b.setPosition(new WWVector(x, y - GRIDSIZE / 8.0f, 4f));
		} else {
			wall2b.setRotation(new WWVector(0, 0, 90));
			wall2b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y, 4f));
		}
		wall2b.setImpactSound("carCrash");
		wall2b.setSlidingSound("carScrape");
		wall2b.addBehavior(new CrashBehavior());
		addObject(wall2b);
		tracks.add(wall2b);

		WWSimpleShape wall3b = new WWBox();
		wall3b.setMonolithic(true);
		wall3b.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0 || rotation == 2) {
			wall3b.setRotation(new WWVector(15, 0, 0));
			wall3b.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y - GRIDSIZE / 8.0f, 2.25f));
		} else {
			wall3b.setRotation(new WWVector(0, 15, 90));
			wall3b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y + GRIDSIZE / 3.0f, 2.25f));
		}
		wall3b.setImpactSound("carCrash");
		wall3b.setSlidingSound("carScrape");
		wall3b.addBehavior(new CrashBehavior());
		addObject(wall3b);
		tracks.add(wall3b);

		moveForward();
	}

	public void makeJump(int gridx, int gridy, int rotation) {

		float x = GRIDSIZE * gridx - GRIDSIZE / 2.0f;
		float y = -GRIDSIZE * gridy + GRIDSIZE / 2.0f;

		WWSimpleShape straight1 = new WWBox();
		straight1.setName("jump");
		straight1.setMonolithic(true);
		straight1.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		straight1.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, GRIDSIZE / 4.0f, 2));
		if (rotation == 0) {
			straight1.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y, 2 - 0.25f));
			straight1.setRotation(new WWVector(0, 20, 0));
		} else if (rotation == 1) {
			straight1.setPosition(new WWVector(x, y + GRIDSIZE / 3.0f, 2 - 0.25f));
			straight1.setRotation(new WWVector(0, 20, 90));
		} else if (rotation == 2) {
			straight1.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y, 2 - 0.25f));
			straight1.setRotation(new WWVector(0, -20, 0));
		} else if (rotation == 3) {
			straight1.setPosition(new WWVector(x, y - GRIDSIZE / 3.0f, 2 - 0.25f));
			straight1.setRotation(new WWVector(0, -20, 90));
		}
		addObject(straight1);
		tracks.add(straight1);

		// catch air in straight2

		WWSimpleShape straight3 = new WWBox();
		straight3.setName("track");
		straight3.setMonolithic(true);
		straight3.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404040));
		straight3.setSize(new WWVector(GRIDSIZE / 3.0f + 2.0f, GRIDSIZE / 4.0f, 10));
		if (rotation == 0) {
			straight3.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y, 2 - 6.0f));
			straight3.setRotation(new WWVector(0, -3.5f, 0));
		} else if (rotation == 1) {
			straight3.setPosition(new WWVector(x, y - GRIDSIZE / 3.0f, 2 - 6.0f));
			straight3.setRotation(new WWVector(0, -3.5f, 90));
		} else if (rotation == 2) {
			straight3.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y, 2 - 6.0f));
			straight3.setRotation(new WWVector(0, 3.5f, 0));
		} else if (rotation == 3) {
			straight3.setPosition(new WWVector(x, y + GRIDSIZE / 3.0f, 2 - 6.0f));
			straight3.setRotation(new WWVector(0, 3.5f, 90));
		}
		addObject(straight3);
		tracks.add(straight3);

		WWSimpleShape wall1a = new WWBox();
		wall1a.setMonolithic(true);
		wall1a.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0) {
			wall1a.setRotation(new WWVector(0, 20, 0));
			wall1a.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y + GRIDSIZE / 8.0f, 2.25f));
		} else if (rotation == 1) {
			wall1a.setRotation(new WWVector(0, 20, 90));
			wall1a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y + GRIDSIZE / 3.0f, 2.25f));
		} else if (rotation == 2) {
			wall1a.setRotation(new WWVector(0, -20, 0));
			wall1a.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y + GRIDSIZE / 8.0f, 2.25f));
		} else if (rotation == 3) {
			wall1a.setRotation(new WWVector(0, -20, 90));
			wall1a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y - GRIDSIZE / 3.0f, 2.25f));
		}
		wall1a.setImpactSound("carCrash");
		wall1a.setSlidingSound("carScrape");
		wall1a.addBehavior(new CrashBehavior());
		addObject(wall1a);
		tracks.add(wall1a);

		// catch air in wall2

		WWSimpleShape wall3a = new WWBox();
		wall3a.setMonolithic(true);
		wall3a.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0) {
			wall3a.setRotation(new WWVector(0, 0, 0));
			wall3a.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y + GRIDSIZE / 8.0f, 0f));
		} else if (rotation == 1) {
			wall3a.setRotation(new WWVector(0, 0, 90));
			wall3a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y - GRIDSIZE / 3.0f, 0f));
		} else if (rotation == 2) {
			wall3a.setRotation(new WWVector(0, 0, 0));
			wall3a.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y + GRIDSIZE / 8.0f, 0f));
		} else if (rotation == 3) {
			wall3a.setRotation(new WWVector(0, 0, 90));
			wall3a.setPosition(new WWVector(x + GRIDSIZE / 8.0f, y + GRIDSIZE / 3.0f, 0f));
		}
		wall3a.setImpactSound("carCrash");
		wall3a.setSlidingSound("carScrape");
		wall3a.addBehavior(new CrashBehavior());
		addObject(wall3a);
		tracks.add(wall3a);

		WWSimpleShape wall1b = new WWBox();
		wall1b.setMonolithic(true);
		wall1b.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0) {
			wall1b.setRotation(new WWVector(0, 20, 0));
			wall1b.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y - GRIDSIZE / 8.0f, 2.25f));
		} else if (rotation == 1) {
			wall1b.setRotation(new WWVector(0, 20, 90));
			wall1b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y + GRIDSIZE / 3.0f, 2.25f));
		} else if (rotation == 2) {
			wall1b.setRotation(new WWVector(0, -20, 0));
			wall1b.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y - GRIDSIZE / 8.0f, 2.25f));
		} else if (rotation == 3) {
			wall1b.setRotation(new WWVector(0, -20, 90));
			wall1b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y - GRIDSIZE / 3.0f, 2.25f));
		}
		wall1b.setImpactSound("carCrash");
		wall1b.setSlidingSound("carScrape");
		wall1b.addBehavior(new CrashBehavior());
		addObject(wall1b);
		tracks.add(wall1b);

		// catch air on wall2b

		WWSimpleShape wall3b = new WWBox();
		wall3b.setMonolithic(true);
		wall3b.setSize(new WWVector(GRIDSIZE / 3.0f + 1.0f, 2, 3));
		if (rotation == 0) {
			wall3b.setRotation(new WWVector(-0, 0, 0));
			wall3b.setPosition(new WWVector(x + GRIDSIZE / 3.0f, y - GRIDSIZE / 8.0f, 0f));
		} else if (rotation == 1) {
			wall3b.setRotation(new WWVector(0, -0, 90));
			wall3b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y - GRIDSIZE / 3.0f, 0f));
		} else if (rotation == 2) {
			wall3b.setRotation(new WWVector(0, 0, 0));
			wall3b.setPosition(new WWVector(x - GRIDSIZE / 3.0f, y - GRIDSIZE / 8.0f, 0f));
		} else if (rotation == 3) {
			wall3b.setRotation(new WWVector(0, 0, 90));
			wall3b.setPosition(new WWVector(x - GRIDSIZE / 8.0f, y + GRIDSIZE / 3.0f, 0f));
		}
		wall3b.setImpactSound("carCrash");
		wall3b.setSlidingSound("carScrape");
		wall3b.addBehavior(new CrashBehavior());
		addObject(wall3b);
		tracks.add(wall3b);

		moveForward();
	}

	public class RaceAction extends WWAction {
		@Override
		public String getName() {
			return "Race";
		}

		@Override
		public void start() {
			hideBannerAds();
			ClientModel clientModel = AndroidClientModel.getClientModel();
			clientModel.setViewpoint(clientModel.getViewpoint()); // to force view from viewpoint
			if (!racing) {
				countDown(3);
			} else if (AndroidClientModel.getClientModel().paused) {
				AndroidClientModel.getClientModel().resumeWorld();
			}
		}
	}

	void countDown(final int secs) {
		playSound("beep", 0.1f, 0.75f);
		AndroidClientModel.getClientModel().flashMessage("" + secs, true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (secs > 1) {
					countDown(secs - 1);
				} else {
					playSound("beep", 0.1f, 1f);
					AndroidClientModel.getClientModel().flashMessage("Go", true);
					startRacing();
				}
			}
		}, 1000);
	}

	class PauseAction extends WWAction {
		@Override
		public String getName() {
			return "Pause";
		}

		@Override
		public void start() {
			stopRacing();
		}
	}

	public void startRacing() {
		raceticks = 0;
		ClientModel clientModel = AndroidClientModel.getClientModel();
		thrust = 6;
		racing = true;
		clientModel.forceAvatar(thrust, 0, 0, 0, 0);
		WWObject c = (WWObject) contender.clone();
		c.setThrust(new WWVector(0, -(5 + 1.5f * world.getLevel()), 0));
		c.setThrustVelocity(new WWVector(0, -(5 + 1.5f * world.getLevel()), 0));
		clientModel.thrustObject(contender.getId(), c);
		playSong("race_music", 0.1f);
		setAvatarActions(new WWAction[0]);
	}

	public void stopRacing() {
		ClientModel clientModel = AndroidClientModel.getClientModel();
		racing = false;
		raceticks = 0;
		stopPlayingSong();
		clientModel.forceAvatar(0, 0, 0, 0, 0);
		avatar.setThrust(new WWVector(0, 0, 0));
//		avatar.setVelocity(new WWVector(0, 0, 0));
		avatar.setTorque(new WWVector(0, 0, 0));
		avatar.setAMomentum(new WWVector(0, 0, 0));
		contender.setThrust(new WWVector(0, 0, 0));
//		contender.setVelocity(new WWVector(0, 0, 0));
		contender.setTorque(new WWVector(0, 0, 0));
		contender.setAMomentum(new WWVector(0, 0, 0));
		stopAllSounds();
	}

	public void resetRace() {
		showBannerAds();
		racing = false;
		avatar.setThrust(new WWVector(0, 0, 0));
		avatar.setVelocity(new WWVector(0, 0, 0));
		avatar.setTorque(new WWVector(0, 0, 0));
		avatar.setAMomentum(new WWVector(0, 0, 0));
		avatar.setPosition(new WWVector(5, GRIDSIZE / 2.0f - 2, 2));
		avatar.setRotation(new WWVector(0, 0, -90));

		contender.setThrust(new WWVector(0, 0, 0));
		contender.setVelocity(new WWVector(0, 0, 0));
		contender.setTorque(new WWVector(0, 0, 0));
		contender.setAMomentum(new WWVector(0, 0, 0));
		contender.setPosition(new WWVector(5, GRIDSIZE / 2.0f, 2));
		contender.setRotation(new WWVector(0, 0, -90));

		avatarStarted = false;
		contenderStarted = false;
		contenderCrossedFinish = false;

		setAvatarActions(new WWAction[] { new RaceAction() });

		AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint());
	}

	float lastThrust;
	float lastTorque;

	@Override
	public boolean controller(float x, float y) {
		if (racing && !AndroidClientModel.getClientModel().useSensors()) {
			float thrust = lastThrust;
			if (y != 0) {
				thrust = y * (0.2f + 0.01f * getLevel());
			}
			float torque;
			if (x == 0) {
				torque = (4 * lastTorque + x) / 5.0f;
			} else {
				torque = (2 * lastTorque + x) / 3.0f;
			}
			AndroidClientModel.getClientModel().forceAvatar(thrust, 2.0f * torque, 0, 0, 0);
			lastThrust = thrust;
			lastTorque = torque;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getSensorXType() {
		if (racing) {
			return WWWorld.MOVE_TYPE_TURN;
		} else {
			return WWWorld.MOVE_TYPE_NONE;
		}
	}

	@Override
	public int getSensorYType() {
		if (racing) {
			return WWWorld.MOVE_TYPE_THRUST;
		} else {
			return WWWorld.MOVE_TYPE_NONE;
		}
	}

	@Override
	public int getMoveXType() {
		if (racing) {
			return WWWorld.MOVE_TYPE_TURN;
		} else {
			return WWWorld.MOVE_TYPE_NONE;
		}
	}

	@Override
	public int getMoveYType() {
		if (racing) {
			return WWWorld.MOVE_TYPE_THRUST;
		} else {
			return WWWorld.MOVE_TYPE_NONE;
		}
	}

	@Override
	public float[] getMoveXTurn() {
		return new float[] { -100, -90, -80, -70, -60, -50, -40, -30, -20, -10, 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	}

	@Override
	public float[] getMoveYThrust() {
		float x = 2 + 0.25f * getLevel();
		return new float[] { 0, 0.5f * x, 1.0f * x, 2.0f * x, 3.0f * x, 4.0f * x };
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	@Override
	public PhysicsThread makePhysicsThread() {
		return new NewPhysicsThread(world, 10, 0);
	}

	@Override
	public boolean dampenCamera() {
		return false;
	}

}
