package com.gallantrealm.webworld.worlds;

import java.util.ArrayList;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

/**
 * Trapped in moving shapes, egg needs to get out!
 * 
 * @author owingsbj
 * 
 */
public class EscapeWorld extends BaseWebWorld {

	WWSimpleShape avatar;
	ArrayList<WWCylinder> rings;
	WWCylinder finish;
	boolean playing;

	public EscapeWorld(String saveWorldFileName, String avatarName) {

		// create a world, add ground, some objects, and one "egg" user
		setName("Escape");
		setGravity(9.8f); // earth gravity

		// the ground
		WWBox ground = new WWBox(); // for the ground
		ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x008000)); // green, like grass
		ground.setPosition(new WWVector(0, 0, -50));
		ground.setSize(new WWVector(1000, 1000, 100));
		ground.setTextureURL(WWSimpleShape.SIDE_ALL, "grass");
		ground.setTextureScaleX(WWSimpleShape.SIDE_ALL, 0.01f);
		ground.setTextureScaleY(WWSimpleShape.SIDE_ALL, 0.01f);
		addObject(ground);

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		user.setAvatarId(avatar.getId());

		if (getLevel() == 0) {
			setLevel(1);
		}

		finish = new WWCylinder();
		finish.setCircleVertices(64);
		finish.setMonolithic(true);
		finish.setPenetratable(true);
		finish.setHollow(0.999f);
		finish.setTextureURL(SIDE_ALL, "finish");
		finish.setTextureScaleX(SIDE_ALL, 0.1f);
		addObject(finish);
		finish.addBehavior(new FinishBehavior());

		buildRings(getLevel());

		resetAvatar();
	}

	class FinishBehavior extends WWBehavior {

		@Override
		public synchronized boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			if (playing) {
				playing = false;
				stopPlayingSong();
				playSound("winningSound", 0.125f);
				playSound("winningSound2", 0.1f);
				AndroidClientModel clientModel = AndroidClientModel.getClientModel();
				int rc = clientModel.alert("Congratulations, you made it!!", new String[] { "Next Level", "Redo this Level" });
				destroyRings();
				if (rc == 0) {
					setLevel(getLevel() + 1);
					clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
				}
				buildRings(getLevel());
				resetAvatar();
			}
			return true;
		}

	}

	private void resetAvatar() {
		avatar.setThrust(new WWVector(0, 0, 0));
		avatar.setVelocity(new WWVector(0, 0, 0));
		avatar.setPosition(new WWVector(0, 0, 0));
		avatar.setRotation(new WWVector(0, 0, 0));
		playSong("escape_music", 0.7f);
		playing = true;
	}

	private void destroyRings() {
		for (int i = 0; i < rings.size(); i++) {
			removeObject(rings.get(i).getId());
		}
		AndroidRenderer.clearRenderings();
	}

	private void buildRings(int level) {
		rings = new ArrayList<WWCylinder>();
		int nrings = 10 * level;
		for (int i = 1; i <= nrings; i++) {
			WWCylinder object = new WWCylinder();
			object.setCircleVertices(64);
			object.setMonolithic(true);
			object.setPenetratable(true);
			object.setHollow(0.9f + i / 1000.0f);
			object.setSize(10 * i, 10 * i, 3 * i);
			object.setAMomentum(new WWVector(FastMath.random(-20, 20), FastMath.random(-20, 20), FastMath.random(-20, 20)));
			object.setColor(WWSimpleShape.SIDE_ALL, new WWColor((int) (FastMath.random() * 0xffffff)));
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
			object.setImpactSound("concrete");
			object.setSlidingSound("carScrape");
			addObject(object);
			rings.add(object);
		}

		finish.setSize(110 * getLevel(), 110 * getLevel(), 10 * getLevel());
		finish.setPosition(0, 0, 5 * getLevel());
	}

}
