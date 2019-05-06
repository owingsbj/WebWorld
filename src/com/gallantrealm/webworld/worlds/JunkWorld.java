package com.gallantrealm.webworld.worlds;

import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
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

public class JunkWorld extends BaseEggWorld {

	WWSimpleShape avatar;
	int beaconsFound = 0;
	boolean flying = false;
	ArrayList<WWObject> junk = new ArrayList<WWObject>();
	
	

	public JunkWorld(String saveWorldFileName, String avatarName) {
		

		setName("Space Junk");
		setGravity(0.0f); // no gravity
		setSkyColor(new WWColor(0x000000));
		setAmbientLightIntensity(0.1f);

		setLevel(Math.max(1, AndroidClientModel.getClientModel().getScore(8)));

		WWSimpleShape space = new WWSphere();
		space.setSize(10000, 10000, 10000);
		space.setHollow(0.99f);
		space.setTextureURL(SIDE_ALL, "space");
		space.setTextureScaleX(SIDE_ALL, 0.1f);
		space.setTextureScaleY(SIDE_ALL, 0.1f);
		space.setFullBright(SIDE_ALL, true);
		addObject(space);

		WWSimpleShape tunnel = new WWCylinder();
		tunnel.setSize(10000, 10000, 10000);
		tunnel.setRotation(90, 0, 0);
		tunnel.setMonolithic(true);
		tunnel.setPenetratable(true);
		tunnel.setPhantom(true);
		tunnel.setTransparency(SIDE_ALL, 0.5f);
		tunnel.setTextureURL(SIDE_ALL, "redx");
		tunnel.setTextureScaleX(SIDE_ALL, 0.01f);
		tunnel.setTextureScaleY(SIDE_ALL, 0.001f);
		tunnel.setHollow(0.01f);
		addObject(tunnel);

		WWSimpleShape finish = new WWBox();
		finish.setSize(1000, 100, 1000);
		finish.setPosition(0, -1100, 0);
		addObject(finish);
		finish.setTransparency(SIDE_ALL, 1);
		finish.addBehavior(new FinishBehavior());

		makeJunk();

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		avatar.addBehavior(new AvatarBehavior());
		avatar.setPosition(0, 1000, 0);
		int avatarid = addObject(avatar);
		user.setAvatarId(avatarid);

		// space ship
		WWSimpleShape ship = new WWSphere();
		ship.setPhantom(true);
		ship.setMonolithic(true);
		ship.setSize(1.5f, 3, 4);
		ship.setCutoutEnd(0.5f);
		ship.setTaperX(0.25f);
		ship.setTaperY(0.25f);
		ship.setRotation(0, 90, -90);
		ship.setPosition(0, -0.5f, -0.25f);
		ship.setParent(avatar);
		addObject(ship);

		WWSimpleShape dome = new WWSphere();
		dome.setPhantom(true);
		dome.setMonolithic(true);
		dome.setSize(2, 2, 1.5f);
		dome.setPosition(0, 0, 0);
		dome.setTransparency(SIDE_ALL, 0.5f);
		dome.setParent(avatar);
		addObject(dome);

		setAvatarActions(new WWAction[] { new StartAction() });
	}

	public void makeJunk() {
		// generate the random objects
		JunkBehavior junkBehavior = new JunkBehavior();
		for (int i = 0; i < 25 * getLevel(); i++) {
			int type = (int) (5.0 * FastMath.random());
			WWSimpleShape object;
			if (type < 1) {
				object = new WWBox();
			} else if (type < 2) {
				object = new WWCylinder();
			} else if (type < 3) {
				object = new WWSphere();
			} else { // float the torus, as it is fun!
				object = new WWTorus();
			}
			object.setCircleVertices(6);
			object.setPenetratable(true);
			object.setFixed(true);
			object.setMonolithic(true);
			object.setImpactSound("wood");
			float depth = FastMath.random(0, 2000);
			object.setPosition(FastMath.random(-50, 50), 1000 - depth, FastMath.random(-50, 50));
			float overallSize = FastMath.random(2f + depth / 500, 10f + depth / 50, 2f);
			object.setSize(new WWVector(overallSize, overallSize, overallSize));
			object.setRotation(new WWVector(FastMath.random() * 360, FastMath.random() * 360, FastMath.random() * 360));
			object.setColor(WWSimpleShape.SIDE_ALL, new WWColor(FastMath.random(0.5f, 1.0f), FastMath.random(0.5f, 1.0f), FastMath.random(0.5f, 1.0f)));
			if (FastMath.random() > 0.25) {
				// Choose a random texture
				float texture = FastMath.random() * 5;
				if (texture < 1.0) {
					object.setTextureURL(WWSimpleShape.SIDE_ALL, "concrete");
				} else if (texture < 2.0) {
					object.setTextureURL(WWSimpleShape.SIDE_ALL, "wood");
				} else if (texture < 3.0) {
					object.setTextureURL(WWSimpleShape.SIDE_ALL, "stucco");
				} else if (texture < 4.0) {
					object.setTextureURL(WWSimpleShape.SIDE_ALL, "grass");
				} else {
					object.setTextureURL(WWSimpleShape.SIDE_ALL, "ivy");
				}
				object.setTextureScaleX(WWSimpleShape.SIDE_ALL, 10.0f / object.sizeX);
				object.setTextureScaleY(WWSimpleShape.SIDE_ALL, 10.0f / object.sizeY);
			}
			addObject(object);
			object.addBehavior(junkBehavior);
			junk.add(object);
		}
	}

	public void destroyJunk() {
		for (WWObject object : junk) {
			removeObject(object.getId());
		}
		junk.clear();
	}

	class StartAction extends WWAction {
		@Override
		public String getName() {
			return "Start";
		}

		@Override
		public void start() {
			AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint()); // to force view from viewpoint
			playSong("junk_music", 0.25f);
			flying = true;
			AndroidClientModel.getClientModel().forceAvatar(50, 0, 0, 0, 0, 0);
			setAvatarActions(new WWAction[0]);
			hideBannerAds();
		}

		@Override
		public void stop() {
		}
	}

	public void reset() {
		flying = false;
		AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0, 0);
		avatar.setVelocity(new WWVector(0, 0, 0));
		avatar.setPosition(new WWVector(0, 1000, 0));
		setAvatarActions(new WWAction[] { new StartAction() });
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

	public class BeaconBehavior extends WWBehavior {

		public WWObject prize;
		public WWObject prize2;

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			AndroidClientModel.getClientModel().flashMessage("You caught a beacon!", true);
			return true;
		}

		@Override
		public boolean timerEvent() {
			prize.setFullBright(SIDE_ALL, !prize.isFullBright(SIDE_ALL));
			prize2.setFullBright(SIDE_ALL, !prize2.isFullBright(SIDE_ALL));
			setTimer(1000);
			return true;
		}

	}

	public class JunkBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (flying && nearObject == avatar) {
				flying = false;
				stopPlayingSong();
				AndroidClientModel.getClientModel().vibrate(250);
				playSound("loosingSound", 0.25f);
				int rc = AndroidClientModel.getClientModel().alert("Sorry, you hit the junk!\nDo you want to play again?", new String[] { "Play Again", "Quit" });
				if (rc == 0) {
					showBannerAds();
					reset();
				}
				if (rc == 1) {
					AndroidClientModel.getClientModel().disconnect();
				}
			}
			return true;
		}

	}

	public class FinishBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (flying && nearObject == avatar) {
				flying = false;
				AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0, 0);
				avatar.setVelocity(new WWVector(0, 0, 0));
				stopPlayingSong();
				playSound("winningSound", 0.125f);
				if (AndroidClientModel.getClientModel().isPlayMusic()) {
					playSound("winningSound2", 0.1f);
				}
				int rc = AndroidClientModel.getClientModel().alert("Hurrah! You made it through the junk!!", new String[] { "Next Level", "Quit" });
				if (rc == 0) {
					destroyJunk();
					setLevel(getLevel() + 1);
					AndroidClientModel.getClientModel().setScore(8, getLevel());
					AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
					makeJunk();
					AndroidRenderer.clearRenderings();
					showBannerAds();
					reset();
				}
				if (rc == 1) {
					AndroidClientModel.getClientModel().disconnect();
				}
			}
			return true;
		}

	}

	float controllerX;
	float controllerY;

	@Override
	public boolean controller(float deltaX, float deltaY) {
		controllerX = deltaX;
		controllerY = deltaY;
		return true;
	}

	class AvatarBehavior extends WWBehavior {
		public AvatarBehavior() {
			setTimer(100);
		}

		@Override
		public boolean timerEvent() {
			if (flying) {
				float x = 1 * (-controllerX);
				float y = -2 * (-controllerY);
				WWVector avpos = avatar.getPosition();
				//float off = FastMath.sqrt(avpos.x * avpos.x + avpos.z * avpos.z);
				x = x - 0.2f * FastMath.sign(avpos.x) * avpos.x * avpos.x;
				y = y - 0.05f * FastMath.sign(avpos.z) * avpos.z * avpos.z;
				AndroidClientModel.getClientModel().forceAvatar(50, 0, y, 0, 0, x);
				JunkWorld.this.setStatus("Distance: " + (int) (1000 - avatar.getPosition().y));
				AndroidClientModel.getClientModel().fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
			} else {
				AndroidClientModel.getClientModel().forceAvatar(0, 0, 0, 0, 0, 0);
				JunkWorld.this.setStatus("");
			}
			setTimer(100);
			return true;
		}
	}
	
}
