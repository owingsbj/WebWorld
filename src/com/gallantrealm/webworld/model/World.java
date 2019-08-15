package com.gallantrealm.webworld.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.MessageDialog;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.android.QuitAction;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWWorld;

public class World extends WWWorld {
	private static final long serialVersionUID = 1L;

	public static boolean runningAvatarScript; // side effect: used by Texture to properly prefix urls
	public static boolean runningLocalAvatarScript; // another side effect: used by Texture to decide to load local file
	public static boolean runningLocalWorldScript; // another side effect: used by Texture to decide to load local file

	Properties worldProperties;
	Properties avatarProperties;
	TopLevel scope;
	Function onRestored;
	String moveType;

	transient float thrust;
	transient float torque;
	transient float lift;
	transient float lean;
	transient float tilt;

	transient AndroidClientModel clientModel;
	
	public class ChangeViewAction extends WWAction implements Serializable {
		private static final long serialVersionUID = 1L;

		private int view;

		@Override
		public String getName() {
			return "View";
		}

		@Override
		public void start() {
			view++;
			if (view == 1) {
				clientModel.setViewpoint(1);
			} else if (view == 2) {
				clientModel.birdsEyeHeight = 15;
				clientModel.setViewpoint(2);
			} else if (view == 3) {
				clientModel.setViewpoint(3);
			} else {
				clientModel.setViewpoint(0);
				view = 0;
			}
		}

	};

	public World() throws Exception {
		super(true, true, null, 15, true);
		initializeWorld();
	}

	public World(String saveWorldFileName, String avatarName) throws Exception {
		super(true, true, saveWorldFileName, 15, true);
		initializeWorld();
	}

	private void initializeWorld() throws Exception {
		clientModel = AndroidClientModel.getClientModel();
		avatarProperties = getAvatarProperties(clientModel.getAvatarName());
		worldProperties = getWorldProperties(clientModel.getWorldName());
		setName(worldProperties.getProperty("name"));
		if (!clientModel.isCustomizeMode()) {
			setActions(new WWAction[] { new PauseAction(), new ChangeViewAction(), new QuitAction() });
		} else {
			setActions(new WWAction[] {new QuitAction()});
		}

		// TODO make this work in BlockWorld.js clientModel.cameraInitiallyFacingAvatar = true;
		clientModel.cameraDampRate = 0;
		clientModel.calibrateSensors();
		clientModel.cameraDampRate = 0.5f;
		clientModel.behindDistance = 3;
		clientModel.behindTilt = 10;
		clientModel.minCameraDistance = 0.01f;

		runScripts();

		clientModel.setSelectedObject(clientModel.getAvatar());
	}

	private Properties getAvatarProperties(final String avatarName) {
		Properties properties = new Properties();
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			// First try file
			File file = new File(clientModel.getLocalFolder() + "/avatars/" + avatarName + "/avatar.properties");
			System.out.println(">> " + file);
			if (file.exists()) {
				inputStream = new FileInputStream(file);
			} else {
				// Then try gallantrealm.com
				URL url = new URL(clientModel.getGallantUrl() + "/webworld/avatars/" + avatarName + "/avatar.properties");
				System.out.println(">> " + url);
				connection = (HttpURLConnection) (url.openConnection());
				inputStream = connection.getInputStream();
			}
			properties.load(inputStream);
			properties.list(System.out);
			System.out.println();
		} catch (IOException e) {
			System.err.println(e);
			clientModel.getContext().runOnUiThread(new Runnable() {
				public void run() {
					final MessageDialog messageDialog = new MessageDialog(clientModel.getContext(), null, "Couldn't download avatar " + avatarName + ".  Are you connected to the internet?", new String[] { "OK" }, null);
					messageDialog.show();
				}
			});
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return properties;
	}

	private Properties getWorldProperties(final String worldName) {
		Properties properties = new Properties();
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			// First try local
			File file = new File(clientModel.getLocalFolder() + "/worlds/" + worldName + "/world.properties");
			System.out.println(">> " + file);
			if (file.exists()) {
				inputStream = new FileInputStream(file);
			} else {
				// Then try gallantrealm.com
				URL url = new URL(clientModel.getGallantUrl() + "/webworld/worlds/" + worldName + "/world.properties");
				System.out.println(">> " + url);
				connection = (HttpURLConnection) (url.openConnection());
				inputStream = connection.getInputStream();
			}
			properties.load(inputStream);
			properties.list(System.out);
			System.out.println();
		} catch (IOException e) {
			System.err.println(e);
			clientModel.getContext().runOnUiThread(new Runnable() {
				public void run() {
					final MessageDialog messageDialog = new MessageDialog(clientModel.getContext(), null, "Couldn't download world " + worldName + ".  Are you connected to the internet?", new String[] { "OK" }, null);
					messageDialog.show();
				}
			});
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return properties;
	}

	private void runScripts() throws Exception {
		final String worldName = clientModel.getWorldName();
		final String avatarName = clientModel.getAvatarName();

		// create toplevel scope
		System.out.println("Creating toplevel scope");
		Context cx = Context.enter();
		cx.setOptimizationLevel(-1);
		scope = new ImporterTopLevel(cx);
		cx.initStandardObjects(scope, false);
		cx.evaluateString(scope, "importPackage(Packages.com.gallantrealm.webworld.model);", "<importPackage>", 1, null);
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

		// Add console and built-in UI methods
		Console console = new Console();
		Object wrappedConsole = Context.javaToJS(console, scope);
		ScriptableObject.putProperty(scope, "console", wrappedConsole);
		scope.defineFunctionProperties(new String[] { "alert", "confirm", "prompt", "select"}, GlobalFunctions.class, ScriptableObject.READONLY);

		// make sure the world, some constants, and avatar are available to the world script
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		Object wrappedWorld = Context.javaToJS(this, scope);
		ScriptableObject.putProperty(scope, "world", wrappedWorld);
		Object wrappedUser = Context.javaToJS(user, scope);
		ScriptableObject.putProperty(scope, "user", wrappedUser);
		
		// restore or create the avatar customization object
		AvatarCustomization avatarCustomization = AvatarCustomization.createOrRestore(avatarName);
		Object wrappedAvatarCustomization = Context.javaToJS(avatarCustomization, scope);
		ScriptableObject.putProperty(scope, "avatarCustomization", wrappedAvatarCustomization);

		// create the avatar
		System.out.println("Opening avatar " + avatarName);
		WWObject avatar = null;
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			// First try local
			File file = new File(clientModel.getLocalFolder() + "/avatars/" + avatarName + "/" + avatarProperties.getProperty("script"));
			System.out.println(">> " + file);
			if (file.exists()) {
				inputStream = new FileInputStream(file);
				World.runningLocalAvatarScript = true;
			} else {
				// Then try gallantrealm.com
				URL url = new URL(clientModel.getGallantUrl() + "/webworld/avatars/" + avatarName + "/" + avatarProperties.getProperty("script"));
				System.out.println(">> " + url);
				connection = (HttpURLConnection) (url.openConnection());
				inputStream = connection.getInputStream();
				World.runningLocalAvatarScript = false;
			}
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			System.out.println("Running avatar script..");
			try {
				World.runningAvatarScript = true;
				cx.evaluateReader(scope, reader, avatarProperties.getProperty("script"), 1, null);
				if (ScriptableObject.hasProperty(scope, "avatar")) {
					NativeJavaObject avatarWrapped = (NativeJavaObject) ScriptableObject.getProperty(scope, "avatar");
					if (avatarWrapped != null) {
						avatar = (WWObject) (avatarWrapped.unwrap());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(scrubScriptError(e.getMessage()));
			} finally {
				World.runningAvatarScript = false;
			}
			if (avatar == null) {
				throw new Exception("Avatar script " + avatarProperties.getProperty("script") + " didn't set an object to use for an avatar.");
			}

			this.addObject(avatar);
			user.setAvatarId(avatar.getId());
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Couldn't download avatar " + avatarName + ".  Are you connected to the internet?");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}

		if (clientModel.isCustomizeMode()) {
			World.runningAvatarScript = true;
			
			// make the avatar pickable and physical, but give it no freedom to move
			avatar.setPickable(true);
			avatar.setPhysical(true);
			avatar.setFreedomMoveX(false);
			avatar.setFreedomMoveY(false);
			avatar.setFreedomMoveZ(false);
			avatar.setFreedomRotateX(false);
			avatar.setFreedomRotateY(false);
			avatar.setFreedomRotateZ(false);

			setPersistent(false);
			setUsesController(false);
			
			// Create the customizer world
			System.out.println("Opening customizer for " + avatarName);
			connection = null;
			inputStream = null;
			try {
				setName(avatarProperties.getProperty("name"));  // to override
				
				// First try local
				File file = new File(clientModel.getLocalFolder() + "/avatars/" + avatarName + "/" + avatarProperties.getProperty("customizer"));
				System.out.println(">> " + file);
				if (file.exists()) {
					inputStream = new FileInputStream(file);
					World.runningLocalWorldScript = true;
				} else {
					// Then try gallantrealm.com
					URL url = new URL(clientModel.getGallantUrl() + "/webworld/avatars/" + avatarName + "/" + avatarProperties.getProperty("customizer"));
					System.out.println(">> " + url);
					connection = (HttpURLConnection) (url.openConnection());
					inputStream = connection.getInputStream();
					World.runningLocalWorldScript = false;
				}
				Reader reader = new InputStreamReader(inputStream, "UTF-8");
				System.out.println("Running customizer script..");

				try {
					cx.evaluateReader(scope, reader, avatarProperties.getProperty("customizer"), 1, null);
				} catch (Exception e) {
					e.printStackTrace();
					throw new Exception(scrubScriptError(e.getMessage()));
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception("Couldn't download customizer for " + avatarName + ".  Are you connected to the internet?");
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}

		} else {

			// make the avatar pickable and physical (can be overwritten by world)
			avatar.setPickable(true);
			avatar.setPhysical(true);
			avatar.setFreedomMoveZ(true);
			avatar.setFreedomRotateX(false);
			avatar.setFreedomRotateY(false);
			avatar.setFreedomRotateZ(true);

			// add a behavior to the avatar so it is facing user for a while
			if (clientModel.cameraInitiallyFacingAvatar) {
				WWBehavior avatarCameraBehavior = new WWBehavior() {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean timerEvent() {
						clientModel.setViewpoint(clientModel.getViewpoint());
						return true;
					}
				};
				avatar.addBehavior(avatarCameraBehavior);
				avatarCameraBehavior.setTimer(5000);
			}

			// create the world
			System.out.println("Opening world " + worldName);
			connection = null;
			inputStream = null;
			try {
				// First try local
				File file = new File(clientModel.getLocalFolder() + "/worlds/" + worldName + "/" + worldProperties.getProperty("script"));
				System.out.println(">> " + file);
				if (file.exists()) {
					inputStream = new FileInputStream(file);
					World.runningLocalWorldScript = true;
				} else {
					// Then try gallantrealm.com
					URL url = new URL(clientModel.getGallantUrl() + "/webworld/worlds/" + worldName + "/" + worldProperties.getProperty("script"));
					System.out.println(">> " + url);
					connection = (HttpURLConnection) (url.openConnection());
					inputStream = connection.getInputStream();
					World.runningLocalWorldScript = false;
				}
				Reader reader = new InputStreamReader(inputStream, "UTF-8");
				System.out.println("Running world script..");

				try {
					cx.evaluateReader(scope, reader, worldProperties.getProperty("script"), 1, null);
				} catch (Exception e) {
					e.printStackTrace();
					throw new Exception(scrubScriptError(e.getMessage()));
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception("Couldn't download world " + worldName + ".  Are you connected to the internet?");
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}

		}

		// Exit the top-level scope
		Context.exit();
	}

	private String scrubScriptError(String scriptErrorMessage) {
		scriptErrorMessage = scriptErrorMessage.replace("Java class", "Class");
		scriptErrorMessage = scriptErrorMessage.replace("com.gallantrealm.webworld.model.", "");
		scriptErrorMessage = scriptErrorMessage.replace("public instance field or method", "property");
		scriptErrorMessage = scriptErrorMessage.replace("Java method", "Method");
		return scriptErrorMessage;
	}

	@Override
	public void restored() {
		clientModel = AndroidClientModel.getClientModel();
		clientModel.setSelectedObject(clientModel.getAvatar());
		if (onRestored != null) {
			callFunction(onRestored, this, new Object[] {});
		}
	}

	public void setOnRestored(Function onRestored) {
		this.onRestored = onRestored;
	}

	public Function getOnRestored() {
		return onRestored;
	}

	@Override
	public void displayed() {
		showBannerAds();
	}

	public void actionsChanged() {
		clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_AVATAR_ACTIONS_CHANGED);
	}

//	public PhysicsThread makePhysicsThread() {
//		return new OldPhysicsThread(this, 10);
//	}
	
	@Override
	public void preloadTexture(String textureName) {
		super.preloadTexture(worldPrefixUrl(textureName));
	}
	
	@Override
	public void preloadSound(String soundName) {
		super.preloadSound(worldPrefixUrl(soundName));
	}
	
	@Override
	public void playSound(String soundName, float volume) {
		super.playSound(worldPrefixUrl(soundName), volume);
	}
	
	@Override
	public void playSound(String soundName, float volume, float pitch) {
		super.playSound(worldPrefixUrl(soundName), volume, pitch);
	}
	
	public void playSong(String url) {
		super.playSong(worldPrefixUrl(url), 1.0f);
	}

	public void playSong(String url, float volume) {
		super.playSong(worldPrefixUrl(url), volume);
	}

	public static String worldPrefixUrl(String url) {
		if (url != null) {
			if (!url.contains(":") && url.contains(".")) { // a file in the world or avatar
				AndroidClientModel clientModel = AndroidClientModel.getClientModel();
				if (World.runningAvatarScript) {
					String avatarName = clientModel.getAvatarName();
					if (World.runningLocalAvatarScript) {
						return clientModel.getLocalFolder() + "/avatars/" + avatarName + "/" + url;
					} else {
						return clientModel.getGallantUrl() + "/webworld/avatars/" + avatarName + "/" + url;
					}
				} else {
					String worldName = clientModel.getWorldName();
					if (World.runningLocalWorldScript) {
						return clientModel.getLocalFolder() + "/worlds/" + worldName + "/" + url;
					} else {
						return clientModel.getGallantUrl() + "/webworld/worlds/" + worldName + "/" + url;
					}
				}
			}
		}
		return url;
	}

	@Override
	public boolean dampenCamera() {
		return clientModel.getViewpoint() != 1;
	}

	@Override
	public boolean allowPicking() {
		return true; // so touch event will work
	}

	public void callFunction(Function fun, Object thisObj, Object[] params) {
		Context cx = Context.enter();
		try {
			Scriptable scriptableThisObj = Context.toObject(thisObj, scope);
			for (int i = 0; i < params.length; i++) {
				params[i] = Context.toObject(params[i], scope);
			}
			fun.call(cx, scope, scriptableThisObj, params);
		} catch (Exception e) {
			final String errorMessage = scrubScriptError(e.getMessage());
			clientModel.getContext().runOnUiThread(new Runnable() {
				public void run() {
					final MessageDialog messageDialog = new MessageDialog(clientModel.getContext(), null, errorMessage, new String[] { "OK" }, null);
					messageDialog.show();
				}
			});

		} finally {
			Context.exit();
		}
	}

	@Override
	public int addObject(WWObject object) {
		int id = super.addObject(object);
		AndroidRenderer.clearRenderings();
		return id;
	}

	public void removeObject(WWObject object) {
		if (object != null) {
			super.removeObject(object.getId());
			AndroidRenderer.clearRenderings();
			if (clientModel.getSelectedObject() == object) {
				clientModel.setSelectedObject(null);
			}
		}
	}

	@Override
	public void setStatus(String status) {
		super.setStatus(status);
		clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
	}

	public final long getTime() {
		return getWorldTime();
	}
	
	private boolean usesController = true;
	
	public void setUsesController(boolean usesController) {
		this.usesController = usesController;
	}
	
	public boolean getUsesController() {
		return usesController;
	}
	
	public boolean usesController() {
		return usesController;
	}

	public String getMoveType() {
		return moveType == null ? "explore" : moveType;
	}
	
	public void setMoveType(String moveType) {
		this.moveType = moveType;
		if (moveType.equals("confront")) {
			setConfrontMode(true);
		} else {
			setConfrontMode(false);
		}
	}

	public int getMoveXType() {
		if (moveType == null || moveType.equals("explore")) {
			return MOVE_TYPE_TURN;
		} else if (moveType.equals("confront")) {
			return MOVE_TYPE_SLIDE;
		} else if (moveType.equals("drive")) {
			return MOVE_TYPE_TURN;
		} else if (moveType.equals("fly")) {
			return MOVE_TYPE_LEAN;
		}
		return MOVE_TYPE_TURN;
	}

	public int getMoveYType() {
		if (moveType == null || moveType.equals("explore")) {
			return MOVE_TYPE_THRUST;
		} else if (moveType.equals("confront")) {
			return MOVE_TYPE_THRUST;
		} else if (moveType.equals("drive")) {
			return MOVE_TYPE_THRUST;
		} else if (moveType.equals("fly")) {
			return MOVE_TYPE_TILT;
		}
		return MOVE_TYPE_THRUST;
	}

	public float[] getMoveXSlide() {
		return new float[] { 3, 2, 1, 0, -1, -2, -3 };
	}

	public float[] getMoveXTurn() {
		return new float[] { -180, -120, -60, -30, -15, 0, 15, 30, 60, 120, 180 };
	}

	public float[] getMoveXLean() {
		return new float[] { -15, -5, 0, 5, 15 };
	}

	public float[] getMoveYThrust() {
		return new float[] { -2, -1.33f, -0.66f, 0, 1, 2, 3 };
	}
	
	public float[] getMoveYTilt() {
		return new float[] { 30, 10, 0, -5, -10 };
	}

	public float[] getMoveYLift() {
		return new float[] { -8, -4, -2, 0, 2, 4, 8 };
	}

}
