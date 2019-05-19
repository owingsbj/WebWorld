package com.gallantrealm.webworld.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.Properties;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;
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
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;

public class World extends WWWorld {

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

	public World() {
		super(true, true, null, 15, true);
		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = true;
		AndroidClientModel.getClientModel().cameraDampRate = 0;
		AndroidClientModel.getClientModel().calibrateSensors();
		AndroidClientModel.getClientModel().cameraDampRate = 0.5f;
		AndroidClientModel.getClientModel().behindDistance = 3;
		AndroidClientModel.getClientModel().behindTilt = 10;

		runScripts(null);
	}

	public World(String saveWorldFileName, String avatarName) {
		super(true, true, saveWorldFileName, 15, true);
		AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = true;
		AndroidClientModel.getClientModel().cameraDampRate = 0;
		AndroidClientModel.getClientModel().calibrateSensors();
		AndroidClientModel.getClientModel().cameraDampRate = 0.5f;
// TODO		AndroidClientModel.getClientModel().dampCameraTranslations = true;
		AndroidClientModel.getClientModel().behindDistance = 3;
		AndroidClientModel.getClientModel().behindTilt = 10;

		runScripts(avatarName);
	}

	private void runScripts(String avatarName) {
		AndroidClientModel clientModel = AndroidClientModel.getClientModel();
		String worldName = clientModel.getWorldName();

		// create toplevel scope
		System.out.println("Creating toplevel scope");
		Context cx = Context.enter();
		cx.setOptimizationLevel(-1);
		TopLevel scope = new ImporterTopLevel(cx);
		cx.initStandardObjects(scope, false);
		cx.evaluateString(scope, "importPackage(Packages.com.gallantrealm.webworld.model);", "<importPackage>", 1, null);

		// create the avatar
		System.out.println("Opening avatar " + avatarName);
		WWObject avatar = null;
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		String avatarScriptName = avatarName + ".avatar";
		try {
			InputStream inStream = clientModel.loadFile(avatarScriptName, false);
			if (inStream == null) {
				System.err.println("Script "+avatarScriptName+" could not  be found");
			} else {
				Reader reader = new InputStreamReader(inStream, "UTF-8");

				// Run the world script
				System.out.println("Running script "+avatarScriptName);
				try {
					Object result = cx.evaluateReader(scope, reader, avatarScriptName, 1, null);
					NativeJavaObject avatarWrapped = (NativeJavaObject) ScriptableObject.getProperty(scope, "avatar");
					if (avatarWrapped != null) {
						avatar = (WWObject) (avatarWrapped.unwrap());
					}
				} catch (Exception e) {
					System.err.println("Script failed: " + e.getMessage());
				}
				inStream.close();
			}
			if (avatar == null) {
				System.err.println("Script didn't set an object to use for an avatar, using a box");
				avatar = new WWBox();
				avatar.setPhysical(true);
				avatar.setDensity(0.1f);
				avatar.setFreedomMoveZ(true);
				avatar.setFreedomRotateX(false);
				avatar.setFreedomRotateY(false);
				avatar.setFreedomRotateZ(true);
			}
			this.addObject(avatar);
			user.setAvatarId(avatar.getId());
		} catch (Exception e) {
			e.printStackTrace();
			clientModel.showMessage("Couldn't create the avatar " + avatarName);
		}

		// add a behavior to the avatar so it is facing user for a while
		if (AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar) {
			WWBehavior avatarCameraBehavior = new WWBehavior() {
				@Override
				public boolean timerEvent() {
					AndroidClientModel.getClientModel().setViewpoint(AndroidClientModel.getClientModel().getViewpoint());
					return true;
				}
			};
			avatar.addBehavior(avatarCameraBehavior);
			avatarCameraBehavior.setTimer(5000);
		}

		// run the first world script
		System.out.println("Opening world "+worldName);
		Properties worldProps = new Properties();
		try {
			worldProps.load(clientModel.getContext().getAssets().open("worlds/" + worldName + "/world.properties"));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		String worldScriptName = "worlds/"+worldName+"/"+worldProps.getProperty("script", "world.js");
		try {
			System.out.println("Running script "+worldScriptName);
			InputStream inStream = clientModel.loadFile(worldScriptName, false);
			Reader reader = new InputStreamReader(inStream, "UTF-8");

			// make sure the world, some constants, and avatar are available to the world script
			Object wrappedWorld = Context.javaToJS(this, scope);
			ScriptableObject.putProperty(scope, "world", wrappedWorld);
			ScriptableObject.putProperty(scope, "SIDE_ALL", 0);
			ScriptableObject.putProperty(scope, "SIDE_TOP", 1);
			ScriptableObject.putProperty(scope, "SIDE_BOTTOM", 2);
			ScriptableObject.putProperty(scope, "SIDE_SIDE1", 3);
			ScriptableObject.putProperty(scope, "SIDE_SIDE2", 4);
			ScriptableObject.putProperty(scope, "SIDE_SIDE3", 5);
			ScriptableObject.putProperty(scope, "SIDE_SIDE4", 6);
			ScriptableObject.putProperty(scope, "SIDE_INSIDE_TOP", 7);
			ScriptableObject.putProperty(scope, "SIDE_INSIDE_BOTTOM", 8);
			ScriptableObject.putProperty(scope, "SIDE_INSIDE1", 9);
			ScriptableObject.putProperty(scope, "SIDE_INSIDE2", 10);
			ScriptableObject.putProperty(scope, "SIDE_INSIDE3", 11);
			ScriptableObject.putProperty(scope, "SIDE_INSIDE4", 12);
			ScriptableObject.putProperty(scope, "SIDE_CUTOUT1", 13);
			ScriptableObject.putProperty(scope, "SIDE_CUTOUT2", 14);
			Object wrappedAvatar = Context.javaToJS(avatar, scope);
			ScriptableObject.putProperty(scope, "avatar", wrappedAvatar);

			try {
				Object result = cx.evaluateReader(scope, reader, worldScriptName, 1, null);
				clientModel.alert("Script ran.  Result is: " + cx.toString(result), null);
			} catch (Exception e) {
				System.err.println("Script failed: " + e.getMessage());
			}
			inStream.close();

		} catch (Exception e) {
			e.printStackTrace();
			clientModel.showMessage("Couldn't load script " + worldScriptName);
		} finally {
			Context.exit();
		}

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
		avatar.setTextureURL(WWSimpleShape.SIDE_ALL, avatarName + "_skin");
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

	public PhysicsThread makePhysicsThread() {
		return new OldPhysicsThread(this, 10);
	}

	@Override
	public boolean dampenCamera() {
		return AndroidClientModel.getClientModel().getViewpoint() != 1;
	}

}
