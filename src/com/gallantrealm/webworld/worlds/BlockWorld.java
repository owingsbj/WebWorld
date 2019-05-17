package com.gallantrealm.webworld.worlds;

import java.io.Serializable;
import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.PauseAction;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWMesh;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWTranslucency;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

public class BlockWorld extends BaseEggWorld {

	WWMesh ground;
	WWObject avatar;
	ArrayList<WWObject> blocks = new ArrayList<WWObject>();

	public BlockWorld(String saveWorldFileName, String avatarName) {
		super(saveWorldFileName);

		// The basic world (like earth)
		setName("Block");
		setGravity(9.8f); // earth gravity
		setFogDensity(0.3f);
		long time = getWorldTime();
		setCreateTime(time);
		setLastModifyTime(time);

		// the ground
		ground = new WWMesh(); // for the ground
		ground.setName("ground");
		ground.setImpactSound("grass");
		// ground.setSlidingSound("movingGrass");
		ground.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x00E000)); // green, like grass
		ground.setColor(WWSimpleShape.SIDE_ALL, new WWColor(0x404000)); // all others brown
		ground.setSize(new WWVector(1000, 1000, 250));
		ground.setPosition(new WWVector(0, 0, -10));
		int meshSize = 200;
		ground.setMeshSize(meshSize, meshSize);
		// - add a few peaks
		for (int i = 0; i < 100; i++) {
			int x = (int) (meshSize * FastMath.random());
			int y = (int) (meshSize * FastMath.random());
			float z = FastMath.random() * 0.0002f;
			int baseSize = (int) (FastMath.random() * 20) + 5;
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
		for (int i = 0; i < 200; i++) {
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

// - flatten (used on testing)
//		for (int i = 0; i <= meshSize; i++) {
//			for (int j = 0; j <= meshSize; j++) {
//				ground.setMeshPoint(i, j, 0.6f);
//			}
//		}
		ground.setTextureURL(WWSimpleShape.SIDE_TOP, "grass");
		ground.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.001f);
		ground.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.001f);
		addObject(ground);

		// the water
		WWTranslucency water = new WWTranslucency();
		water.setName("water");
		water.setPenetratable(true);
		water.setInsideLayerDensity(0.25f);
		water.setPosition(new WWVector(0, 0, -25 - 25 * (float) Math.random()));
		water.setSize(new WWVector(1000, 1000, 50));
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
		water.setFullBright(SIDE_INSIDE1, true);
		water.setFullBright(SIDE_INSIDE2, true);
		water.setFullBright(SIDE_INSIDE3, true);
		water.setFullBright(SIDE_INSIDE4, true);
		addObject(water);

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

		// air (to provide friction and balloon floating)
		WWSimpleShape air = new WWSphere();
		air.setName("air");
		air.setPenetratable(true);
		air.setPosition(new WWVector(0, 0, -100));
		air.setSize(new WWVector(500, 5000, 5000));
		air.setTransparency(WWSimpleShape.SIDE_ALL, 1.0f);
		air.setCutoutStart(0.5f); // half dome
		air.setSolid(false);
		air.setRotation(new WWVector(0, 90, 0));
		air.setFriction(0.1f);
		air.setDensity(0.0001f);
		addObject(air);

		// first user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		if (AndroidClientModel.getClientModel().isSimpleRendering()) {
			final WWSimpleShape avatarShadow = new WWBox(1, 1, 0.25f);
			avatarShadow.setPhantom(true);
			avatarShadow.setTransparency(SIDE_ALL, 1);
			avatarShadow.setTransparency(SIDE_TOP, 0.5f);
			avatarShadow.setTextureURL(SIDE_TOP, "eggshadow");
			avatarShadow.setPosition(0, 0, -avatar.sizeZ / 2.0f);
			avatarShadow.setParent(avatar);
			addObject(avatarShadow);
			avatarShadow.addBehavior(new WWAnimation() {

				@Override
				public void getAnimatedPosition(WWObject object, WWVector position, long time) {
					if (object == avatarShadow) {
						WWVector p = avatar.getPosition();
						WWVector r = avatar.getRotation();
						float meshValue = ground.getMeshValue(p);
						if (meshValue <= 10) { // in water
							position.z -= 1000; // hide shadow
						} else {
							float jumpHeight = (p.z - meshValue - ground.getPosition().z) - avatar.sizeZ / 2.0f;
							// System.out.println(jumpHeight);
							position.z -= jumpHeight / 4.0f;
							position.y += (jumpHeight + 2) / 4.0f * FastMath.cos(FastMath.TORADIAN * (r.z - 45));
							position.x -= (jumpHeight + 2) / 4.0f * FastMath.sin(FastMath.TORADIAN * (r.z - 45));
						}
					}
				}

				@Override
				public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
				}

			});
		}
		avatar.setPosition(new WWVector(0, 0, 25));
		user.setAvatarId(avatar.getId());

		worldActions = new WWAction[] { new PauseAction(), new ChangeViewAction() };
		setAvatarActions(new WWAction[] { new JumpAction(), new SwimAction(), new DigAction(), new PileAction(), new BlockAction() });

	}

	public class DigAction extends WWAction implements Serializable {
		@Override
		public String getName() {
			return "Dig";
		}

		@Override
		public void start() {
			WWVector cell = ground.getMeshCell(avatar.getPosition());
			int i = (int) cell.x;
			int j = (int) cell.y;
			ground.setMeshPoint(i, j, ground.getMeshPoint(i, j) - 0.01f);
			ground.setMeshPoint(i + 1, j, ground.getMeshPoint(i + 1, j) - 0.01f);
			ground.setMeshPoint(i, j + 1, ground.getMeshPoint(i, j + 1) - 0.01f);
			ground.setMeshPoint(i + 1, j + 1, ground.getMeshPoint(i + 1, j + 1) - 0.01f);
			ground.getRendering().updateRendering();
			// ((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	public class PileAction extends WWAction implements Serializable {
		@Override
		public String getName() {
			return "Pile";
		}

		@Override
		public void start() {
			WWVector cell = ground.getMeshCell(avatar.getPosition());
			int i = (int) cell.x;
			int j = (int) cell.y;
			ground.setMeshPoint(i, j, ground.getMeshPoint(i, j) + 0.01f);
			ground.setMeshPoint(i + 1, j, ground.getMeshPoint(i + 1, j) + 0.01f);
			ground.setMeshPoint(i, j + 1, ground.getMeshPoint(i, j + 1) + 0.01f);
			ground.setMeshPoint(i + 1, j + 1, ground.getMeshPoint(i + 1, j + 1) + 0.01f);
			ground.getRendering().updateRendering();
			// ((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
		}
	}

	public class JumpAction extends WWAction implements Serializable {

		@Override
		public String getName() {
			return "Jump";
		}

		@Override
		public void start() {
			WWObject avatar = AndroidClientModel.getClientModel().getAvatar();
			WWVector velocity = avatar.getVelocity();
			velocity.z = 5;
			avatar.setVelocity(velocity);
		}

	}

	public class SwimAction extends WWAction implements Serializable {

		boolean swimming;

		@Override
		public String getName() {
			return "Swim";
		}

		@Override
		public void start() {
			WWObject avatar = AndroidClientModel.getClientModel().getAvatar();
			if (!swimming) {
				avatar.setDensity(1);
				swimming = true;
			} else {
				avatar.setDensity(0.1f);
				swimming = false;
			}
		}

	}

	public class BlockAction extends WWAction implements Serializable {

		@Override
		public String getName() {
			return "Block";
		}

		@Override
		public void start() {
			WWVector position = avatar.getPosition();
			position.x = Math.round(position.x / 4f) * 4f;
			position.y = Math.round(position.y / 4f) * 4f;
			position.z = Math.round(position.z / 2f - 0.5f) * 2f + 1.25f;
			Object object = getBlockOrPosition(position);
			if (object instanceof WWVector) {
				WWVector blockPosition = (WWVector) object;
				WWBox block = new WWBox();
				block.setMonolithic(true);
				block.setTextureURL(SIDE_ALL, "brick");
				block.setSize(4, 4, 2);
				block.setPosition(blockPosition);
				addObject(block);
				blocks.add(block);
				AndroidClientModel.getClientModel().cameraInitiallyFacingAvatar = false; // needed to keep refacing egg
//				WWBox blockShadow = new WWBox(4, 4, 0.25f);
//				blockShadow.setPhantom(true);
//				blockShadow.setTransparency(SIDE_ALL, 1);
//				blockShadow.setTransparency(SIDE_TOP, 0.5f);
//				blockShadow.setTextureURL(SIDE_TOP, "blockshadow");
//				blockShadow.setPosition(blockPosition);
//				float z = ground.getMeshValue(blockPosition) - 10;
//				blockShadow.setPosition(blockPosition.x + (blockPosition.z - z) / 4, blockPosition.y + (blockPosition.z - z) / 4, z);
//				addObject(blockShadow);
				((AndroidRenderer) getRendering().getRenderer()).clearRenderings();
			} else {
				WWObject block = (WWObject) object;
				removeObject(block.getId());
				blocks.remove(block);
			}
		}

		private Object getBlockOrPosition(WWVector position) {

			// Check the block directly ahead of the avatar
			float x = 0;
			if (-FastMath.sin(FastMath.TORADIAN * avatar.getRotation().z) > 0.7) {
				x = 4;
			} else if (-FastMath.sin(FastMath.TORADIAN * avatar.getRotation().z) < -0.7) {
				x = -4;
			}
			float y = 0;
			if (-FastMath.cos(FastMath.TORADIAN * avatar.getRotation().z) > 0.7) {
				y = 4;
			} else if (-FastMath.cos(FastMath.TORADIAN * avatar.getRotation().z) < -0.7) {
				y = -4;
			}
			position.x += x;
			position.y += y;

			WWObject block = blockAtPosition(position);
			if (block != null) {
				return block;
			} else {
				return position;
			}

		}

		private WWObject blockAtPosition(WWVector position) {
			boolean atPosition = false;
			for (int i = 0; i < blocks.size(); i++) {
				WWObject block = blocks.get(i);
				if (block.getPosition().subtract(position).length() < 1) {
					return block;
				}
			}
			return null;
		}
	}

	@Override
	public boolean usesAccelerometer() {
		return true;
	}

	@Override
	public float[] getMoveXTurn() {
		return new float[] { -60, -30, -20, -10, 0, 0, 0, 10, 20, 30, 60 };
	}

	@Override
	public float[] getMoveYThrust() {
		return new float[] { -4, -3, -2, -1, 0, 0, 0, 2, 4, 6, 8 };
	}

//	public boolean supportsOpenGLES20() {
//		return false;
//	}
}
