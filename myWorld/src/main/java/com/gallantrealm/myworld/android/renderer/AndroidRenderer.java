package com.gallantrealm.myworld.android.renderer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.gallantrealm.android.HttpFileCache;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.client.renderer.IRenderer;
import com.gallantrealm.myworld.client.renderer.IRendering;
import com.gallantrealm.myworld.client.renderer.ISoundGenerator;
import com.gallantrealm.myworld.client.renderer.ITextureRenderer;
import com.gallantrealm.myworld.client.renderer.IVideoTextureRenderer;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWCylinder;
import com.gallantrealm.myworld.model.WWMesh;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWParticleEmitter;
import com.gallantrealm.myworld.model.WWPlant;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWSculpty;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWTorus;
import com.gallantrealm.myworld.model.WWTranslucency;
import com.gallantrealm.myworld.model.WWVector;
import com.gallantrealm.myworld.model.WWWorld;
import com.htc.view.DisplaySetting;
import com.lge.real3d.Real3D;
import com.lge.real3d.Real3DInfo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

public class AndroidRenderer implements IRenderer, GLSurfaceView.Renderer {

	// These values are used in the positionCamera to damp the positional movements
	public float dampXCamera;
	public float dampYCamera;
	public float dampZCamera;
	public float dampCameraVelocityX;
	public float dampCameraVelocityY;
	public float dampCameraVelocityZ;
	public float dampCameraDistance;
	public float absoluteCameraPan;	// including avatar tracking
	public float absoluteCameraTilt;

	public long lastCameraAdjustTime = 0;

	public int pickedSide;
	public float pickedOffsetX;
	public float pickedOffsetY;

	public static final float CLOSENESS = 0.25f;
	public static final float TORADIAN = 0.0174532925f;
	public static final float TODEGREES = FastMath.TODEGREES;

	public float stereoAmount = 1.0f; // 0.5 - 2.0 are good range

	public final ClientModel clientModel = AndroidClientModel.getClientModel();
	public float lastLimitedCameraDistance = clientModel.getCameraDistance();
	public static AndroidRenderer androidRenderer;

	public static AndroidRenderer createAndroidRenderer(Context context, GLSurfaceView view, boolean simpleRendering) {
		androidRenderer = new AndroidRenderer(context, view, simpleRendering);
		return androidRenderer;
	}

	public final AndroidSoundGenerator soundGenerator;
	public Context context;
	public GLSurfaceView view;
	public boolean simpleRendering;
	public static HashMap<String, Integer> textureCache = new HashMap<String, Integer>();
	public static HashMap<String, Integer> normalTextureCache = new HashMap<String, Integer>();
	public static HashMap<String, Boolean> hasAlphaCache = new HashMap<String, Boolean>();

	public AndroidRenderer(Context context, GLSurfaceView view, boolean simpleRendering) {
		System.out.println(">AndroidRenderer.constructor");
		this.context = context;
		this.view = view;
		this.simpleRendering = simpleRendering;
		this.soundGenerator = new AndroidSoundGenerator(context);
		System.out.println("<AndroidRenderer.constructor");
	}

	public static AndroidRenderer getAndroidRenderer(Context context) {
		// androidRenderer.context = context;
		return androidRenderer;
	}

	public void destroy() {
		getSoundGenerator().destroy();
		GLSurface.destroyVertexBuffers();
		textureCache.clear();
		normalTextureCache.clear();
		hasAlphaCache.clear();
		geometryCache.clear();
	}

	private static final int viswindow = 60; // this needs to be based on zoom someday
	static final int SHADOW_MAP_WIDTH = 2048;
	static final int SHADOW_MAP_HEIGHT = 2048;

	/**
	 * Returns true if the object is fully or partially visible (not occluded) and within rendering threshold. May return true even when not visible (as the implementation may improve over time).
	 */
	public final boolean isVisible(WWVector adjustedCameraPosition, WWObject object, long worldTime, WWVector temp) {
		if (!object.renderit) {
			return false;
		}
		if (Math.abs(clientModel.getDampedCameraTilt()) > 45) {
			return true;
		}

		object.getAbsoluteAnimatedPosition(temp, worldTime);
		temp.subtract(adjustedCameraPosition);

		if (temp.length() < object.extent) { // to big and close to accurately determine
			return true;
		}

		float theta = TODEGREES * FastMath.atan2(temp.y, temp.x);
		float offpan = Math.abs(theta + clientModel.getDampedCameraPan() + 36000) % 360 - 270;

		return offpan > -viswindow && offpan < viswindow;
	}

	public final WWVector getAdjustedCameraPosition() {
		float x = dampXCamera + dampCameraDistance * (float) Math.sin(TORADIAN * absoluteCameraPan) * (float) Math.cos(TORADIAN * absoluteCameraTilt);
		float y = dampYCamera + dampCameraDistance * (float) Math.cos(TORADIAN * absoluteCameraPan) * (float) Math.cos(TORADIAN * absoluteCameraTilt);
		float z = dampZCamera + (float) Math.sin(TORADIAN * absoluteCameraTilt) * dampCameraDistance;
		WWVector adjustedCameraPosition = new WWVector(x, y, z);
// if (Math.abs(dampCameraTilt) < 45) {
// adjustedCameraPosition.add(-clientModel.world.renderingThreshold * FastMath.sin(TORADIAN * dampCameraPan), -clientModel.world.renderingThreshold * FastMath.cos(TORADIAN * dampCameraPan), 0);
// }
		return adjustedCameraPosition;
	}

	public static final boolean USE_DEPTH_SHADER = true;
	public HashMap<String, GLSurface[]> geometryCache = new HashMap();

	private boolean clearRenderingsNeeded;
	private long nrenders = 0;

	public void clearRenderings() {
		clearRenderingsNeeded = true;
		nrenders = 0;
	}

	public DepthShader depthShader;
	public ShadowMapShader shadowMapShader;
	public Shader textureShader;

	protected float[] projectionMatrix = new float[16];
	protected float[] viewMatrix = new float[16];
	protected float[] sunViewMatrix = new float[16];

	Bitmap regenBitmap;
	String regenTextureName;
	int shadowMapFrameBufId;
	int shadowMapTextureId;
	WWVector lastShadowCameraViewPosition = new WWVector();
	boolean surfaceCreated;
	boolean is3DDevice;
	boolean hasDepthTexture;
	private long lastDrawFrameTime = 0;
	int viewportWidth;
	int viewportHeight;
	public Thread threadWaitingForPick;
	public int pickX;
	public int pickY;
	public WWObject pickedObject;

	public static void checkGlError() {
		int error;
		RuntimeException exception = null;
		while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
			Throwable t = new Throwable();
			t.fillInStackTrace();
			StackTraceElement caller = t.getStackTrace()[1];
			System.out.println("AndroidRenderer.checkGlError " + error + ": " + GLU.gluErrorString(error) + " at " + caller);
		}
	}

	public static void ignoreGlError() {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
		}
	}

	public synchronized final int getNormalTexture(String textureName, boolean pixelate) {
		if (textureName == null) {
			textureName = "flat";
		}
		if (textureName.endsWith(".png")) {
			textureName = textureName.substring(0, textureName.length() - 4) + "_nrm.png";
		} else if (textureName.endsWith(".jpg")) {
			textureName = textureName.substring(0, textureName.length() - 4) + "_nrm.jpg";
		} else {
			textureName = textureName + "_nrm.png";
		}
		Integer idInteger = normalTextureCache.get(textureName);
		if (idInteger != null) {
			return idInteger;
		} else {
			InputStream is = null;
			boolean isPkm = false;
			try {
				System.out.println("AndroidRenderer.getNormalTexture loading bitmap " + textureName);
				Bitmap bitmap = null;
				if (textureName.contains(":")) { // a url
					Uri uri = Uri.parse(textureName);
					Bitmap unscaledBitmap = readImageTexture(uri);
					if (unscaledBitmap != null) {
						bitmap = Bitmap.createScaledBitmap(unscaledBitmap, 256, 256, false);
						if (bitmap == null) {
							bitmap = unscaledBitmap;
						} else 	if (unscaledBitmap != bitmap) {
							unscaledBitmap.recycle();
						}
					}
				} else {
					if (isPkm) {
						return genCompressedTexture(textureName);
					} else {
						try {
							is = context.getAssets().open(textureName); // asset
						} catch (Exception e) {
							try {
								File file = new File(context.getFilesDir(), textureName); // local file
								is = new BufferedInputStream(new FileInputStream(file), 65536);
							} catch (Exception e2) {
								// not there
							}
						}
						if (is != null) {
							bitmap = BitmapFactory.decodeStream(is);
						}
					}
				}
				if (bitmap == null) { // a failure
					System.out.println("AndroidRenderer.getNormalTexture couldn't find " + textureName + " so will use flat_nrm.png");
					int textureId = getNormalTexture("flat", true);
					normalTextureCache.put(textureName, textureId);
					return textureId;
				}
				int textureId = genTexture(bitmap, textureName, pixelate);
				normalTextureCache.put(textureName, textureId);
				return textureId;
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					// Ignore.
				}
			}
		}
	}

	public boolean textureHasAlpha(String textureName) {
		Boolean hasAlpha =  hasAlphaCache.get(textureName);
		if (hasAlpha == null) {
			return false;
		}
		return hasAlpha;
	}

	@Override
	public IRendering createWorldRendering(WWWorld world, long worldTime) {
		return new GLWorld(this, world, worldTime);
	}

	@Override
	public IRendering createBoxRendering(WWBox box, long worldTime) {
		return new GLSimpleShape(this, box, worldTime);
	}

	@Override
	public IRendering createCylinderRendering(WWCylinder cylinder, long worldTime) {
		return new GLSimpleShape(this, cylinder, worldTime);
	}

	@Override
	public IRendering createSphereRendering(WWSphere sphere, long worldTime) {
		return new GLSimpleShape(this, sphere, worldTime);
	}

	@Override
	public IRendering createTorusRendering(WWTorus torus, long worldTime) {
		return new GLSimpleShape(this, torus, worldTime);
	}

	@Override
	public IRendering createMeshRendering(WWMesh mesh, long worldTime) {
		return new GLMesh(this, mesh, worldTime);
	}

	@Override
	public IRendering createSculptyRendering(WWSculpty sculpty, long worldTime) {
		return new GLSculpty(this, sculpty, worldTime);
	}

	@Override
	public IRendering createPlantRendering(WWPlant plant, long worldTime) {
		return new GLPlant(this, plant, worldTime);
	}

	@Override
	public IRendering createTranslucencyRendering(WWTranslucency translucency, long worldTime) {
		return new GLTranslucency(this, translucency, worldTime);
	}

	@Override
	public ITextureRenderer getTextureRenderer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVideoTextureRenderer getVideoTextureRenderer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Perform setup that only needs to be done whenever the rendering surface changes.
	 * This speeds up the drawing frame rate.
	 */
	private void initializeStandardDraw() {

		// bind default framebuffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	
		GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
		float ratio;
		if (viewportWidth > viewportHeight) {
			ratio = (float) viewportWidth / (float) viewportHeight;
		} else {
			ratio = (float) viewportHeight / (float) viewportWidth;
		}
		Matrix.frustumM(projectionMatrix, 0, -ratio / 2.0f * CLOSENESS, ratio / 2.0f * CLOSENESS, -0.5f * CLOSENESS, 0.5f * CLOSENESS, 1.0f * CLOSENESS, 16384 * CLOSENESS);
	
		GLES20.glDisable(GLES20.GL_DITHER);
	
		// Depth buffer settings
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glDepthFunc(GLES20.GL_LESS);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glFrontFace(GLES20.GL_CW);
	
		// Texture buffer settings
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		// GLES20.glEnable(GLES20.GL_BLEND); // enabled before drawing translucent textures
	
		// set color texture properties
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
	
		// set shadow map texture properties
		if (!clientModel.isSimpleRendering() && shadowMapTextureId != 0) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT); // GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT); // GL_CLAMP_TO_EDGE);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapTextureId);
		}
	
		// set bump map texture properties
		GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
	
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	}

	public synchronized final int getTexture(String textureName, boolean pixelate) {
		if (textureName == null) {
			textureName = "white";
		}
		Integer idInteger = textureCache.get(textureName);
		if (idInteger != null) {
			if (textureName.equals(regenTextureName)) {
				updateTexture(regenBitmap, idInteger);
				regenTextureName = null;
				regenBitmap = null;
			}
			return idInteger;
		} else {
			InputStream is = null;
			boolean isPkm = false;
			try {
				System.out.println("AndroidRenderer.getTexture loading bitmap " + textureName);
				Bitmap bitmap = null;
				if (textureName.equals("surface_select")) { // a special bitmap
					bitmap = Bitmap.createBitmap(512, 512, Config.RGB_565);
					for (int i = 0; i < 512; i++) {
						int red = ((i & 0x1C0) >> 1) | 0x10;
						int green = (i & 0x03F) << 2;
						int blue = 0;
						int color = 0xFF000000 | (red << 16) | (green << 8) | blue;
						for (int j = 0; j < 512; j++) {
							bitmap.setPixel(i, j, color);
						}
					}
				} else if (textureName.contains(":")) { // a url
					Uri uri = Uri.parse(textureName);
					Bitmap unscaledBitmap = readImageTexture(uri);
					if (unscaledBitmap != null) {
						bitmap = Bitmap.createScaledBitmap(unscaledBitmap, 256, 256, false);
						if (unscaledBitmap != bitmap) {
							unscaledBitmap.recycle();
						}
					}
				} else {
					if (isPkm) {
						return genCompressedTexture(textureName);
					} else {
						if (textureName.startsWith("/")) { // fully qualified file name
							try {
								File file = new File(textureName);
								is = new BufferedInputStream(new FileInputStream(file), 65536);
								bitmap = BitmapFactory.decodeStream(is);
							} catch (Exception e2) {
								// not there
							}
						} else { // open an asset or local file
							try {
//								try {
//									is = context.getAssets().open(textureName + ".dds"); // dds is faster?
//									return genCompressedTexture(textureName);
//								} catch (Exception e) {
								is = context.getAssets().open(textureName + ".png"); // asset
								bitmap = BitmapFactory.decodeStream(is);
//								}
							} catch (Exception e) {
								try {
									File file = new File(context.getFilesDir(), textureName + ".png"); // local file
									is = new BufferedInputStream(new FileInputStream(file), 65536);
									bitmap = BitmapFactory.decodeStream(is);
								} catch (Exception e2) {
									// not there
								}
							}
						}
					}
				}
				if (bitmap == null) { // problems
					int textureId = getTexture("white", true); // use white texture
					textureCache.put(textureName, textureId);
					hasAlphaCache.put(textureName, false);
					return textureId;
				}
				boolean hasAlpha = bitmap.hasAlpha();

				int textureId = genTexture(bitmap, textureName, pixelate);
				textureCache.put(textureName, textureId);
				hasAlphaCache.put(textureName, hasAlpha);
				return textureId;
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					// Ignore.
				}
			}
		}
	}

	final int genTexture(Bitmap bitmap, String textureName, boolean pixelate) {
		System.out.println("AndroidRenderer.genTexture " + textureName);
		int[] textureIds = new int[1];
		GLES20.glGenTextures(1, textureIds, 0);
		int textureId = textureIds[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		if (textureName.equals("surface_select")) {
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		} else {
			if (simpleRendering) {
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES11.GL_GENERATE_MIPMAP, GLES20.GL_FALSE);
			}
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);
			if (pixelate) {
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			} else {
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			}
		}
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

		// Generate textures (original and mipmaps)
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int level = 0;
		while (height >= 1 || width >= 1) {

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bitmap, 0);
			// GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, GLES20.GL_RGB, bitmap, GLES20.GL_UNSIGNED_SHORT_5_6_5, 0);
			// GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0);

			if (height <= 1 || width <= 1) {
				break;
			}
			if (textureName.equals("surface_select") || height > 1024 || width > 1024) { // avoid mipmapping large textures
				bitmap.recycle();
				break;
			}

			// Increase the mipmap level
			level++;

			// gen a scaled mipmap
			height /= 2;
			width /= 2;
			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);

			// Clean up
			bitmap.recycle();
			bitmap = bitmap2;
		}
		return textureId;
	}

	final int genCompressedTexture(String textureName) {
		System.out.println("AndroidRenderer.genCompressedTexture " + textureName);

		int[] textureIds = new int[1];
		GLES20.glGenTextures(1, textureIds, 0);
		int textureId = textureIds[0];
		textureCache.put(textureName, textureId);
		hasAlphaCache.put(textureName, false);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		if (simpleRendering) {
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES11.GL_GENERATE_MIPMAP, GLES20.GL_FALSE);
		}
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

		try {
			int level = 0;
			while (level <= 9) {
				InputStream is = context.getAssets().open(textureName + "_mip_" + level + ".pkm"); // asset
				ETC1Texture etc1texture = ETC1Util.createTexture(is);

				// Generate textures (original and mipmaps)
				int width = etc1texture.getWidth();
				int height = etc1texture.getHeight();
				Buffer data = etc1texture.getData();
				int imageSize = data.remaining();
				GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, level, ETC1.ETC1_RGB8_OES, width, height, 0, imageSize, data);
				level++;
			}

		} catch (IOException e) {
			// e.printStackTrace();
		}
		return textureId;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		System.out.println(">AndroidRenderer.onSurfaceCreated");
		if (clientModel.isStereoscopic()) {
			try {
				is3DDevice = DisplaySetting.setStereoscopic3DFormat(view.getHolder().getSurface(), DisplaySetting.STEREOSCOPIC_3D_FORMAT_SIDE_BY_SIDE);
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println("No HTC 3D device support: " + e.getMessage());
			}
			if (!is3DDevice) {
				try {
					Real3D real3D = new Real3D(view.getHolder());
					is3DDevice = real3D.setReal3DInfo(new Real3DInfo(true, Real3D.REAL3D_TYPE_SS, Real3D.REAL3D_ORDER_LR));
				} catch (Throwable e) {
					System.out.println("No LG 3D device support: " + e.getMessage());
				}
			}
		}
		clearTextureCache();
		clearRenderings();
	
		// The depth texture extension is required for shadows
		String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		System.out.println(extensions);
		if (extensions.contains("GL_OES_depth_texture") ) {
			hasDepthTexture = true;
		}
	
		// Initialize shaders
		if (clientModel.isSimpleRendering()) {
			textureShader = new SimpleTextureShader();
		} else {
			textureShader = new ShadowingTextureShader();
		}
		if (USE_DEPTH_SHADER) {
			depthShader = new DepthShader();
		}
		if (hasDepthTexture) {
			shadowMapShader = new ShadowMapShader();
			// setupShadowMap();	// note: skipped because it is done in onSurfaceChanged
		}
		GLES20.glReleaseShaderCompiler();
		GLES20.glGetError(); // to clear as releaseShaderCompiler might not be supported
	
		if (clientModel != null && clientModel.world != null) {
			long time = clientModel.world.getWorldTime();
			clientModel.world.createRendering(this, time);
		}
	
		initializeStandardDraw();
	
		surfaceCreated = true;
		System.out.println("<AndroidRenderer.onSurfaceCreated");
	}

	final void updateTexture(Bitmap bitmap, int textureId) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		// avoid mipmapping for updated textures to save time
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

		// Generate textures (original and mipmaps)
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int level = 0;

		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, level, 0, 0, bitmap);
		// GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, level, 0, 0, bitmap, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5);
		// GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, level, 0, 0, bitmap, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE);
	}

	public final synchronized void regenTexture(Bitmap bitmap, String textureName) {
		this.regenBitmap = bitmap;
		this.regenTextureName = textureName;
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		System.out.println(">AndroidRenderer.onSurfaceChanged");
	
		clearTextureCache();
	
		viewportWidth = width;
		viewportHeight = height;
	
		if (hasDepthTexture) {
			setupShadowMap();
		}
		initializeStandardDraw();
	
		System.out.println("<AndroidRenderer.onSurfaceChanged");
	}

	/**
	 * Read the image into a 256x256 bitmap, ready for texture scaling
	 * 
	 * @param selectedImage
	 * @return
	 */
	public static final Bitmap readImageTexture(Uri selectedImage) {
		if (selectedImage.getScheme() != null && selectedImage.getScheme().startsWith("http")) {
			// from the web
			Bitmap bm = null;
			try {
//				HttpURLConnection connection = (HttpURLConnection) (new URL(selectedImage.toString()).openConnection());
//				InputStream instream = connection.getInputStream();
//				bm = BitmapFactory.decodeStream(instream);
				File imageFile = HttpFileCache.getFile(selectedImage.toString(), ClientModel.getClientModel().getContext());
				if (imageFile != null) {
					bm = BitmapFactory.decodeFile(imageFile.getPath());
				}
				return bm;
			} catch (Exception e) {
				System.err.println("AndroidRenderer.readImageTexture: " + e.getMessage());
			}
			return bm;
		} else {
			// from assets
			Bitmap bm = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			AssetFileDescriptor fileDescriptor = null;
			try {
				fileDescriptor = ClientModel.getClientModel().getContext().getContentResolver().openAssetFileDescriptor(selectedImage, "r");

				// first, get the bitmap size
				options.inJustDecodeBounds = true;
				bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);

				// then, get a sampled-down bitmap large enough for the texture
				options.inJustDecodeBounds = false;
				options.inSampleSize = Math.min(options.outWidth, options.outHeight) / 512;
				bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
			} catch (FileNotFoundException e) {
				System.err.println("AndroidRenderer.readImageTexture: File not found -- " + selectedImage);
			} finally {
				try {
					if (fileDescriptor != null) {
						fileDescriptor.close();
					}
				} catch (IOException e) {
				}
			}

			// Determine the image rotation and antirotate if necessary
			try {
				ExifInterface exif = new ExifInterface(selectedImage.getPath());
				int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				System.out.println("EXIF rotation = " + exifOrientation);
				int rotation;
				if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
					rotation = 90;
				} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
					rotation = 180;
				} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
					rotation = 270;
				} else {
					rotation = 0;
				}
				if (rotation != 0) {
					android.graphics.Matrix matrix = new android.graphics.Matrix();
					matrix.preRotate(rotation);
					bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return bm;
		}
	}

	static long lastStatusUpdateTime;

	@Override
	public void onDrawFrame(GL10 unused) {
		AndroidRenderer.ignoreGlError();  // make sure errors are clear

		if (!surfaceCreated) { // happens early when creating surface
			return;
		}

		WWWorld world = clientModel.world;
		if (world == null) { // happens on on disconnect
			return;
		} else if (world.getRendering() == null) { // this happens on disconnect sometimes too
			return;
		}

		try { // catch any other rendering exceptions

			long drawFrameTime = System.currentTimeMillis();
			if (lastDrawFrameTime == 0) {
				lastDrawFrameTime = drawFrameTime - 25;
			}

			// wait long enough to match frame rate
			try {
				Thread.sleep(Math.max(0, 32 - (drawFrameTime - lastDrawFrameTime)));
				drawFrameTime = System.currentTimeMillis();
				if (lastStatusUpdateTime < drawFrameTime - 1000) {
					clientModel.setActualFrameRate((int)(1000 / (drawFrameTime - lastDrawFrameTime)));
					lastStatusUpdateTime = drawFrameTime;
				}
			} catch (InterruptedException e) {
			}

			if (world.getPhysicsIterationTime() == 0) { // physics running on rendering thread
				world.performPhysicsIteration(Math.min(drawFrameTime - lastDrawFrameTime, 50));
			}

			lastDrawFrameTime = drawFrameTime;

			GLWorld worldRendering = (GLWorld) world.getRendering();

			long time;
			synchronized (world) {
				time = world.getWorldTime();
				worldRendering.snap(time);
			}
			preRender(time);

			// Generate the shadow map texture(s)
			if (hasDepthTexture && !clientModel.isSimpleRendering() && world.supportsShadows() && worldRendering.drawnOnce) {
				// generate view matrix for perspective shadow map
				WWVector dampCameraDistanceVector = new WWVector(0, dampCameraDistance, 0);
				WWVector dampCameraRotationVector = new WWVector(-absoluteCameraTilt, 0, clientModel.getDampedCameraPan());
				rotate(dampCameraDistanceVector, dampCameraRotationVector);
				float x = dampXCamera + dampCameraDistanceVector.x;
				float y = dampYCamera + dampCameraDistanceVector.y;
				float z = dampZCamera + dampCameraDistanceVector.z;
				clientModel.setDampedCameraLocation(x, y, z);
				Matrix.translateM(viewMatrix, 0, projectionMatrix, 0, 0, 0, 0);
				Matrix.rotateM(viewMatrix, 0, absoluteCameraTilt, 1, 0, 0);
				Matrix.rotateM(viewMatrix, 0, -absoluteCameraPan, 0, 1, 0);
				Matrix.translateM(viewMatrix, 0, -x, -z, -y);
				shadowMapShader.setViewPosition(dampCameraDistanceVector.x, dampCameraDistanceVector.y, dampCameraDistanceVector.z);
				generateShadowMap(time, viewMatrix);
			}

			AndroidRenderer.checkGlError();  // make sure errors are clear

			// Sunlight
			textureShader.setSunPosition(world.getSunDirectionX(), world.getSunDirectionY(), world.getSunDirectionZ());
			WWColor sunColor = world.getSunColor();
			textureShader.setSunColor(sunColor.getRed(), sunColor.getGreen(), sunColor.getBlue());
			textureShader.setSunIntensity(world.getSunIntensity());
			textureShader.setAmbientLightIntensity(world.getAmbientLightIntensity());

			// Fog
			textureShader.setFogDensity(world.getFogDensity());

			if (threadWaitingForPick != null) {
				pickingDraw(time, viewMatrix);
				initializeStandardDraw();
			}

			// Clear to background color
			WWColor skyColor = world.getSkyColor();
			GLES20.glClearColor(skyColor.getRed(), skyColor.getGreen(), skyColor.getBlue(), 1);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

			if (clientModel.isStereoscopic()) {

				// transform according to camera - left eye
				WWVector dampCameraDistanceVector = new WWVector(0, dampCameraDistance, 0);
				dampCameraDistanceVector.add(stereoAmount * -0.02f * dampCameraDistance, 0, 0);
				WWVector dampCameraRotationVector = new WWVector(-absoluteCameraTilt, 0, absoluteCameraPan);
				rotate(dampCameraDistanceVector, dampCameraRotationVector);
				float x = dampXCamera + dampCameraDistanceVector.x;
				float y = dampYCamera + dampCameraDistanceVector.y;
				float z = dampZCamera + dampCameraDistanceVector.z;
				clientModel.setDampedCameraLocation(x, y, z);
				Matrix.translateM(viewMatrix, 0, projectionMatrix, 0, 0, 0, 0);
				Matrix.rotateM(viewMatrix, 0, stereoAmount * 1.0f, 0, 1, 0);
				Matrix.rotateM(viewMatrix, 0, absoluteCameraTilt, 1, 0, 0);
				Matrix.rotateM(viewMatrix, 0, -absoluteCameraPan, 0, 1, 0);
				Matrix.translateM(viewMatrix, 0, -x, -z, -y);
				textureShader.setViewMatrix(viewMatrix);
				textureShader.setViewPosition(dampCameraDistanceVector.x, dampCameraDistanceVector.y, dampCameraDistanceVector.z);

				GLES20.glColorMask(true, false, false, true);

				// draw
				worldRendering.draw(textureShader, viewMatrix, time, GLWorld.DRAW_TYPE_LEFT_EYE);

				// transform according to camera - right eye
				dampCameraDistanceVector = new WWVector(0, dampCameraDistance, 0);
				dampCameraDistanceVector.add(stereoAmount * 0.02f * dampCameraDistance, 0, 0);
				dampCameraRotationVector = new WWVector(-absoluteCameraTilt, 0, absoluteCameraPan);
				rotate(dampCameraDistanceVector, dampCameraRotationVector);
				x = dampXCamera + dampCameraDistanceVector.x;
				y = dampYCamera + dampCameraDistanceVector.y;
				z = dampZCamera + dampCameraDistanceVector.z;
				clientModel.setDampedCameraLocation(x, y, z);
				Matrix.translateM(viewMatrix, 0, projectionMatrix, 0, 0, 0, 0);
				Matrix.rotateM(viewMatrix, 0, stereoAmount * -1.0f, 0, 1, 0);
				Matrix.rotateM(viewMatrix, 0, absoluteCameraTilt, 1, 0, 0);
				Matrix.rotateM(viewMatrix, 0, -absoluteCameraPan, 0, 1, 0);
				Matrix.translateM(viewMatrix, 0, -x, -z, -y);
				textureShader.setViewMatrix(viewMatrix);
				textureShader.setViewPosition(dampCameraDistanceVector.x, dampCameraDistanceVector.y, dampCameraDistanceVector.z);

				GLES20.glColorMask(false, true, true, true);

				// draw again
				GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
				worldRendering.draw(textureShader, viewMatrix, time, GLWorld.DRAW_TYPE_RIGHT_EYE);

				GLES20.glColorMask(true, true, true, true);

			} else {

				// transform according to camera
				WWVector dampCameraDistanceVector = new WWVector(0, dampCameraDistance, 0);
				WWVector dampCameraRotationVector = new WWVector(-absoluteCameraTilt, 0, absoluteCameraPan);
				rotate(dampCameraDistanceVector, dampCameraRotationVector);
				float x = dampXCamera + dampCameraDistanceVector.x;
				float y = dampYCamera + dampCameraDistanceVector.y;
				float z = dampZCamera + dampCameraDistanceVector.z;
				clientModel.setDampedCameraLocation(x, y, z);
				Matrix.translateM(viewMatrix, 0, projectionMatrix, 0, 0, 0, 0);
				Matrix.rotateM(viewMatrix, 0, absoluteCameraTilt, 1, 0, 0);
				Matrix.rotateM(viewMatrix, 0, -absoluteCameraPan, 0, 1, 0);
				Matrix.translateM(viewMatrix, 0, -x, -z, -y);
				textureShader.setViewMatrix(viewMatrix);
				textureShader.setViewPosition(dampCameraDistanceVector.x, dampCameraDistanceVector.y, dampCameraDistanceVector.z);

				if (USE_DEPTH_SHADER) {
					// Do a depth-only draw
					GLES20.glColorMask(false, false, false, false);
					depthShader.setViewMatrix(viewMatrix);
					worldRendering.draw(depthShader, viewMatrix, time, GLWorld.DRAW_TYPE_SHADOW);
					// Followed by a full draw
					GLES20.glDepthFunc(GLES20.GL_LEQUAL);
					GLES20.glColorMask(true, true, true, true);
					GLES20.glDepthMask(false);
					worldRendering.draw(textureShader, viewMatrix, time, GLWorld.DRAW_TYPE_MONO);
					GLES20.glDepthMask(true);
					GLES20.glDepthFunc(GLES20.GL_LESS);
				} else {
					worldRendering.draw(textureShader, viewMatrix, time, GLWorld.DRAW_TYPE_MONO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// checkGlError();

		if (clientModel.world != null) {
			clientModel.world.setRendered(true);
		}
		// Note: Used to delay physics start till here, but this caused a physics thread leak (would restart physics while pausing world)
		world.setRendered(true);
	}

	public final void rotate(WWVector point, WWVector rotation) {

		float x = point.x;
		float y = point.y;
		float z = point.z;
		float r;
		float theta;
		float newTheta;

		float rotationX = rotation.x;
		float rotationY = rotation.y;
		float rotationZ = rotation.z;

		// Rotate around x axis
		if (rotationX != 0.0) {
			r = (float) Math.sqrt(y * y + z * z);
			theta = FastMath.atan2(y, z);
			newTheta = theta + TORADIAN * rotationX;
			y = r * (float) Math.sin(newTheta);
			z = r * (float) Math.cos(newTheta);
		}

		// Rotate around y axis
		if (rotationY != 0.0) {
			r = (float) Math.sqrt(x * x + z * z);
			theta = FastMath.atan2(x, z);
			newTheta = theta + TORADIAN * -rotationY;
			x = r * (float) Math.sin(newTheta);
			z = r * (float) Math.cos(newTheta);
		}

		// Rotate around z axis
		if (rotationZ != 0.0) {
			r = (float) Math.sqrt(x * x + y * y);
			theta = FastMath.atan2(x, y);
			newTheta = theta + TORADIAN * rotationZ;
			x = r * (float) Math.sin(newTheta);
			y = r * (float) Math.cos(newTheta);
		}

		point.x = x;
		point.y = y;
		point.z = z;
	}

	private void preRender(long time) {
		try {
			WWWorld world = clientModel.world;
			if (world == null) {
				// TODO delete all rendered objects?
			} else {
	
				// Check for world property changes
				long lastModifyTime = world.getLastModifyTime();
				if (world.getLastRenderingTime() < lastModifyTime) {
					// world.updateRendering();
					world.setLastRenderingTime(lastModifyTime);
				}
	
				WWObject avatar = clientModel.getAvatar();
	
				// Get avatar position
				WWVector avatarPosition = new WWVector();
				if (avatar != null) {
					avatar.getPosition(avatarPosition);
				}
	
				// position the camera
				positionCamera(time);
	
				// Three times a second, check for changes to the rendering and render/derender objects
				if ((nrenders % (333 / clientModel.getFrameRate())) == 0) {

					if (clearRenderingsNeeded) {
						System.out.println("AndroidRenderer.preRender: clear renderings entered");

						// synchronized (world) {
						System.out.println("AndroidRenderer.preRender: dropping renderings for all objects");
						WWObject[] objects = world.getObjects();
						for (int i = 0; i < objects.length; i++) {
							WWObject object = objects[i];
							if (object != null) {
								IRendering rendering = object.getRendering();
								if (rendering != null) {
									object.dropRendering();
								}
							}
						}

						System.out.println("AndroidRenderer.preRender: clearing rendering cache");
						((GLWorld) world.getRendering()).drawnOnce = false;
						geometryCache.clear();
						GLSurface.initializeVertexBuffers();
						((GLWorld) world.getRendering()).drawnOnce = false;
						((GLWorld) world.getRendering()).drawGroups = null;
						// textureCache.clear();
						nrenders = 0;
						clearRenderingsNeeded = false;
						// }

						System.out.println("AndroidRenderer.preRender: creating renderings for objects that are part of rendering groups");
						// note this is needed to make sure the surfaces are adjacent in the vertex buffer
						int largestGroup = 0;
						for (int i = 0; i <= world.lastObjectIndex; i++) {
							WWObject object = world.objects[i];
							if (object != null) {
								largestGroup = (object.group > largestGroup) ? object.group : largestGroup;
							}
						}
						for (int g = 1; g <= largestGroup; g++) {
							for (int i = 0; i <= world.lastObjectIndex; i++) {
								WWObject object = world.objects[i];
								if (object != null && object.group == g) {
									object.createRendering(this, time);
								}
							}
						}

						System.out.println("AndroidRenderer.preRender: clear renderings leave");
					}
				}
	
				WWObject[] objects = world.getObjects();
				for (int i = 0; i <= world.lastObjectIndex; i++) {
					WWObject object = objects[i];
					if (object != null) {

						// If the object is deleted
						if (object.deleted) {

							// Delete the rendering if it exists
							IRendering rendering = object.getRendering();
							if (rendering != null) {
								object.dropRendering();
							}

							// Else the object is not deleted
						} else {

							// If the object is within the rendering threshold (considering size), mark it for rendering
							if (object.parentId == 0) {
								float renderDistance = lastDeterminedCameraPosition.distanceFrom(object.getPosition()) / object.extent;
								if (renderDistance  > clientModel.world.getRenderingThreshold()) {
									object.renderit = false;
								} else {
									object.renderit = true;
									if (renderDistance > 100) {
										object.renderLod = WWObject.RENDER_LOD_NANO;
									} else if (renderDistance > 50) {
										object.renderLod = WWObject.RENDER_LOD_MICRO;
									} else if (renderDistance > 25) {
										object.renderLod = WWObject.RENDER_LOD_MINI;
									} else {
										object.renderLod = WWObject.RENDER_LOD_FULL;
									}
								}
							} else {
								WWObject parentObject = world.objects[object.parentId];
								object.renderit = parentObject.renderit;    // assumes parent preceeds child in creation order
								object.renderLod = parentObject.renderLod;
							}

							// If marked for rendering and a rendering doesn't exist, create it
							// Create the rendering if not created
							if (object.renderit && object.getRendering() == null) {
								//System.out.println("AndroidRenderer.prerender: creating rendering for "+object);
								object.createRendering(this, time);

								// If the new object is the user's avatar, set camera position on it
								if (clientModel.getAvatar() == object) {
									if (clientModel.getCameraObject() == null) {
										clientModel.setCameraObject(object);
									}
									if (clientModel.getCameraObject() == object) {
										if (clientModel.cameraInitiallyFacingAvatar) {
											clientModel.setCameraPan(180);
											clientModel.setCameraDistance(clientModel.initiallyFacingDistance);
											clientModel.setCameraTilt(30.0f);
										}
									}
									// clientModel.setCameraDistance(2.0f);
									// clientModel.setCameraTilt(5.0f);
									// clientModel.setCameraPan(0.0f);
								}
							}

						}
					} // object != null
				} // for all objects

				nrenders++;
	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void clearTextureCache() {
		textureCache = new HashMap<String, Integer>(); // clear doesn't work
		normalTextureCache = new HashMap<String, Integer>();
		hasAlphaCache = new HashMap<String, Boolean>();
	}

	@Override
	public final ISoundGenerator getSoundGenerator() {
		return soundGenerator;
	}

	private WWVector lastDeterminedCameraPosition;

	/**
	 * Move the camera to the current camera position, pan, tilt and distance. This also dampens the camera movements, to make the shifting of the camera more obvious and natural.
	 */
	protected final void positionCamera(long time) {
		if (clientModel.world == null) {
			return;
		}
		WWVector cameraPoint = new WWVector();
		float cameraPan = clientModel.getCameraPan();
		float cameraPanVelocity = clientModel.getCameraPanVelocity();
		float cameraTilt = clientModel.getCameraTilt();
		float cameraTiltVelocity = clientModel.getCameraTiltVelocity();
		float cameraDistance = clientModel.getCameraDistance();
		float cameraDistanceVelocity = clientModel.getCameraDistanceVelocity();
		WWObject cameraObject = clientModel.getCameraObject();

		WWObject avatar = clientModel.getAvatar();
		WWQuaternion cameraObjectRotation;
		if (cameraObject != null) {
			cameraObjectRotation = cameraObject.getAbsoluteAnimatedRotation(time);
		} else {
			cameraObjectRotation = new WWQuaternion();
		}
		float cameraSlideX = clientModel.getCameraSlideX();
		float cameraSlideY = clientModel.getCameraSlideY();
		float cameraSlideZ = clientModel.getCameraSlideZ();
		float cameraSlideXVelocity = clientModel.getCameraSlideXVelocity();
		float cameraSlideYVelocity = clientModel.getCameraSlideYVelocity();
		float cameraSlideZVelocity = clientModel.getCameraSlideZVelocity();

		// Apply camera velocities
		if (lastCameraAdjustTime > 0 && clientModel.world != null) {
			float timeDelta = (time - lastCameraAdjustTime) / 1000.0f;
			cameraSlideX += cameraSlideXVelocity * timeDelta;
			cameraSlideY += cameraSlideYVelocity * timeDelta;
			cameraSlideZ += cameraSlideZVelocity * timeDelta;
			cameraPan += cameraPanVelocity * timeDelta;
			cameraTilt += cameraTiltVelocity * timeDelta;
			cameraTilt = FastMath.min(89, FastMath.max(-89, cameraTilt));
			cameraDistance += (cameraDistance * cameraDistanceVelocity) * timeDelta;
			clientModel.setCameraSlide(cameraSlideX, cameraSlideY, cameraSlideZ);
			clientModel.setCameraPan(cameraPan);
			clientModel.setCameraTilt(cameraTilt);
			clientModel.setCameraDistance(cameraDistance);
		}
		lastCameraAdjustTime = time;

		// Adjust camera position based on tracking object
		if (cameraObject != null) {
			cameraObject.getAbsoluteAnimatedPosition(cameraPoint, time);
			if (avatar == cameraObject && cameraDistance < 10) {
				WWVector point = new WWVector(0, 0, 0.75f);
				cameraObject.rotate(point, cameraObjectRotation);
				cameraPoint.add(point); // so avatar is in lower part of screen
			}
		}

		// Adjust camera position based on camera slide
		float slideCameraPointX = cameraPoint.x + (float) Math.cos(TORADIAN * cameraPan) * cameraSlideX - (float) Math.sin(TORADIAN * cameraPan) * (float) Math.sin(TORADIAN * cameraTilt) * cameraSlideY
				- (float) Math.sin(TORADIAN * cameraPan) * (float) Math.cos(TORADIAN * cameraTilt) * cameraSlideZ;
		float slideCameraPointY = cameraPoint.y - (float) Math.sin(TORADIAN * cameraPan) * cameraSlideX - (float) Math.cos(TORADIAN * cameraPan) * (float) Math.sin(TORADIAN * cameraTilt) * cameraSlideY
				- (float) Math.cos(TORADIAN * cameraPan) * (float) Math.cos(TORADIAN * cameraTilt) * cameraSlideZ;
		float slideCameraPointZ = cameraPoint.z + (float) Math.cos(TORADIAN * cameraTilt) * cameraSlideY - (float) Math.sin(TORADIAN * cameraTilt) * cameraSlideZ;

		// limit camera according to unpenetratable objects
		float limitedCameraDistance = cameraDistance;
		if (clientModel.limitCameraDistance) {
			if (lastLimitedCameraDistance < cameraDistance) {
				limitedCameraDistance = Math.min(cameraDistance, lastLimitedCameraDistance / 0.5f);
			} else {
				limitedCameraDistance = cameraDistance;
			}
			WWObject[] objects = clientModel.world.getObjects();
			int lastObjectIndex = clientModel.world.lastObjectIndex;
			WWVector cameraLocation = new WWVector();
			WWVector position = new WWVector();
			WWQuaternion rotation = new WWQuaternion();
			WWVector tempPoint = new WWVector();
			WWVector penetrationVector = new WWVector();
			boolean penetrated = true;
			while (penetrated) {
				penetrated = false;
				for (int i = 0; i <= lastObjectIndex; i++) {
					WWObject object = objects[i];
					if (object != null && !object.deleted && !object.penetratable && object.solid && !object.phantom) {
						if (cameraObject != null && avatar != null && (cameraObject == avatar || avatar.isDescendant(cameraObject))) {
							cameraLocation.x = slideCameraPointX + limitedCameraDistance * (float) Math.sin(TORADIAN * (cameraPan + cameraObjectRotation.getYaw())) * (float) Math.cos(TORADIAN * (cameraTilt + cameraObjectRotation.getPitch()));
							if (cameraDistance < 10) {
								cameraLocation.y = slideCameraPointY + limitedCameraDistance * (float) Math.cos(TORADIAN * (cameraPan + cameraObjectRotation.getYaw())) * (float) Math.cos(TORADIAN * (cameraTilt + cameraObjectRotation.getPitch()));
								cameraLocation.z = slideCameraPointZ + (float) Math.sin(TORADIAN * cameraTilt) * limitedCameraDistance;
							} else {
								cameraLocation.y = slideCameraPointY + limitedCameraDistance * (float) Math.cos(TORADIAN * cameraPan) * (float) Math.cos(TORADIAN * cameraTilt);
								cameraLocation.z = slideCameraPointZ + (float) Math.sin(TORADIAN * cameraTilt) * limitedCameraDistance;
							}
						} else {
							cameraLocation.x = slideCameraPointX + limitedCameraDistance * (float) Math.sin(TORADIAN * cameraPan) * (float) Math.cos(TORADIAN * cameraTilt);
							cameraLocation.y = slideCameraPointY + limitedCameraDistance * (float) Math.cos(TORADIAN * cameraPan) * (float) Math.cos(TORADIAN * cameraTilt);
							cameraLocation.z = slideCameraPointZ + (float) Math.sin(TORADIAN * cameraTilt) * limitedCameraDistance;
						}
						// First, see if the objects are "close". If they are, it is worth
						// determining if they actually overlap
						object.getPosition(position);
						float extent = object.extent;
						if (object.parentId != 0 || (Math.abs(position.x - cameraLocation.x) < extent && Math.abs(position.y - cameraLocation.y) < extent && Math.abs(position.z - cameraLocation.z) < extent)) {
							object.getRotation(rotation);
							object.getPenetration(cameraLocation, position, rotation, time, tempPoint, penetrationVector);
							if (penetrationVector != null && penetrationVector.length() > 0 && limitedCameraDistance > 0.25) {
								if (Math.abs(penetrationVector.z) > FastMath.max(Math.abs(penetrationVector.x), Math.abs(penetrationVector.y)) && cameraTilt < 45.0) {
									cameraTilt += 15.0;
								} else {
									limitedCameraDistance = limitedCameraDistance * 0.5f;
								}
								penetrated = true;
								// System.out.println("penetrated: " + object.hashCode() + " " + cameraLocation + " " + position + " " + rotation + " " + penetrationVector);
							}
						}
					}
				}
			}
			lastDeterminedCameraPosition = cameraLocation;
		}
		limitedCameraDistance = FastMath.max(clientModel.minCameraDistance, limitedCameraDistance);
		lastLimitedCameraDistance = limitedCameraDistance;

		// Dampen camera, to give the user a better understanding of the position change
		if (cameraObject != null && clientModel.cameraDampRate > 0) {
			if (clientModel.world.dampenCamera()) {
				dampXCamera = (slideCameraPointX + clientModel.cameraDampRate * dampXCamera) / (clientModel.cameraDampRate + 1);
				dampYCamera = (slideCameraPointY + clientModel.cameraDampRate * dampYCamera) / (clientModel.cameraDampRate + 1);
				dampZCamera = (slideCameraPointZ + clientModel.cameraDampRate * dampZCamera) / (clientModel.cameraDampRate + 1);
			} else {
				// dampXCamera = (2 * slideCameraPointX + dampXCamera) / 3.0f;
				// dampYCamera = (2 * slideCameraPointY + dampYCamera) / 3.0f;
				// dampZCamera = (2 * slideCameraPointZ + dampZCamera) / 3.0f;

				WWVector cameraVelocity = cameraObject.getVelocity();

				dampCameraVelocityX = (cameraVelocity.x + clientModel.cameraDampRate * dampCameraVelocityX) / (clientModel.cameraDampRate + 1);
				dampCameraVelocityY = (cameraVelocity.y + clientModel.cameraDampRate * dampCameraVelocityY) / (clientModel.cameraDampRate + 1);
				dampCameraVelocityZ = (cameraVelocity.z + clientModel.cameraDampRate * dampCameraVelocityZ) / (clientModel.cameraDampRate + 1);

				dampXCamera = slideCameraPointX - 0.01f * dampCameraVelocityX;
				dampYCamera = slideCameraPointY - 0.01f * dampCameraVelocityY;
				dampZCamera = slideCameraPointZ - 0.01f * dampCameraVelocityZ;
			}

		} else {
			dampXCamera = slideCameraPointX;
			dampYCamera = slideCameraPointY;
			dampZCamera = slideCameraPointZ; // to avoid shaky's
		}

		float dampCameraPan = (cameraPan + clientModel.cameraDampRate * clientModel.getDampedCameraPan()) / (clientModel.cameraDampRate + 1);
		float dampCameraTilt = (cameraTilt + clientModel.cameraDampRate * clientModel.getDampedCameraTilt()) / (clientModel.cameraDampRate + 1);
		if (cameraObject != null && avatar != null && (avatar == cameraObject || avatar.isDescendant(cameraObject))) {
			absoluteCameraPan = dampCameraPan + cameraObjectRotation.getYaw();
			if (cameraDistance < 10) {
				if (avatar == cameraObject) {
					absoluteCameraTilt = dampCameraTilt - cameraObjectRotation.getPitch();
				} else {
					absoluteCameraTilt = cameraTilt - cameraObjectRotation.getPitch();
				}
			} else {
				absoluteCameraTilt = dampCameraTilt;
			}
		} else {
			absoluteCameraPan = dampCameraPan;
			absoluteCameraTilt = dampCameraTilt;
		}
		dampCameraDistance = (limitedCameraDistance + clientModel.cameraDampRate * dampCameraDistance) / (clientModel.cameraDampRate + 1);
		clientModel.setDampedCameraLocation(dampXCamera, dampYCamera, dampZCamera);
		clientModel.setDampedCameraTilt(dampCameraTilt);
		clientModel.setDampedCameraPan(dampCameraPan);
	}

	public void initializeCameraPosition() {
		dampXCamera = clientModel.getCameraSlideX();
		dampYCamera = clientModel.getCameraSlideY();
		dampZCamera = clientModel.getCameraSlideZ();
		clientModel.setDampedCameraPan(clientModel.getCameraPan());
		clientModel.setDampedCameraTilt(clientModel.getCameraTilt());
		dampCameraDistance = clientModel.getCameraDistance();
		lastCameraAdjustTime = 0;
		lastLimitedCameraDistance = clientModel.getCameraDistance();
	}

	public WWObject waitForPickingDraw(WWObject object, int px, int py) {
		pickX = px;
		pickY = py;
		pickedObject = object;
		WWWorld world = clientModel.world;
		if (threadWaitingForPick == null && world != null && world.getRendered()) {
			threadWaitingForPick = Thread.currentThread();
			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait();
				}
			} catch (InterruptedException e) {
			}
		}
		return pickedObject;
	}

	/**
	 * Creates a texture and frame buffer object (FBO) used for a shadow map. Returns the framebuffer id for it.
	 */
	private void setupShadowMap() {
		System.out.println(">AndroidRenderer.setupShadowMap");
		int texW = SHADOW_MAP_WIDTH;
		int texH = SHADOW_MAP_HEIGHT;

		// generate and bind a frame buffer for the shadow map
		int[] shadowMapFb = new int[1];
		GLES20.glGenFramebuffers(1, shadowMapFb, 0);
		shadowMapFrameBufId = shadowMapFb[0];
		GLES20.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, shadowMapFrameBufId);

		// texture 1 is used for the shadow map so make it active now
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

		// generate and bind a depth texture for the shadow map
		int[] shadowMapTex = new int[1];
		GLES20.glGenTextures(1, shadowMapTex, 0);
		shadowMapTextureId = shadowMapFb[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapTextureId);

		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, texW, texH, 0, GLES20.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_INT, null);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, shadowMapTextureId, 0);

		// configure the depth texture for optimal shadow mapping
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT); // GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT); // GL_CLAMP_TO_EDGE);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapTextureId);

		// restore back to the default framebuffer and texture 0
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		System.out.println("<AndroidRenderer.setupShadowMap");
	}

	protected float[] tempMatrix = new float[16];

	/**
	 * Generates a shadow map for the moving objects.
	 */
	private void generateShadowMap(long time, float[] viewMatrix) {
	
		// bind the shadow framebuffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowMapFrameBufId);
	
		// Cull front faces for shadow generation
		GLES20.glCullFace(GLES20.GL_FRONT);
	
		// viewport
		GLES20.glViewport(0, 0, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT);
	
		// Clear buffers
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT); // only the depth buffer is used
	
		// View from the light's perspective
		float FAR_OUT = 1000000.0f;
		WWVector sunPosition = clientModel.world.getSunDirection().normalize().scale(FAR_OUT);
		float ratio = (float) SHADOW_MAP_WIDTH / SHADOW_MAP_HEIGHT;
		float[] sunProjectionMatrix = new float[16];
		float zoom = 0.08f / FastMath.max(0.25f, (float) Math.sqrt(clientModel.getCameraDistance() / (1.0f + clientModel.getCameraTilt() / 30.0f))); // + (15 - clientModel.getCameraTilt() / 6.0f));
		Matrix.frustumM(sunProjectionMatrix, 0, -ratio / zoom, ratio / zoom, -1 / zoom, 1 / zoom, FAR_OUT - 1000.0f, FAR_OUT + 1000.0f); // sunPosition.length() * 0.99f, 100000.0f);
	
		WWObject cameraObject = clientModel.getCameraObject();
		if (cameraObject != null) {
			lastShadowCameraViewPosition = cameraObject.getPosition().clone();
			lastShadowCameraViewPosition.x -= FastMath.sinDeg(clientModel.getCameraPan() + cameraObject.getRotation().getYaw()) * 20 * (1.0f - clientModel.getCameraTilt() / 90.0f);
			lastShadowCameraViewPosition.y -= FastMath.cosDeg(clientModel.getCameraPan() + cameraObject.getRotation().getYaw()) * 20 * (1.0f - clientModel.getCameraTilt() / 90.0f);
			Matrix.setLookAtM(sunViewMatrix, 0, //
					lastShadowCameraViewPosition.x + sunPosition.x, sunPosition.z + lastShadowCameraViewPosition.z, lastShadowCameraViewPosition.y + sunPosition.y, // sun position
					lastShadowCameraViewPosition.x, lastShadowCameraViewPosition.z, lastShadowCameraViewPosition.y, // center (where the light is looking at)
					0, 1, 0 // up vector
			);
		} else {
			Matrix.setLookAtM(sunViewMatrix, 0, //
					sunPosition.x, sunPosition.z, sunPosition.y, // sun position
					0, 0, 0, // center (where the light is looking at)
					0, 1, 0 // up vector
			);
		}
	
		// modelviewprojection matrix
		Matrix.multiplyMM(sunViewMatrix, 0, sunProjectionMatrix, 0, sunViewMatrix, 0);
		shadowMapShader.setSunViewMatrix(sunViewMatrix);
		textureShader.setSunViewMatrix(sunViewMatrix);
	
		GLES20.glColorMask(false, false, false, false); // no sense drawing colors
	
		// Draw the objects
		if (clientModel.world != null) {
			GLWorld worldRendering = (GLWorld) clientModel.world.getRendering();
			worldRendering.draw(shadowMapShader, viewMatrix, time, GLWorld.DRAW_TYPE_SHADOW);
		}
	
		// restore settings to what's needed for normal draw
		GLES20.glColorMask(true, true, true, true);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
		GLES20.glCullFace(GLES20.GL_BACK);
	}

	/**
	 * Perform a special frame draw that draws each object in a different color, to determine what object is at a particular pixel.
	 */
	public void pickingDraw(long time, float[] viewMatrix) {
		// TODO GLES20.glEnableClientState(GLES20.GL_VERTEX_ARRAY);
		// TODO GLES20.glDisableClientState(GLES20.GL_NORMAL_ARRAY);
		// TODO GLES20.glDisableClientState(GLES20.GL_TEXTURE_COORD_ARRAY);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glFrontFace(GLES20.GL_CW);
		// GLES20.glMatrixMode(GLES20.GL_MODELVIEW);
		// GLES20.glLoadIdentity();
	
		// TODO GLES20.glDisable(GLES20.GL_LIGHTING);
		// GLES20.glDisable(GLES20.GL_FOG);
		textureShader.setFogDensity(0.0f);
	
		// Clear to black
		GLES20.glClearColor(0, 0, 0, 1);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	
		// transform according to camera
		WWVector dampCameraDistanceVector = new WWVector(0, dampCameraDistance, 0);
		WWVector dampCameraRotationVector = new WWVector(-absoluteCameraTilt, 0, absoluteCameraPan);
		rotate(dampCameraDistanceVector, dampCameraRotationVector);
		float x = dampXCamera + dampCameraDistanceVector.x;
		float y = dampYCamera + dampCameraDistanceVector.y;
		float z = dampZCamera + dampCameraDistanceVector.z;
		clientModel.setDampedCameraLocation(x, y, z);
		Matrix.translateM(viewMatrix, 0, projectionMatrix, 0, 0, 0, 0);
		Matrix.rotateM(viewMatrix, 0, absoluteCameraTilt, 1, 0, 0);
		Matrix.rotateM(viewMatrix, 0, -absoluteCameraPan, 0, 1, 0);
		Matrix.translateM(viewMatrix, 0, -x, -z, -y);
		textureShader.setViewMatrix(viewMatrix);
	
		GLWorld worldRendering = (GLWorld) clientModel.world.getRendering();
		worldRendering.draw(textureShader, viewMatrix, time, GLWorld.DRAW_TYPE_PICKING);
		GLES20.glFlush();
		GLES20.glFinish();
	
		int objectId = getObjectIdAtPixel(pickX, pickY);
		if (objectId == 0) {
			objectId = getObjectIdAtPixel(pickX - 2, pickY);
			if (objectId == 0) {
				objectId = getObjectIdAtPixel(pickX + 2, pickY);
				if (objectId == 0) {
					objectId = getObjectIdAtPixel(pickX, pickY - 2);
					if (objectId == 0) {
						objectId = getObjectIdAtPixel(pickX, pickY + 2);
						if (objectId == 0) {
							objectId = getObjectIdAtPixel(pickX - 2, pickY - 2);
							if (objectId == 0) {
								objectId = getObjectIdAtPixel(pickX + 2, pickY - 2);
								if (objectId == 0) {
									objectId = getObjectIdAtPixel(pickX - 2, pickY + 2);
									if (objectId == 0) {
										objectId = getObjectIdAtPixel(pickX + 2, pickY + 2);
										if (objectId == 0) {
											objectId = getObjectIdAtPixel(pickX - 4, pickY);
											if (objectId == 0) {
												objectId = getObjectIdAtPixel(pickX + 4, pickY);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if (objectId == 0) {
			pickedObject = null;
		} else {
			pickedObject = clientModel.world.objects[objectId];
		}
		Thread thread = threadWaitingForPick;
		threadWaitingForPick = null;
		synchronized (thread) {
			thread.notify();
		}
	}

	private int getObjectIdAtPixel(int x, int y) {
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4);
		pixelBuffer.order(ByteOrder.nativeOrder());
		GLES20.glPixelStorei(GLES20.GL_PACK_ALIGNMENT, 1);
		GLES20.glReadPixels(x, y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		pixelBuffer.rewind();
		byte b[] = new byte[4];
		pixelBuffer.get(b);
		// System.out.println("x=" + x + " y=" + y + " " + b[0] + " " + b[1] + " " + b[2] + " " + b[3]);
		b[0] >>= 4;
		b[0] &= 0xF;
		b[1] >>= 4;
		b[1] &= 0xF;
		b[2] >>= 4;
		b[2] &= 0xF;
		int objectId = (b[0] << 8) + (b[1] << 4) + b[2];
		return objectId;
	}

	@Override
	public IRendering createParticlesRendering(WWParticleEmitter particles, long worldTime) {
		return new GLParticleEmitter(this, particles, worldTime);
	}

}
