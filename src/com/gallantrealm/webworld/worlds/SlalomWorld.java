package com.gallantrealm.webworld.worlds;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.android.StartWorldActivity;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.NewPhysicsThread;
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
import com.gallantrealm.myworld.model.WWTranslucency;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;
import android.content.Intent;

public class SlalomWorld extends BaseEggWorld {

	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	public long raceStartTime;
	public long raceFinishTime;
	int courseNumber;
	public int ngates;
	public int terrain;
	public int nobstacles;
	boolean racing;
	boolean raceStarted;
	WWBehavior groundBehavior;
	GateBehavior[] gateBehaviors;
	int slidingSoundId;
	float slidingSoundVolume;
	boolean catchingAir;
	WWObject avatar;

	public SlalomWorld(String saveWorldFileName, String avatarName) {
		this.courseNumber = 10;
		setLevel(Math.max(1, AndroidClientModel.getClientModel().getLevel(courseNumber)));
		this.ngates = 5 + 5 * Math.max(1, clientModel.getLevel(courseNumber));
		this.terrain = clientModel.getLevel(courseNumber);
		this.nobstacles = clientModel.getLevel(courseNumber);

		clientModel.behindTilt = 0;
		clientModel.initiallyFacingDistance = 2;

		worldActions = new WWAction[] { new PauseAction(), new ChangeViewAction() };

		// The basic world (like earth)
		setName("Slalom");
		setGravity(30); // for less bouncing //9.8f); // earth gravity
		setFogDensity(0.1f);
		long time = getWorldTime();
		setCreateTime(time);
		setLastModifyTime(time);

		// first user (created first for translucency reasons)
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		avatar.setFreedomMoveX(false);
		avatar.setFreedomMoveY(true);
		avatar.setFreedomMoveZ(true);
		avatar.setFreedomRotateX(true);
		avatar.setFreedomRotateY(false);
		avatar.setFreedomRotateZ(false);
		avatar.setFriction(0.1f);
		user.setAvatarId(avatar.getId());

		// the user's skis
		WWSimpleShape ski1 = new WWCylinder();
		ski1.setPhantom(true);
		ski1.setMonolithic(true);
		// board.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		ski1.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		ski1.setSize(new WWVector(0.1f, 2.0f, 0.05f));
		ski1.setPosition(new WWVector(-0.15f, 0, -0.5f));
		ski1.setSolid(false);
		ski1.setParent(avatar);
		addObject(ski1);
		WWSimpleShape ski2 = new WWCylinder();
		ski2.setPhantom(true);
		ski2.setMonolithic(true);
		// board.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x000000));
		ski2.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
		ski2.setSize(new WWVector(0.1f, 2.0f, 0.05f));
		ski2.setPosition(new WWVector(0.15f, 0, -0.5f));
		ski2.setSolid(false);
		ski2.setParent(avatar);
		addObject(ski2);

		// the ground
		WWMesh ground = new WWMesh(); // for the ground
		ground.setElasticity(-1);
		ground.setName("ground");
		// ground.setImpactSound("grass");
		// ground.setSlidingSound("movingGrass");
		ground.setSize(new WWVector(1000, 1000, 1000));
		ground.setPosition(new WWVector(0, 0, -10));
		int meshSize = 100;
		ground.setMeshSize(meshSize, meshSize);

		// - create slope, curving inward to opposite corner
		for (int i = 0; i <= meshSize; i++) {
			for (int j = 0; j <= meshSize; j++) {
				float z = (i + j) * (i + j) / 80000f + 0.5f;
				ground.setMeshPoint(i, j, z);
			}
		}

		// - add a few gorges
		for (int i = 0; i < 250; i++) {
			int x = (int) (FastMath.random(meshSize / 4, meshSize - 2));
			int y = (int) (FastMath.random(meshSize / 4, meshSize - 2));
			float z = -FastMath.random() * 0.0002f * (x * x + y * y) / meshSize / meshSize * terrain;
			int baseSize = (int) FastMath.random(2, 4);
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

		// dunk any low areas to make ice clifs
		for (int i = 0; i <= meshSize; i++) {
			for (int j = 0; j <= meshSize; j++) {
				float p = ground.getMeshPoint(i, j);
				if (p < 0.51) {
					ground.setMeshPoint(i, j, p - 0.05f);
				}
			}
		}

		// raise sides
		for (int i = 0; i <= meshSize; i++) {
			for (int j = 0; j <= meshSize; j++) {
				if (i == 0 || i == meshSize || j == 0 || j == meshSize) {
					float x = ground.getMeshPoint(i, j);
					ground.setMeshPoint(i, j, x + 0.1f);
				}
			}
		}

		// lift the back for a platform
		ground.setMeshPoint(meshSize - 1, meshSize - 2, ground.getMeshPoint(meshSize - 2, meshSize - 2));
		ground.setMeshPoint(meshSize - 2, meshSize - 1, ground.getMeshPoint(meshSize - 2, meshSize - 2));
		ground.setMeshPoint(meshSize - 1, meshSize - 1, ground.getMeshPoint(meshSize - 2, meshSize - 2));

		ground.setTextureURL(WWSimpleShape.SIDE_TOP, "ice");
		ground.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.1f);
		ground.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.1f);
		addObject(ground);

		groundBehavior = new GroundBehavior();
		ground.addBehavior(groundBehavior);
		groundBehavior.setTimer(100);

		// the water
		WWTranslucency water = new WWTranslucency();
		water.setName("water");
		water.setPenetratable(true);
		water.setInsideLayerDensity(0.25f);
		water.setPosition(new WWVector(0, 0, -50));
		water.setSize(new WWVector(1500, 1500, 100));
		water.setSolid(false);
		water.setDensity(0.25f);
		water.setFriction(0.5f);
		water.setElasticity(-0.5f);
		water.setImpactSound("water");
		water.setSlidingSound("movingWater");
		water.setInsideColor(0x202040);
		water.setInsideTransparency(0.7f);
		water.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x8080F0));
		water.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x8080F0));
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
		water.setFullBright(SIDE_INSIDE1, true);
		water.setFullBright(SIDE_INSIDE2, true);
		water.setFullBright(SIDE_INSIDE3, true);
		water.setFullBright(SIDE_INSIDE4, true);
		addObject(water);
		water.addBehavior(new WaterBehavior());

		// the sky
		WWSimpleShape sky = new WWSphere();
		sky.setName("sky");
		sky.setPenetratable(true);
		sky.setTransparency(SIDE_ALL, 0.01f); // keeps from generating shadows
		sky.setPosition(new WWVector(0, 0, -800));
		sky.setSize(new WWVector(3000, 5000, 5000));
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
		
		// Position the sun a little better
		setSunDirection(new WWVector(-2, -1, 1));

		setAvatarActions(new WWAction[] { new RaceAction() });

		// Add obstacles
		for (int n = 0; n < nobstacles; n++) {
			WWSimpleShape obstacle;

			obstacle = new WWSphere();
			obstacle.setMonolithic(true);
			obstacle.setSolid(true);
			obstacle.setTextureURL(SIDE_ALL, "stucco");
			obstacle.setSize(FastMath.random(6, 24), FastMath.random(6, 24), FastMath.random(6, 24));
			obstacle.setRotation(FastMath.random(0, 360), FastMath.random(0, 360), FastMath.random(0, 360));
			obstacle.setCircleVertices((int) FastMath.random(3, 7));

			float p = FastMath.random(meshSize / 4, meshSize - 6);
			int i = (int) FastMath.random(p - 6, p + 6);
			int j = (int) FastMath.random(p - 6, p + 6);
			float x = i * 1000 / meshSize - 500;
			float y = j * 1000 / meshSize - 500;
			float z = ground.getMeshPoint(i, j) * 1000 - 500 - 11;
			obstacle.setPosition(x, y, z);
			addObject(obstacle);
		}

		// Add gates
		gateBehaviors = new GateBehavior[ngates];
		for (int n = 0; n < ngates; n++) {
			WWSimpleShape gate = new WWBox();
			gate.setSize(50, 1, 25);
			gate.setRotation(0, 0, 45);
			gate.setSolid(false);
			gate.setTransparency(SIDE_ALL, 1);
			gate.setTransparency(SIDE_SIDE3, 0.01f);
			gate.setTransparency(SIDE_SIDE1, 0.01f);
			int i = n * (meshSize - 25) / ngates + 25 - 4;
			int j = n * (meshSize - 25) / ngates + 25 - 4;
			int offset = (int) FastMath.random(2, 4);
			if (n % 2 == 0) {
				gate.setTextureURL(SIDE_SIDE3, "slalomgatel");
				gate.setTextureURL(SIDE_SIDE1, "slalomgatel");
				gate.setTextureScaleX(SIDE_SIDE1, -1);
				gate.setFullBright(SIDE_SIDE3, true);
				i -= offset;
				j += offset;
			} else {
				gate.setTextureURL(SIDE_SIDE3, "slalomgater");
				gate.setTextureURL(SIDE_SIDE1, "slalomgater");
				gate.setTextureScaleX(SIDE_SIDE1, -1);
				gate.setFullBright(SIDE_SIDE3, true);
				i += offset;
				j -= offset;
			}
			i = Math.min(Math.max(i, 2), meshSize - 2);
			j = Math.min(Math.max(j, 2), meshSize - 2);
			float x = i * 1000 / meshSize - 500;
			float y = j * 1000 / meshSize - 500;
			float z = (ground.getMeshPoint(i, j) + ground.getMeshPoint(i + 1, j - 1) + ground.getMeshPoint(i - 1, j + 1)) / 3 * 1000 - 500 - 12;
			// float z = (i + j) * (i + j) / 1000 - 500 - 12;
			gate.setPosition(x, y, z);
			addObject(gate);
			gateBehaviors[n] = new GateBehavior();
			gate.addBehavior(gateBehaviors[n]);
		}

		// Start Gate
		WWSimpleShape startGate = new WWBox();
		startGate.setSize(10, 1, 10);
		startGate.setRotation(0, 0, 45);
		startGate.setSolid(false);
		startGate.setTransparency(SIDE_ALL, 1);
		startGate.setTransparency(SIDE_SIDE3, 0.01f);
		startGate.setTransparency(SIDE_SIDE1, 0.01f);
		startGate.setTextureURL(SIDE_SIDE3, "slalom_start_gate");
		startGate.setFullBright(SIDE_SIDE3, true);
		startGate.setTextureURL(SIDE_SIDE1, "slalom_start_gate");
		float x = 98 * 1000 / meshSize - 500;
		float y = 98 * 1000 / meshSize - 500;
		float z = ground.getMeshPoint(98, 98) * 1000 - 500 - 7;
		startGate.setPosition(x, y, z);
		addObject(startGate);

		// Finish Gate
		WWSimpleShape finishGate = new WWBox();
		finishGate.setSize(20, 1, 15);
		finishGate.setRotation(0, 0, 45);
		finishGate.setSolid(false);
		finishGate.setTransparency(SIDE_ALL, 1);
		finishGate.setTransparency(SIDE_SIDE3, 0.01f);
		finishGate.setTransparency(SIDE_SIDE1, 0.01f);
		finishGate.setTextureURL(SIDE_SIDE3, "slalom_finish_gate");
		finishGate.setFullBright(SIDE_SIDE3, true);
		finishGate.setTextureURL(SIDE_SIDE1, "slalom_finish_gate");
		x = 15 * 1000 / meshSize - 500;
		y = 15 * 1000 / meshSize - 500;
		z = ground.getMeshPoint(15, 15) * 1000 - 500 - 7;
		finishGate.setPosition(x, y, z);
		addObject(finishGate);
		finishGate.addBehavior(new FinishLineBehavior());

		resetRace();
	}

	public class RaceAction extends WWAction {

		@Override
		public String getName() {
			return "Slide";
		}

		@Override
		public void start() {
			hideBannerAds();
			clientModel.behindTilt = 40;
			clientModel.setViewpoint(clientModel.getViewpoint());
			// avatar.setToSlide();
			avatar.setPosition(new WWVector(480.5f, 480.5f, avatar.getPosition().z));
			clientModel.calibrateSensors();
			countDown(3);
			setAvatarActions(new WWAction[] {});
		}
	}

	void countDown(final int secs) {
		playSound("beep", 0.05f, 0.75f);
		clientModel.flashMessage("" + secs, true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (secs > 1) {
					countDown(secs - 1);
				} else {
					playSound("beep", 0.05f, 1f);
					clientModel.flashMessage("Go", true);
					startRacing();
				}
			}
		}, 1000);
	}

	public void startRacing() {
		// avatar.slide(10);
		clientModel.behindTilt = 2;
		clientModel.cameraDampRate = 5;
		raceStartTime = this.getWorldTime();
		racing = true;
		raceStarted = false;
		updateAvatarActions();
		playSong("slalom_music", 0.125f);
		slidingSoundVolume = 0.1f;
		slidingSoundId = startSound("movingGrass", slidingSoundVolume, 1);
	}

	public void stopRacing() {
		clientModel.cameraDampRate = 1;
		stopPlayingSong();
		stopSound(slidingSoundId);
		stopAllSounds();
		racing = false;
	}

	public void resetRace() {
		showBannerAds();
		// avatar.stand();
		stopRacing();
		AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0);
		avatar.setPosition(new WWVector(483, 483, 480));
		avatar.setRotation(new WWVector(0, 0, 45));
		avatar.setVelocity(new WWVector(0, 0, 0));
		avatar.setAMomentum(new WWVector(0, 0, 0));
		// avatar.stand();
		for (int i = 0; i < gateBehaviors.length; i++) {
			gateBehaviors[i].gateCleared = false;
		}
		updateAvatarActions();
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	public void updateAvatarActions() {
		WWAction[] avatarActions;
		if (!racing) {
			avatarActions = new WWAction[] { new RaceAction() };
		} else {
			avatarActions = new WWAction[] {};
		}
		setAvatarActions(avatarActions);
	}

	public int nGatesCleared() {
		int gatesCleared = 0;
		for (int i = 0; i < gateBehaviors.length; i++) {
			if (gateBehaviors[i].gateCleared) {
				gatesCleared += 1;
			}
		}
		return gatesCleared;
	}

	public class GroundBehavior extends WWBehavior {

		float impactVolume;

		@Override
		public boolean timerEvent() {
			if (SlalomWorld.this.racing) {
				slidingSoundId = adjustSound(slidingSoundId, slidingSoundVolume, 1);
				slidingSoundVolume *= 0.5f;
				if (catchingAir) {
					impactVolume += 0.1f;
				}
				long millis = SlalomWorld.this.getWorldTime() - SlalomWorld.this.raceStartTime;
				String timeString = formatTime(millis);
				SlalomWorld.this.setStatus("Time: " + timeString + "  Gate: " + nGatesCleared() + " of " + ngates);
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
			} else {
				String timeString = formatTime(0);
				SlalomWorld.this.setStatus("Time: " + timeString + "  Gate: 0 of " + ngates);
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
			}
			setTimer(100);
			return true;
		}

		public String formatTime(long millis) {
			Date time = new Date(millis);
			SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.S");
			String timeString = sdf.format(time);
			if (timeString.endsWith(".")) {
				timeString += "0";
			}
			timeString = timeString.substring(0, 7);
			return timeString;
		}

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			catchingAir = false;
			adjustEgg(nearObject, proximity);
			float collideVol = Math.min(1, impactVolume);
			impactVolume = 0;
			float curvingVol = FastMath.abs(nearObject.getAMomentum().z) * nearObject.getVelocity().length() / 20000;
			slidingSoundVolume = FastMath.max(slidingSoundVolume, collideVol + curvingVol);
			return true;
		}

		@Override
		public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			adjustEgg(nearObject, proximity);
			float curvingVol = FastMath.abs(nearObject.getAMomentum().z) * nearObject.getVelocity().length() / 20000;
			slidingSoundVolume = FastMath.max(slidingSoundVolume, curvingVol);
			return true;
		}

		@Override
		public boolean stopSlideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			catchingAir = true;
			return true;
		}

		private void adjustEgg(WWObject egg, WWVector proximity) {
			if (racing) {
				WWVector penguinRotation = egg.getRotation();
				WWVector proxnorm = proximity.normalize();
				float proxlen = (float) Math.sqrt(proxnorm.x * proxnorm.x + proxnorm.y * proxnorm.y);
				float bowlangle = (float) Math.atan2(proxnorm.y, proxnorm.x);
				float tilt = -FastMath.sin(bowlangle + FastMath.TORADIAN * penguinRotation.z) * proxlen * 1.5f;
				float lean = FastMath.cos(bowlangle + FastMath.TORADIAN * penguinRotation.z) * proxlen * 1.5f;
				egg.setRotation(FastMath.TODEGREES * tilt, FastMath.TODEGREES * lean, penguinRotation.z);
				if (egg.getVelocity().length() < 3 && egg.getAMomentum().length() < 10) {
					clientModel.flashMessage("Turn to accelerate", false);
				}
			}
		}

	}

	public class WaterBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (racing) {
				stopRacing();
				playSound("loosingSound", 0.25f);
				int rc = clientModel.alert("Oops!", "You missed the finish gate.", new String[] { "Race Again", "Quit" }, null);
				if (rc == 0) {
					resetRace();
				} else {
					clientModel.disconnect();
				}
			}
			return true;
		}

	}

	public class GateBehavior extends WWBehavior {

		public boolean gateCleared;

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (nearObject == avatar) {
				if (!gateCleared) {
					gateCleared = true;
					SlalomWorld.this.playSound("success", 0.04f);
				}
			}
			return true;
		}

	}

	public class FinishLineBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (nearObject == avatar) {
				long raceFinishTime = this.getWorldTime();
				stopRacing();
				long millis = raceFinishTime - SlalomWorld.this.raceStartTime;
				String timeString = formatTime(millis);
				int time = (int) millis;
				int rc;
				if (nGatesCleared() >= ngates) {
					playSound("winningSound", 0.075f);
					if (clientModel.isPlayMusic()) {
						playSound("winningSound2", 0.1f);
					}
					if (clientModel.getTime(courseNumber) == 0 || time < clientModel.getTime(courseNumber)) {
						clientModel.setTime(courseNumber, time);
						clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
						rc = clientModel.alert("Finished!", "You cleared all the gates.\nYou're time was: " + timeString + "\nThat's a new record!!\nDo you want to move up a level?", new String[] { "Next level", "This level", "Quit" },
								null);
					} else {
						rc = clientModel.alert("Finished!", "You cleared all the gates.\nYou're time was: " + timeString + "\nDo you want to move up a level?", new String[] { "Next level", "This level", "Quit" }, null);
					}
					if (rc == 0) {
						clientModel.setLevel(courseNumber, Math.max(1, clientModel.getLevel(courseNumber)) + 1);
						setLevel(clientModel.getLevel(courseNumber));
						clientModel.disconnect();
						clientModel.setWorldName("com.gallantrealm.eggworld.worlds.SlalomWorld");
						Intent intent = new Intent(clientModel.getContext(), StartWorldActivity.class);
						clientModel.getContext().startActivity(intent);
					} else if (rc == 1) {
						resetRace();
					} else {
						clientModel.disconnect();
					}
				} else {
					playSound("loosingSound", 0.05f);
					if (ngates - nGatesCleared() == 1) {
						rc = clientModel.alert("Sorry", "You missed a gate.", new String[] { "Race Again", "Quit" }, null);
					} else {
						rc = clientModel.alert("Sorry", "You missed " + (ngates - nGatesCleared()) + " gates.", new String[] { "Race Again", "Quit" }, null);
					}
					if (rc == 0) {
						resetRace();
					} else {
						clientModel.disconnect();
					}
				}
			}
			return true;
		}
	}

	public static String formatTime(long millis) {
		Date time = new Date(millis);
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.S");
		String timeString = sdf.format(time);
		if (timeString.endsWith(".")) {
			timeString += "0";
		}
		timeString = timeString.substring(0, 7);
		return timeString;
	}

	@Override
	public boolean controller(float x, float y) {
		float turning = 1 + getLevel() * 0.1f;
		float speed = 1 + getLevel() * 0.5f;
		if (avatar != null) {
			if (racing) {
				if (avatar.getPosition().z < 0) { // swim
					AndroidClientModel.getClientModel().forceAvatar(10, x * 2.5f * turning, -(y + 50) * 1, 0, 0);
				} else { // slide
					if (avatar.getVelocity().length() < 20 + 2 * speed) {
						AndroidClientModel.getClientModel().forceAvatar(FastMath.abs(x / 2) * speed, x * 2.5f * turning, -5, 0, 0);
					} else {
						AndroidClientModel.getClientModel().forceAvatar(0, x * 2.5f * turning, -5, 0, 0);
					}
				}
			} else { // just walking
				// AndroidClientModel.getClientModel().forceAvatar(y / 5, x, 0, 0, 0);
			}
		}
		return true;
	}

	@Override
	public boolean dampenCamera() {
		return false;
	}

	@Override
	public PhysicsThread makePhysicsThread() {
		return new NewPhysicsThread(this, 15, 2);
	}

	@Override
	public boolean supportsOpenGLES20() {
		return false;
	}

}
