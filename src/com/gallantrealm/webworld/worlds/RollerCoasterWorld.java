package com.gallantrealm.webworld.worlds;

import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

/**
 * Coaster builder for eggs.
 */
public class RollerCoasterWorld extends BaseEggWorld {

	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	public float SECTION_SIZE = 1f;
	public float TURN_SIZE = 15;

	int preTrackObjectCount;
	boolean dirty;
	WWAction[] worldBuildActions;
	WWAction[] worldRideActions;
	WWAction[] avatarBuildActions;
	WWAction[] avatarRideActions;
	WWSimpleShape carCore;
	WWSimpleShape avatar;
	WWSimpleShape car;
	AvatarRidingAnimation avatarRidingBehavior;
	boolean riding;
	ArrayList<CoasterSection> coasterSections;
	float x, y, z, direction, slope;
	int beamGroup, railGroup, supportGroup;

	public class TrackPiece extends WWBox {
		public TrackPiece(CoasterSection section, boolean addSupport) {
			super(2, SECTION_SIZE, 1);
			setTransparency(SIDE_ALL, 1);
			setPenetratable(true);
			setPhantom(true);
			setFixed(true);
			RollerCoasterWorld.this.addObject(this);

			float beamLength = SECTION_SIZE;
			float rail1Length = SECTION_SIZE;
			float rail2Length = SECTION_SIZE;
			float dx = 0;
			float dy = 0;
			float dz = 0;
			if (section.type == CoasterSection.SectionType.CURVE_UP) {
				beamLength *= 1.05f;
				rail1Length *= 1.0f;
				rail2Length *= 1.0f;
				// dz = 0.02f;
			} else if (section.type == CoasterSection.SectionType.CURVE_DOWN) {
				beamLength *= 1.05f;
				rail1Length *= 1.1f;
				rail2Length *= 1.1f;
				// dz = -0.02f;
			} else if (section.type == CoasterSection.SectionType.LEFT_TURN) {
				beamLength *= 1.05f;
				rail1Length *= 0.96f;
				rail2Length *= 1.1f;
				// dx = -0.02f;
			} else if (section.type == CoasterSection.SectionType.RIGHT_TURN) {
				beamLength *= 1.05f;
				rail1Length *= 1.1f;
				rail2Length *= 0.96f;
				// dx = 0.02f;
			} else { // straight
				beamLength *= 1.05f;
				rail1Length *= 1.1f;
				rail2Length *= 1.1f;
			}

			float slopeAdjust = FastMath.sin(section.slope * FastMath.TORADIAN);

			WWCylinder beam = new WWCylinder(0.25f, 0.25f, beamLength);
			beam.setCircleVertices(3);
			beam.setPhantom(true);
			beam.setPenetratable(true);
			// note: it is better not to be monolithic and instead don't draw the ends for a cylinder
			beam.setFixed(true);
			beam.setColor(SIDE_ALL, new WWColor(1, 0.25f, 0.25f));
			beam.setTransparency(SIDE_TOP, 1);
			beam.setTransparency(SIDE_BOTTOM, 1);
			beam.setRotation(90, 0, 0);
			beam.setPosition(0 + dx, 0 + dy, 0 + dz);
			RollerCoasterWorld.this.addObject(beam);
			beam.setParent(this);
//			beam.setGroup(beamGroup);

			WWBox rail1 = new WWBox(0.125f, rail1Length, 0.25f);
			if (section.type == CoasterSection.SectionType.CURVE_DOWN) {
				rail1.setTaperY(-0.06f);
			} else if (section.type == CoasterSection.SectionType.CURVE_UP) {
				rail1.setTaperY(-0.2f);
			}
			rail1.setPhantom(true);
			rail1.setPenetratable(true);
			rail1.setMonolithic(true);
			rail1.setFixed(true);
			rail1.setColor(SIDE_ALL, new WWColor(0.25f, 0.25f, 1f));
			rail1.setPosition(-0.25f + dx, 0 + dy, 0.5f + dz);
			if (section.type == CoasterSection.SectionType.LEFT_TURN) {
				rail1.setRotation(5 * slopeAdjust, 0, -10 * slopeAdjust);
			} else if (section.type == CoasterSection.SectionType.RIGHT_TURN) {
				rail1.setRotation(-5 * slopeAdjust, 0, 10 * slopeAdjust);
			}
			RollerCoasterWorld.this.addObject(rail1);
			rail1.setParent(this);
//			rail1.setGroup(railGroup);

			WWBox rail2 = new WWBox(0.125f, rail2Length, 0.25f);
			if (section.type == CoasterSection.SectionType.CURVE_DOWN) {
				rail2.setTaperY(-0.06f);
			} else if (section.type == CoasterSection.SectionType.CURVE_UP) {
				rail2.setTaperY(-0.2f);
			}
			rail2.setPhantom(true);
			rail2.setPenetratable(true);
			rail2.setMonolithic(true);
			rail2.setFixed(true);
			rail2.setColor(SIDE_ALL, new WWColor(0.25f, 0.25f, 1f));
			rail2.setPosition(0.25f + dx, 0 + dy, 0.5f + dz);
			if (section.type == CoasterSection.SectionType.LEFT_TURN) {
				rail2.setRotation(-5 * slopeAdjust, 0, -10 * slopeAdjust);
			} else if (section.type == CoasterSection.SectionType.RIGHT_TURN) {
				rail2.setRotation(5 * slopeAdjust, 0, 10 * slopeAdjust);
			}
			RollerCoasterWorld.this.addObject(rail2);
			rail2.setParent(this);
//			rail2.setGroup(railGroup);

			if (addSupport && (section.slope % 360 < 90 || section.slope % 360 > 270)) {
				WWCylinder support = new WWCylinder(0.2f, 0.2f, section.z);
				support.setCircleVertices(3);
				support.setMonolithic(true);
				support.setPenetratable(true);
				support.setPhantom(true);
				support.setFixed(true);
				support.setPosition(section.x, section.y, section.z / 2.0f);
				RollerCoasterWorld.this.addObject(support);
				support.setGroup(supportGroup);
			}

			section.objectCount = lastObjectIndex;
		}
	}

	public RollerCoasterWorld(String saveWorldFileName, String avatarName) {

		// create a world, add ground, some objects, and one "egg" user
		setName("Roller Coaster");
		setGravity(9.8f); // earth gravity

		railGroup = getNextGroup();
		beamGroup = getNextGroup();
		supportGroup = getNextGroup();

		// the ground
		WWBox ground = new WWBox(); // for the ground
		ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x008000)); // green, like grass
		ground.setPosition(new WWVector(0, 0, -50));
		ground.setSize(new WWVector(1000, 1000, 100));
		ground.setTextureURL(WWSimpleShape.SIDE_ALL, "grass");
		ground.setTextureScaleX(WWSimpleShape.SIDE_ALL, 0.01f);
		ground.setTextureScaleY(WWSimpleShape.SIDE_ALL, 0.01f);
		addObject(ground);

		// the sky
		WWSimpleShape sky = new WWSphere();
		sky.setName("sky");
		sky.setPenetratable(true);
		sky.setTransparency(SIDE_ALL, 0.01f); // keeps from generating shadows
		sky.setPosition(new WWVector(0, 0, -800));
		sky.setSize(new WWVector(2000, 5000, 5000));
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

		carCore = new WWBox();
		carCore.setPhantom(true);
		carCore.setTransparency(SIDE_ALL, 1);
		addObject(carCore);
		avatarRidingBehavior = new AvatarRidingAnimation();
		carCore.addBehavior(avatarRidingBehavior);

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		user.setAvatarId(avatar.getId());
		avatar.setPhysical(false);
		avatar.setPosition(0, 0, 1.3f);
		avatar.setParent(carCore);
		avatar.setFixed(true);

		// car
		car = new WWSphere();
		car.setPhantom(true);
		car.setMonolithic(true);
		car.setColor(SIDE_ALL, new WWColor(1, 1, 0.5f));
		car.setSize(1.25f, 1.25f, 2);
		car.setCutoutEnd(0.5f);
		car.setTaperX(0.25f);
		car.setTaperY(0.25f);
		car.setHollow(0.75f);
		car.setRotation(0, 90, -90);
		car.setPosition(0, -0.1f, -0.25f);
		car.setParent(avatar);
		addObject(car);
		car.setFixed(true);

		setLevel(0); // level is not used

		// reload the track
		preTrackObjectCount = lastObjectIndex;
		coasterSections = (ArrayList<CoasterSection>) clientModel.loadObject("coaster");
		if (coasterSections == null || coasterSections.size() == 0) {
			clear();
			coasterSections = new ArrayList<CoasterSection>();
			addStation();
		} else {
			CoasterSection lastSection = coasterSections.get(coasterSections.size() - 1);
			x = lastSection.x;
			y = lastSection.y;
			z = lastSection.z;
			direction = lastSection.direction;
			slope = lastSection.slope;
		}

		// build it
		renderTrack();

		worldBuildActions = new WWAction[] { new ClearAction(), new SaveAction(), new RideAction() };
		worldRideActions = new WWAction[] { new PauseAction(), new BuildAction(), new ChangeViewAction() };
		setWorldActions(worldBuildActions);
		avatarBuildActions = new WWAction[] { new MakeStraightAction(), new MakeCurveUpAction(), new MakeCurveDownAction(), new MakeLeftTurnAction(), new MakeRightTurnAction(), new UndoAction() };
		avatarRideActions = new WWAction[] {};
		setAvatarActions(avatarBuildActions);

		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = false;
//		AndroidClientModel.getClientModel().behindDistance = 100;
//		AndroidClientModel.getClientModel().behindTilt = 45;
		AndroidClientModel.getClientModel().setViewpoint(3);

	}

	class BuildAction extends WWAction {
		@Override
		public String getName() {
			return "Build";
		}

		@Override
		public void start() {
			startBuild();
		}
	}

	public void startBuild() {
		riding = false;
		setWorldActions(worldBuildActions);
		setAvatarActions(avatarBuildActions);
		clientModel.setViewpoint(3);
		showBannerAds();
	}

	class RideAction extends WWAction {
		@Override
		public String getName() {
			return "Ride";
		}

		@Override
		public void start() {
			avatarRidingBehavior.startRide();
		}
	}

	class AvatarRidingAnimation extends WWAnimation {

		@Override
		public void getAnimatedPosition(WWObject object, WWVector position, long time) {
			if (riding && object == carCore && rideSection < coasterSections.size() - 1) {
				CoasterSection coasterSection1 = coasterSections.get(rideSection);
				CoasterSection coasterSection2 = coasterSections.get(rideSection + 1);
				float d = rideTick - rideSection * 100 + (time - lastEventTime) * speed / 30.0f;
				float x = (coasterSection1.x * (100.0f - d) + coasterSection2.x * d) / 100.0f;
				float y = (coasterSection1.y * (100.0f - d) + coasterSection2.y * d) / 100.0f;
				float z = (coasterSection1.z * (100.0f - d) + coasterSection2.z * d) / 100.0f;
				position.set(x, y, z);
			}
		}

		@Override
		public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
			if (riding && object == carCore && rideSection < coasterSections.size() - 1) {
				CoasterSection coasterSection1 = coasterSections.get(rideSection);
				CoasterSection coasterSection2 = coasterSections.get(rideSection + 1);
				float d = rideTick - rideSection * 100 + (time - lastEventTime) * speed / 30.0f;
				float slope = (coasterSection1.slope * (100.0f - d) + coasterSection2.slope * d) / 100.0f;
				float direction = (coasterSection1.direction * (100.0f - d) + coasterSection2.direction * d) / 100.0f;
				rotation.set(slope, 0, direction);
			}
		}

		int rideTick;
		int rideSection;
		float speed;
		int timerCycle;
		int soundId;
		long lastEventTime;

		public void startRide() {
			hideBannerAds();
			rideTick = 0;
			speed = 1;
			riding = true;
			setWorldActions(worldRideActions);
			setAvatarActions(avatarRideActions);
			clientModel.setSelectedObject(avatar);
			clientModel.setCameraObject(avatar);
			clientModel.setViewpoint(0);
			rideSection = 0;
			CoasterSection coasterSection = coasterSections.get(rideSection);
			carCore.setPosition(coasterSection.x, coasterSection.y, coasterSection.z);
			carCore.setRotation(coasterSection.slope, 0, coasterSection.direction);
			clientModel.flashMessage("Please remain seated!", false);
			avatarRidingBehavior.setTimer(1000);
			lastEventTime = 0;
		}

		@Override
		public boolean timerEvent() {
			if (lastEventTime == 0) {
				playSound("beep", 0.25f, 1.5f);
				lastEventTime = RollerCoasterWorld.this.getWorldTime();
			}
			timerCycle++;
			if (riding) {
				rideTick += speed;
				rideSection = rideTick / 100;
				if (rideSection >= coasterSections.size() - 1) {
					riding = false;
					carCore.setVelocity(new WWVector(0, 0, 0));
					if (soundId != 0) {
						stopSound(soundId);
						soundId = 0;
					}
					playSound("winningSound", 0.25f);
					int rc = clientModel.alert("The ride is over!", new String[] { "Ride Again", "Build", "Quit" });
					if (rc == 0) {
						startRide();
					} else if (rc == 1) {
						startBuild();
					} else {
						clientModel.disconnect();
						return true;
					}
				} else {
					CoasterSection coasterSection1 = coasterSections.get(rideSection);
					CoasterSection coasterSection2 = coasterSections.get(rideSection + 1);
					float d = rideTick - rideSection * 100;
					float slope = (coasterSection1.slope * (100.0f - d) + coasterSection2.slope * d) / 100.0f;
					float slopeAdjust = FastMath.sin(slope * FastMath.TORADIAN);
					speed *= 0.998f; // friction
					speed -= speed * slopeAdjust / 30.0f;
					speed = FastMath.range(speed, 5.0f, 500.0f);
					if (timerCycle % 12 == 0) {
						if (speed > 11) {
							if (soundId == 0) {
								soundId = startSound("thruster", FastMath.min(1, speed / 100), FastMath.range(speed / 100, 0.6f, 1.9f));
							} else {
								adjustSound(soundId, FastMath.min(1, speed / 50), FastMath.range(speed / 50, 0.6f, 1.9f));
							}
						} else {
							if (soundId != 0) {
								stopSound(soundId);
								soundId = 0;
							}
							if (speed == 5.0f && slopeAdjust > 0.25f) {
								playSound("thud", slopeAdjust, 0.5f);
							}
						}
					}
					setTimer(1); // should fire every physics loop (15millis)
				}
			} else {
				if (soundId != 0) {
					stopSound(soundId);
					soundId = 0;
				}
			}
			lastEventTime = RollerCoasterWorld.this.getWorldTime();
			return true;
		}
	}

	class ClearAction extends WWAction {
		@Override
		public String getName() {
			return "Clear";
		}

		@Override
		public void start() {
			if (coasterSections.size() > 3) {
				int answer = clientModel.alert("Clear the entire track?", new String[] { "OK", "Cancel" });
				if (answer == 0) {
					clear();
					addStation();
					renderTrack();
					((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
					dirty = true;
				}
			}
		}
	}

	class UndoAction extends WWAction {
		@Override
		public String getName() {
			return "Undo";
		}

		@Override
		public void start() {
			if (coasterSections.size() > 3) {
				coasterSections.remove(coasterSections.size() - 1);
				coasterSections.remove(coasterSections.size() - 1);
				coasterSections.remove(coasterSections.size() - 1);
				CoasterSection lastSection = coasterSections.get(coasterSections.size() - 1);
				x = lastSection.x;
				y = lastSection.y;
				z = lastSection.z;
				direction = lastSection.direction;
				slope = lastSection.slope;
				for (int i = lastSection.objectCount + 1; i <= lastObjectIndex; i++) {
					WWObject object = objects[i];
					if (object != null) {
						removeObject(i);
					}
				}
				lastObjectIndex = lastSection.objectCount;
				clientModel.setSelectedObject(null);
				((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
				dirty = true;
			}
		}
	}

	class MakeStraightAction extends WWAction {
		@Override
		public String getName() {
			return "Straight";
		}

		@Override
		public void start() {
			addStraight(false);
			addStraight(true);
			TrackPiece piece = addStraight(false);
			clientModel.setSelectedObject(piece);
			dirty = true;
			((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	class MakeCurveUpAction extends WWAction {
		@Override
		public String getName() {
			return "Up";
		}

		@Override
		public void start() {
			addCurveUp(false);
			addCurveUp(true);
			TrackPiece piece = addCurveUp(false);
			clientModel.setSelectedObject(piece);
			dirty = true;
			((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	class MakeCurveDownAction extends WWAction {
		@Override
		public String getName() {
			return "Down";
		}

		@Override
		public void start() {
			addCurveDown(false);
			addCurveDown(true);
			TrackPiece piece = addCurveDown(false);
			clientModel.setSelectedObject(piece);
			dirty = true;
			((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	class MakeLeftTurnAction extends WWAction {
		@Override
		public String getName() {
			return "Left";
		}

		@Override
		public void start() {
			addLeftTurn(false);
			addLeftTurn(true);
			TrackPiece piece = addLeftTurn(false);
			clientModel.setSelectedObject(piece);
			dirty = true;
			((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	class MakeRightTurnAction extends WWAction {
		@Override
		public String getName() {
			return "Right";
		}

		@Override
		public void start() {
			addRightTurn(false);
			addRightTurn(true);
			TrackPiece piece = addRightTurn(false);
			clientModel.setSelectedObject(piece);
			dirty = true;
			((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	@Override
	public void clear() {
		coasterSections = new ArrayList<CoasterSection>();
		x = 0;
		y = 0;
		z = 0;
		direction = 0;
		slope = 0;
	}

	public void move(float turn, float tilt) {
		float d = SECTION_SIZE / 2.0f;
		float newx1 = d * FastMath.sin(-direction * FastMath.TORADIAN) * FastMath.cos(slope * FastMath.TORADIAN);
		float newy1 = d * FastMath.cos(direction * FastMath.TORADIAN) * FastMath.cos(slope * FastMath.TORADIAN);
		float newz1 = d * FastMath.sin(slope * FastMath.TORADIAN);
		x += newx1;
		y -= newy1;
		z += newz1;
		direction += turn;
		slope += tilt;
		float newx2 = d * FastMath.sin(-direction * FastMath.TORADIAN) * FastMath.cos(slope * FastMath.TORADIAN);
		float newy2 = d * FastMath.cos(direction * FastMath.TORADIAN) * FastMath.cos(slope * FastMath.TORADIAN);
		float newz2 = d * FastMath.sin(slope * FastMath.TORADIAN);
		x += newx2;
		y -= newy2;
		z += newz2;
	}

	public void addStation() {
		CoasterSection section = new CoasterSection(CoasterSection.SectionType.STATION, x, y, z, direction, slope);
		coasterSections.add(section);
		renderSection(section, false);
	}

	public TrackPiece addStraight(boolean addSupport) {
		move(0, 0);
		CoasterSection section = new CoasterSection(CoasterSection.SectionType.STRAIGHT, x, y, z, direction, slope);
		coasterSections.add(section);
		return renderSection(section, addSupport);
	}

	public TrackPiece addLeftTurn(boolean addSupport) {
		move(TURN_SIZE, 0);
		CoasterSection section = new CoasterSection(CoasterSection.SectionType.LEFT_TURN, x, y, z, direction, slope);
		coasterSections.add(section);
		return renderSection(section, addSupport);
	}

	public TrackPiece addRightTurn(boolean addSupport) {
		move(-TURN_SIZE, 0);
		CoasterSection section = new CoasterSection(CoasterSection.SectionType.RIGHT_TURN, x, y, z, direction, slope);
		coasterSections.add(section);
		return renderSection(section, addSupport);
	}

	public TrackPiece addCurveUp(boolean addSupport) {
		move(0, TURN_SIZE);
		CoasterSection section = new CoasterSection(CoasterSection.SectionType.CURVE_UP, x, y, z, direction, slope);
		coasterSections.add(section);
		return renderSection(section, addSupport);
	}

	public TrackPiece addCurveDown(boolean addSupport) {
		move(0, -TURN_SIZE);
		CoasterSection section = new CoasterSection(CoasterSection.SectionType.CURVE_DOWN, x, y, z, direction, slope);
		coasterSections.add(section);
		return renderSection(section, addSupport);
	}

	private TrackPiece renderTrack() {
		for (int i = preTrackObjectCount + 1; i <= lastObjectIndex; i++) {
			WWObject object = getObjects()[i];
			if (object != null) {
				this.removeObject(i);
			}
		}
		lastObjectIndex = preTrackObjectCount;
		TrackPiece lastPiece = null;
		for (int i = 0; i < coasterSections.size(); i++) {
			CoasterSection section = coasterSections.get(i);
			lastPiece = renderSection(section, i % 3 == 0);
		}
		return lastPiece;
	}

	private TrackPiece renderSection(CoasterSection section, boolean addSupport) {
		TrackPiece track = new TrackPiece(section, addSupport);
		track.setPosition(section.x, section.y, section.z);
		track.setRotation(section.slope, 0, section.direction);
		return track;
	}

	float smoothDeltaX;
	float smoothDeltaY;

	@Override
	public boolean controller(float deltaX, float deltaY) {
//		if (riding && clientModel.getViewpoint() == 1) {
//			smoothDeltaX = (3 * smoothDeltaX + deltaX) / 4.0f;
//			smoothDeltaY = (3 * smoothDeltaY + deltaY) / 4.0f;
//			clientModel.setCameraPan(-smoothDeltaX);
//			clientModel.setCameraTilt(smoothDeltaY);
//		}
		return true;
	}

	@Override
	public boolean usesAccelerometer() {
		return riding;
	}

	@Override
	public boolean usesController() {
		return riding;
	}

	class SaveAction extends WWAction {

		@Override
		public String getName() {
			return "Save";
		}

		@Override
		public void start() {
			int answer = clientModel.alert("Save the track?", new String[] { "OK", "Cancel" });
			if (answer == 0) {
				clientModel.saveObject(coasterSections, "coaster");
				dirty = false;
			}
		}
	}

	@Override
	public void save() {
		clientModel.saveObject(coasterSections, "coaster");
	}

	@Override
	public boolean needsSaving() {
		return dirty;
	}

	public boolean supportsOpenGLES20() {
		return false;
	}
}
