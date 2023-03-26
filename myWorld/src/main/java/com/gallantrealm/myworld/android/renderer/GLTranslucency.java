package com.gallantrealm.myworld.android.renderer;

import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.model.SideAttributes;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWTranslucency;
import com.gallantrealm.myworld.model.WWVector;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Creates a primitive with a layer (cutout1) that is shown in front of the camera when  the camera is positioned within the object.  This layer when given partial transparency, forms the illusion of a translucent substance.
 * This is useful for water or other nearly clear objects, to give the simplest illusion of murky depth.
 */
public class GLTranslucency extends GLObject  {

	int sideLayers;
	int topLayers;
	float sizeX;
	float sizeY;
	float sizeZ;
	float layerDensity;
	float layerTransparency;
	int layerColor;

	public GLTranslucency(AndroidRenderer renderer, WWObject object, long worldTime) {
		super(renderer, object, worldTime);
		buildRendering();
	}

	public void buildRendering() {
		this.sizeX = object.size.x;
		this.sizeY = object.size.y;
		this.sizeZ = object.size.z;
		this.layerDensity = ((WWTranslucency) object).getInsideLayerDensity();
		this.layerTransparency = ((WWTranslucency) object).getInsideTransparency();
		this.layerColor = ((WWTranslucency) object).getInsideColor();

		sideLayers = (int) (layerDensity * sizeY);
		topLayers = (int) (layerDensity * sizeZ);

		// Create top (use a 10x10 matrix for better fog look on large transparencies like water
		GLSurface topGeometry = new GLSurface(11, 11);
		for (int y = 0; y < 11; y++) {
			for (int x = 0; x < 11; x++) {
				topGeometry.setVertex(x, y, x * sizeX / 10 - sizeX / 2, sizeZ / 2, y * sizeY / 10 - sizeY / 2);
			}
		}
		topGeometry.generateNormals();
		this.setSide(WWObject.SIDE_TOP, topGeometry);

		// Create the inside top, no normals
		GLSurface insideTopGeometry = new GLSurface(11, 11);
		for (int y = 0; y < 11; y++) {
			for (int x = 0; x < 11; x++) {
				insideTopGeometry.setVertex(10 - x, y, x * sizeX / 10 - sizeX / 2, sizeZ / 2, y * sizeY / 10 - sizeY / 2);
			}
		}
		this.setSide(WWObject.SIDE_INSIDE_TOP, insideTopGeometry);

		// Create the inside bottom, again no normals
		GLSurface insideBottomGeometry = new GLSurface(11, 11);
		for (int y = 0; y < 11; y++) {
			for (int x = 0; x < 11; x++) {
				insideBottomGeometry.setVertex(x, y, x * sizeX / 10 - sizeX / 2, -sizeZ / 2, y * sizeY / 10 - sizeY / 2);
			}
		}
		this.setSide(WWObject.SIDE_INSIDE_BOTTOM, insideBottomGeometry);

		// Create the inside sides (inside1-inside4), no normals.  These are drawn "far away" as if fully hollow
		GLSurface inside1Geometry = new GLSurface(2, 2);
		inside1Geometry.setVertex(0, 0, -sizeX / 2, -sizeZ / 2, sizeY / 2);
		inside1Geometry.setVertex(1, 0, sizeX / 2, -sizeZ / 2, sizeY / 2);
		inside1Geometry.setVertex(0, 1, -sizeX / 2, sizeZ / 2, sizeY / 2);
		inside1Geometry.setVertex(1, 1, sizeX / 2, sizeZ / 2, sizeY / 2);
		this.setSide(WWObject.SIDE_INSIDE1, inside1Geometry);
		GLSurface inside2Geometry = new GLSurface(2, 2);
		inside2Geometry.setVertex(0, 0, -sizeX / 2, -sizeZ / 2, -sizeY / 2);
		inside2Geometry.setVertex(1, 0, -sizeX / 2, -sizeZ / 2, sizeY / 2);
		inside2Geometry.setVertex(0, 1, -sizeX / 2, sizeZ / 2, -sizeY / 2);
		inside2Geometry.setVertex(1, 1, -sizeX / 2, sizeZ / 2, sizeY / 2);
		this.setSide(WWObject.SIDE_INSIDE2, inside2Geometry);
		GLSurface inside3Geometry = new GLSurface(2, 2);
		inside3Geometry.setVertex(0, 0, sizeX / 2, -sizeZ / 2, -sizeY / 2);
		inside3Geometry.setVertex(1, 0, -sizeX / 2, -sizeZ / 2, -sizeY / 2);
		inside3Geometry.setVertex(0, 1, sizeX / 2, sizeZ / 2, -sizeY / 2);
		inside3Geometry.setVertex(1, 1, -sizeX / 2, sizeZ / 2, -sizeY / 2);
		this.setSide(WWObject.SIDE_INSIDE3, inside3Geometry);
		GLSurface inside4Geometry = new GLSurface(2, 2);
		inside4Geometry.setVertex(0, 0, sizeX / 2, -sizeZ / 2, sizeY / 2);
		inside4Geometry.setVertex(1, 0, sizeX / 2, -sizeZ / 2, -sizeY / 2);
		inside4Geometry.setVertex(0, 1, sizeX / 2, sizeZ / 2, sizeY / 2);
		inside4Geometry.setVertex(1, 1, sizeX / 2, sizeZ / 2, -sizeY / 2);
		this.setSide(WWObject.SIDE_INSIDE4, inside4Geometry);

		// Create an inside shape to box the camera. This shape is only shown when the object is clipped by the camera frustrum.
		// It provides a (crude) illusion of the translucency even when the translucency is penetrated. Mapped to CUTOUT1
		GLSurface insideXGeometry = new GLSurface(2, 3);
		float maskWidth = 3.0f * AndroidRenderer.CLOSENESS;
		float maskDistance = 1.5f * AndroidRenderer.CLOSENESS;
		insideXGeometry.setVertex(0, 0, maskWidth, -sizeZ / 2, -maskDistance * 1.2f);
		insideXGeometry.setVertex(1, 0, -maskWidth, -sizeZ / 2, -maskDistance * 1.2f);
		insideXGeometry.setVertex(0, 1, maskWidth, sizeZ / 2, -maskDistance);
		insideXGeometry.setVertex(1, 1, -maskWidth, sizeZ / 2, -maskDistance);
		insideXGeometry.setVertex(0, 2, maskWidth, sizeZ / 2, 0);
		insideXGeometry.setVertex(1, 2, -maskWidth, sizeZ / 2, 0);
		this.setSide(WWObject.SIDE_CUTOUT1, insideXGeometry);

	}

	@Override
	public void updateRendering() {
		buildRendering();  // for now..
	}

	@Override
	public void draw(Shader shader, float[] viewMatrix, float[] sunViewMatrix, long worldTime, int drawType, boolean drawtrans, int lod) {
		if (drawType == DRAW_TYPE_PICKING  || drawType == DRAW_TYPE_SHADOW ) {
			// can't pick or shadow translucencies
			return;
		}
		ClientModel clientModel = AndroidClientModel.getClientModel();
		WWVector cameraPosition = getRenderer().getAdjustedCameraPosition(); //AndroidClientModel.getClientModel().getDampedCameraLocation();
		float cameraPan = clientModel.getDampedCameraPan();
		float cameraTilt = clientModel.getDampedCameraTilt();
		WWVector position = new WWVector();
		object.getAnimatedPosition(position, worldTime);
		modelMatrix = getAnimatedModelMatrix(worldTime);
		Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, modelMatrix, 0);
		for (int side = 0; side < WWObject.NSIDES; side++) {
			if (sides[side] != null) {
				SideAttributes sideAttributes = object.sideAttributes[side];
				if (side == WWObject.SIDE_CUTOUT1) {

					// GLES20.glTranslatef((p.x - position.x), 0.0f, (p.y - position.y));
					float[] cutoutMatrix = new float[16];
					Matrix.translateM(cutoutMatrix, 0, modelMatrix, 0, (cameraPosition.x - position.x), 0.0f, (cameraPosition.y - position.y));
					if (clientModel.getAvatar() != null && clientModel.cameraObject == clientModel.getAvatar()) {
						cameraPan += clientModel.getAvatar().getRotation().getYaw();
					}
					Matrix.rotateM(cutoutMatrix, 0, cameraPan, 0.0f, 1.0f, 0.0f);
					Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, cutoutMatrix, 0);
					Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, cutoutMatrix, 0);

					float trans = sideAttributes.transparency;
					if ((drawtrans && trans > 0.0 && trans < 1.0) || (!drawtrans && trans == 0.0)) {
						if (drawType != DRAW_TYPE_PICKING) {
							WWColor sideColor = sideAttributes.getColor();
							float[] color;
							if (trans == 0.0) {
								GLES20.glDisable(GLES20.GL_BLEND);
								// GLES20.glMaterialfv(GLES20.GL_FRONT_AND_BACK, GLES20.GL_AMBIENT_AND_DIFFUSE, new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f }, 0);
								color = new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f };
							} else {
								GLES20.glEnable(GLES20.GL_BLEND);
								// GLES20.glMaterialfv(GLES20.GL_FRONT_AND_BACK, GLES20.GL_AMBIENT_AND_DIFFUSE, new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f - trans }, 0);
								color = new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f - trans };
							}
							float shininess = sideAttributes.shininess;

							// Note: Adding specular lighting distorts the shading on infuse (but not simulator)
							// GLES20.glMaterialfv(GLES20.GL_FRONT_AND_BACK, GLES20.GL_SPECULAR, new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 0.0f }, 0);
							// GLES20.glMaterialf(GLES20.GL_FRONT_AND_BACK, GLES20.GL_SHININESS, 1.0f);
							String textureUrl = sideAttributes.texture.name;
							int textureId = renderer.getTexture(textureUrl, sideAttributes.texture.pixelated);
							Matrix.setIdentityM(textureMatrix, 0);
							Matrix.scaleM(textureMatrix, 0, 1.0f / sideAttributes.texture.scaleX, 1.0f / sideAttributes.texture.scaleY, 1.0f);
							Matrix.translateM(textureMatrix, 0, sideAttributes.texture.offsetX + 0.5f, sideAttributes.texture.offsetY + 0.5f, 0.0f);
							float textureRotation = sideAttributes.texture.rotation;
							if (textureRotation != 0.0f) {
								Matrix.rotateM(textureMatrix, 0, textureRotation, 0.0f, 0.0f, 1.0f);
							}
							GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
							GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
							int bumpTextureId = renderer.getNormalTexture(textureUrl, sideAttributes.texture.pixelated);
							GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
							GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpTextureId);
							boolean hasAlpha = renderer.textureHasAlpha(textureUrl);
							GLSurface geometry = sides[side];
							geometry.draw(shader, drawType, cutoutMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, shininess, sideAttributes.fullBright, hasAlpha, lod);
						}
					}
					
					// Need to restore the mv and sunMv matrices as they were adjusted using cutoutMatrix above
					Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);
					Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, modelMatrix, 0);
					
				} else {
					float trans = sideAttributes.transparency;
					if ((drawtrans && trans > 0.0 && trans < 1.0) || (!drawtrans && trans == 0.0)) {
						if (drawType == DRAW_TYPE_PICKING) {
							int id = object.getId();
							float red = (((id & 0xF00) >> 8) + 0.5f) / 16.0f;
							float green = (((id & 0x00F0) >> 4) + 0.5f) / 16.0f;
							float blue = ((id & 0x00F) + 0.5f) / 16.0f;
							float[] color = new float[] {red, green, blue, 1.0f};
							GLSurface geometry = sides[side];
							geometry.draw(shader, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, 0.0f, true, false, lod);
						} else {
							WWColor sideColor = sideAttributes.getColor();
							float[] color;
							if (trans == 0.0) {
								GLES20.glDisable(GLES20.GL_BLEND);
								// GLES20.glMaterialfv(GLES20.GL_FRONT_AND_BACK, GLES20.GL_AMBIENT_AND_DIFFUSE, new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f }, 0);
								color = new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f };
							} else {
								GLES20.glEnable(GLES20.GL_BLEND);
								// GLES20.glMaterialfv(GLES20.GL_FRONT_AND_BACK, GLES20.GL_AMBIENT_AND_DIFFUSE, new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f - trans }, 0);
								color = new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 1.0f - trans };
							}
							float shininess = sideAttributes.shininess;

							// Note: Adding specular lighting distorts the shading on infuse (but not simulator)
							// GLES20.glMaterialfv(GLES20.GL_FRONT_AND_BACK, GLES20.GL_SPECULAR, new float[] { sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), 0.0f }, 0);
							// GLES20.glMaterialf(GLES20.GL_FRONT_AND_BACK, GLES20.GL_SHININESS, 1.0f);
							String textureUrl = sideAttributes.texture.name;
							int textureId = renderer.getTexture(textureUrl, sideAttributes.texture.pixelated);
							Matrix.setIdentityM(textureMatrix, 0);
							Matrix.scaleM(textureMatrix, 0, 1.0f / sideAttributes.texture.scaleX, 1.0f / sideAttributes.texture.scaleY, 1.0f);
							Matrix.translateM(textureMatrix, 0, sideAttributes.texture.offsetX + 0.5f, sideAttributes.texture.offsetY + 0.5f, 0.0f);
							float textureRotation = sideAttributes.texture.rotation;
							if (textureRotation != 0.0f) {
								Matrix.rotateM(textureMatrix, 0, textureRotation, 0.0f, 0.0f, 1.0f);
							}
							GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
							GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
							int bumpTextureId = renderer.getNormalTexture(textureUrl, sideAttributes.texture.pixelated);
							GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
							GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpTextureId);
							boolean hasAlpha = renderer.textureHasAlpha(textureUrl);
							GLSurface geometry = sides[side];
							geometry.draw(shader, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, shininess, sideAttributes.fullBright, hasAlpha, lod);
						}
					}
				}
			}
		}
	}

}
