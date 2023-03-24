package com.gallantrealm.myworld.android.renderer;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.client.renderer.IRenderer;
import com.gallantrealm.myworld.model.SideAttributes;
import com.gallantrealm.myworld.model.WWMatrix;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * This is the superclass of all OpenGL primitives used in MyWorld.
 */
public abstract class GLObject extends GLRendering {

	static int lastTextureId;
	static int lastBumpTextureId;

	protected final WWObject object;
	protected final AndroidRenderer renderer;

	protected GLSurface[] sides = new GLSurface[WWObject.NSIDES];

	protected GLObject(AndroidRenderer renderer, WWObject object, long worldTime) {
		this.renderer = renderer;
		this.object = object;
	}

	public final WWObject getObject() {
		return object;
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

	public void setSide(int side, GLSurface geometry) {
		sides[side] = geometry;
	}

	public GLSurface getSide(int side) {
		return sides[side];
	}

	/**
	 * Apply all of the distortion effects to the geometry. These effects do not require any knowledge of the object itself, just its coordinates, so they can be applied after the initial geometry has been determined.
	 */
	protected void adjustGeometry(GLSurface surface, float sizeX, float sizeY, float sizeZ, float taperX, float taperY, float shearX, float shearY, float twist) {
		int vertexCount = surface.getVertexCount();
		Point3f coordinate = new Point3f();

		for (int i = 0; i < vertexCount; i++) {
			surface.getVertex(i, coordinate);

			// Note: The order of these actions is important. They are done so editing the object is easier.
			// Note: coordinate.z is the y coordinate, and coordinate.y is the z coordinate (java3d and myworld are
			// different)

			// Taper
			float zoffset = coordinate.y + 0.5f;
			coordinate.x = coordinate.x * (1.0f - taperX * zoffset);
			coordinate.z = coordinate.z * (1.0f - taperY * zoffset);

			// Shear
			coordinate.x = coordinate.x + shearX * zoffset;
			coordinate.z = coordinate.z + shearY * zoffset;

			// Resize
			coordinate.x = coordinate.x * sizeX;
			coordinate.y = coordinate.y * sizeZ;
			coordinate.z = coordinate.z * sizeY;

			// Twist
			float r = (float) Math.sqrt(coordinate.x * coordinate.x + coordinate.z * coordinate.z);
			float theta = FastMath.atan2(coordinate.x, coordinate.z);
			float thetaDelta = (coordinate.y / sizeZ + 0.5f) * twist * 2.0f * (float) Math.PI;
			coordinate.x = r * (float) Math.sin(theta + thetaDelta);
			coordinate.z = r * (float) Math.cos(theta + thetaDelta);

			// Model matrix -- add in only when completely fixed
			if (object.group != 0) {
				// Note: only groups because other places need to be changed
				float[] lhsMat = getModelMatrix(0);
				float[] rhsVec = new float[] { coordinate.x, coordinate.y, coordinate.z, 1 };
				float[] resultVec = new float[4];
				Matrix.multiplyMV(resultVec, 0, lhsMat, 0, rhsVec, 0);
				coordinate.x = resultVec[0];
				coordinate.y = resultVec[1];
				coordinate.z = resultVec[2];
			}

			surface.setVertex(i, coordinate);
		}
	}

	protected void adjustTextureCoords(GLSurface surface, int side) {
		if (object.fixed) {
			SideAttributes sideAttributes = object.sideAttributes[side];
			String textureUrl = sideAttributes.texture.name;
			if (textureUrl != null) {
				float[] textureMatrix = new float[16];
				Matrix.setIdentityM(textureMatrix, 0);
				Matrix.scaleM(textureMatrix, 0, 1.0f / sideAttributes.texture.scaleX, 1.0f / sideAttributes.texture.scaleY, 1.0f);
				Matrix.translateM(textureMatrix, 0, sideAttributes.texture.offsetX + 0.5f, sideAttributes.texture.offsetY + 0.5f, 0);
				Matrix.rotateM(textureMatrix, 0, sideAttributes.texture.rotation, 0, 0, 1);
				surface.adjustTextureCoords(textureMatrix);
			}
		}
	}

	private static float[] tempMatrix = new float[16];

	/**
	 * Adjust position and rotations for GL according to parent(s)
	 */
	private void parentalAdjust(float[] modelMatrix, int parentId, long worldTime) {
		WWObject parent = object.world.objects[parentId];
		parentId = parent.parentId;
		if (parentId != 0) {
			parentalAdjust(modelMatrix, parentId, worldTime);
		}
		WWVector position = new WWVector();
		parent.getAnimatedPosition(position, worldTime);
		// GLES20.glTranslatef(position.x, position.z, position.y);
		Matrix.translateM(modelMatrix, 0, position.x, position.z, position.y);
		WWQuaternion rotation = new WWQuaternion();
		parent.getAnimatedRotation(rotation, worldTime);
		rotation.toMatrix(tempMatrix);
		Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, tempMatrix, 0);
	}

	private final WWVector position = new WWVector();
	private final WWQuaternion rotation = new WWQuaternion();

	float[] modelMatrix;
	static float[] textureMatrix = new float[16];

	public void snap(long worldTime) {
		if (modelMatrix == null || !object.fixed) {
			// To snap the object we'll create the model matrix
			object.getAnimatedPosition(position, worldTime);
			object.getAnimatedRotation(rotation, worldTime);
			modelMatrix = new float[16];
			Matrix.setIdentityM(modelMatrix, 0);
			Matrix.translateM(modelMatrix, 0, position.x, position.z, position.y);
			Matrix.translateM(modelMatrix, 0, object.rotationPoint.x, object.rotationPoint.z, object.rotationPoint.y);
			rotation.toMatrix(tempMatrix);
			Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, tempMatrix, 0);
			Matrix.translateM(modelMatrix, 0, -object.rotationPoint.x, -object.rotationPoint.z, -object.rotationPoint.y);
			object.preAnimateModelMatrix(object, new WWMatrix(modelMatrix), worldTime);
			object.lastRenderingTime = worldTime;
		}
	}

	// Used for the combined model matrix when it is a child object
	public float[] compositeModelMatrix = new float[16];
	long compositeModelMatrixTime = -1;

	/**
	 * Returns the matrix for this object with no animation applied
	 */
	public final float[] getModelMatrix(long worldTime) {
		if (modelMatrix == null) {
			snap(worldTime);
		}
		if (object.parentId == 0) {
			return modelMatrix;
		} else {
			if (worldTime != compositeModelMatrixTime) {
				WWObject parent = object.world.objects[object.parentId];
				float[] parentModelMatrix = ((GLObject) parent.getRendering()).getModelMatrix(worldTime);
				Matrix.multiplyMM(compositeModelMatrix, 0, parentModelMatrix, 0, modelMatrix, 0);
				compositeModelMatrixTime = worldTime;
			}
			return compositeModelMatrix;
		}
	}

	/**
	 * Returns the model matrix with animations applied.
	 */
	public final float[] getAnimatedModelMatrix(long worldTime) {
		float[] modelMatrix = getModelMatrix(worldTime);
		float[] animatedMatrix = modelMatrix.clone();
		object.postAnimateModelMatrix(object, new WWMatrix(animatedMatrix), worldTime);
		return animatedMatrix;
	}

	float[] mvMatrix = new float[16];
	float[] sunMvMatrix = new float[16];
	long sunMvMatrixTime = -1;

	public final float[] getSunMvMatrix(long worldTime, float[] modelMatrix, float[] sunViewMatrix) {
		if (worldTime != sunMvMatrixTime) {
			Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, modelMatrix, 0);
			sunMvMatrixTime = worldTime;
		}
		return sunMvMatrix;
	}

	@Override
	public void draw(Shader shader, float[] viewMatrix, float[] sunViewMatrix, long worldTime, int drawType, boolean drawtrans, boolean mini) {

// Note: This optimization is disabled so that textures with transparent pixels are taken into account
// when determining shadows.  This lets plants cast shadows.
//
//		if (drawType == DRAW_TYPE_SHADOW || shader instanceof DepthShader) {
//
//			// simplified drawing with no lighting or textures. It can always be monolithic
//			SideAttributes sideAttributes = object.sideAttributes[WWObject.SIDE_ALL];
//			if (sideAttributes.getTransparency() == 0) {
//				float[] modelMatrix = getModelMatrix(worldTime);
//				Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);
//				Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, modelMatrix, 0);
//				GLSurface.drawMonolith(shader, sides, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, null, 0, true, true);
//			}
//
//		} else
		if (object.monolithic || drawType == DRAW_TYPE_PICKING) {

			// Monolithic drawing. Draw all surfaces together
			SideAttributes sideAttributes = object.sideAttributes[WWObject.SIDE_ALL];
			float trans = sideAttributes.transparency;
			if ((drawtrans && trans > 0.0 && trans < 1.0) || (!drawtrans && trans == 0.0)) {
				float[] modelMatrix = getAnimatedModelMatrix(worldTime);
				Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);
				Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, modelMatrix, 0);
				Matrix.setIdentityM(textureMatrix, 0);

				// send to the shader
				if (drawType == DRAW_TYPE_PICKING) {
					float[] color = null;
					float shininess = 0.0f;
					int id = object.getId();
					float red = (((id & 0xF00) >> 8) + 0.5f) / 16.0f;
					float green = (((id & 0x00F0) >> 4) + 0.5f) / 16.0f;
					float blue = ((id & 0x00F) + 0.5f) / 16.0f;
					color = new float[] { red, green, blue, 1.0f };
					int textureId = renderer.getTexture("white", true);
					if (textureId != lastTextureId) {
						GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
						GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
						lastTextureId = textureId;
					}
					int bumpTextureId = renderer.getNormalTexture("white", true);
					if (bumpTextureId != lastBumpTextureId) {
						GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
						GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpTextureId);
						lastBumpTextureId = bumpTextureId;
					}
					GLSurface.drawMonolith(shader, sides, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, shininess, true, false);
				} else {
					float[] color = null;
					float shininess = 0.0f;
					float red = sideAttributes.color.getRed();
					float green = sideAttributes.color.getGreen();
					float blue = sideAttributes.color.getBlue();
					shininess = sideAttributes.shininess;
					if (drawType == DRAW_TYPE_LEFT_EYE) { // red side
						red = (red * 3 + green + blue) / 5.0f;
						green = 0;
						blue = 0;
					} else if (drawType == DRAW_TYPE_RIGHT_EYE) { // cyan side
						red = 0;
						green = (green * 3 + red) / 4.0f;
						blue = (blue * 3 + red) / 4.0f;
					}
					color = new float[] { red, green, blue, 1.0f - trans };
					if (!object.fixed) { // for fixed the texture matrix is baked into the texture coords
						Matrix.scaleM(textureMatrix, 0, 1.0f / sideAttributes.texture.scaleX, 1.0f / sideAttributes.texture.scaleY, 1.0f);
						Matrix.translateM(textureMatrix, 0, sideAttributes.texture.offsetX + 0.5f, sideAttributes.texture.offsetY + 0.5f, 0.0f);
						float textureRotation = sideAttributes.texture.rotation;
						if (textureRotation != 0.0f) {
							Matrix.rotateM(textureMatrix, 0, textureRotation, 0.0f, 0.0f, 1.0f);
						}
					}
					String textureUrl = sideAttributes.texture.name;
					int textureId = renderer.getTexture(textureUrl, sideAttributes.texture.pixelated);
					if (textureId != lastTextureId) {
						GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
						GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
						lastTextureId = textureId;
					}
					int bumpTextureId = renderer.getNormalTexture(textureUrl, sideAttributes.texture.pixelated);
					if (bumpTextureId != lastBumpTextureId) {
						GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
						GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpTextureId);
						lastBumpTextureId = bumpTextureId;
					}
					boolean hasAlpha = renderer.textureHasAlpha(textureUrl);
					GLSurface.drawMonolith(shader, sides, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, shininess, sideAttributes.fullBright, hasAlpha);
				}
			}

		} else {

			// Non-monolithic drawing. Draw each surface separately
			boolean sideDrawn = false;

			float[] modelMatrix = getAnimatedModelMatrix(worldTime);
			Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);
			Matrix.multiplyMM(sunMvMatrix, 0, sunViewMatrix, 0, modelMatrix, 0);

			float[] color = null;
			float shininess = 0.0f;
			for (int side = 0; side < WWObject.NSIDES; side++) {
				if (sides[side] != null) {
					SideAttributes sideAttributes = object.sideAttributes[side];
					float trans = sideAttributes.transparency;
					if ((drawtrans && trans > 0.0 && trans < 1.0) || (!drawtrans && trans == 0.0)) {
						if (!sideDrawn) {
							if (drawType == DRAW_TYPE_PICKING) {
								// if (!object.isPickable()) {
								// return;
								// }
								int id = object.getId();
								float red = (((id & 0xF00) >> 8) + 0.5f) / 16.0f;
								float green = (((id & 0x00F0) >> 4) + 0.5f) / 16.0f;
								float blue = ((id & 0x00F) + 0.5f) / 16.0f;
								color = new float[] { red, green, blue, 1.0f };
								shininess = 0.0f;
								color = new float[] { 0, 0, 1, 1 };
							}
							sideDrawn = true;
						}
						if (drawType == DRAW_TYPE_PICKING) { // || drawType == DRAW_TYPE_SHADOW) {
							GLSurface geometry = sides[side];
							geometry.draw(shader, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, 0.0f, true, false);
						} else {

							float red = sideAttributes.color.getRed();
							float green = sideAttributes.color.getGreen();
							float blue = sideAttributes.color.getBlue();
							shininess = sideAttributes.shininess;
							if (drawType == DRAW_TYPE_LEFT_EYE) { // red side
								red = (red * 3 + green + blue) / 5.0f;
								green = 0;
								blue = 0;
							} else if (drawType == DRAW_TYPE_RIGHT_EYE) { // cyan side
								red = 0;
								green = (green * 3 + red) / 4.0f;
								blue = (blue * 3 + red) / 4.0f;
							}
							color = new float[] { red, green, blue, 1.0f - trans };

							Matrix.setIdentityM(textureMatrix, 0);
							if (!object.fixed) { // for fixed the texture matrix is baked into the texture coords
								Matrix.scaleM(textureMatrix, 0, 1.0f / object.sideAttributes[side].texture.scaleX, 1.0f / object.sideAttributes[side].texture.scaleY, 1.0f);
								Matrix.translateM(textureMatrix, 0, sideAttributes.texture.offsetX + 0.5f, sideAttributes.texture.offsetY + 0.5f, 0.0f);
								float textureRotation = sideAttributes.texture.rotation;
								if (textureRotation != 0.0f) {
									Matrix.rotateM(textureMatrix, 0, textureRotation, 0.0f, 0.0f, 1.0f);
								}
							}
							String textureUrl = sideAttributes.texture.name;
							int textureId = renderer.getTexture(textureUrl, sideAttributes.texture.pixelated);
							if (textureId != lastTextureId) {
								GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
								GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
								lastTextureId = textureId;
							}
							int bumpTextureId = renderer.getNormalTexture(textureUrl, sideAttributes.texture.pixelated);
							if (bumpTextureId != lastBumpTextureId) {
								GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
								GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpTextureId);
								lastBumpTextureId = bumpTextureId;
							}
							boolean hasAlpha = renderer.textureHasAlpha(textureUrl);
							GLSurface geometry = sides[side];
							if (mini) {
								geometry.drawMini(shader, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, shininess, sideAttributes.fullBright, hasAlpha);
							} else {
								geometry.draw(shader, drawType, modelMatrix, mvMatrix, sunMvMatrix, textureMatrix, color, shininess, sideAttributes.fullBright, hasAlpha);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Draw a group of surfaces. The surfaces belong to this object and all objects in the same group, and were collected earlier using collectSurfaces. Since all surfaces have similar texture, the texture attributes are applied once for
	 * all and a monolithic draw is done.
	 */
	public void drawSurfaces(Shader shader, GLSurface[] groupSurfaces, float[] viewMatrix, float[] sunViewMatrix, long worldTime, int drawType, boolean drawtrans) {

// Note: This optimization is disabled so that textures with transparent pixels are taken into account
// when determining shadows.  This lets plants cast shadows.
//
//		if (drawType == DRAW_TYPE_SHADOW || shader instanceof DepthShader) {
//
//			// simplified drawing with no lighting or textures. It can always be monolithic
//			SideAttributes sideAttributes = object.sideAttributes[WWObject.SIDE_ALL];
//			if (sideAttributes.getTransparency() == 0) {
//				GLSurface.drawMonolith(shader, groupSurfaces, drawType, modelMatrix, viewMatrix, sunViewMatrix, textureMatrix, null, 0, sideAttributes.isFullBright(), true);
//			}
//
//		} else {

			// choose a reference side, the first non-transparent side
			SideAttributes sideAttributes = null;
			for (int side = WWObject.SIDE_ALL; side <= WWObject.SIDE_CUTOUT2; side++) {
				if (sideAttributes == null && object.sideAttributes[side].transparency < 1) {
					sideAttributes = object.sideAttributes[side];
				}
			}
			if (sideAttributes == null) { // all transparent
				return;
			}

			float trans = sideAttributes.transparency;
			float[] color = null;
			float shininess = 0.0f;
			if ((drawtrans && trans > 0.0) || (!drawtrans && trans == 0.0)) {
				// float[] modelMatrix = getModelMatrix(worldTime);
				Matrix.setIdentityM(modelMatrix, 0);
				boolean hasAlpha = false;
			//	if (drawType != DRAW_TYPE_SHADOW) {
					Matrix.setIdentityM(textureMatrix, 0);
					if (false) { // texture matrix is always baked into texture coords for groups
						Matrix.scaleM(textureMatrix, 0, 1.0f / sideAttributes.texture.scaleX, 1.0f / sideAttributes.texture.scaleY, 1.0f);
						Matrix.translateM(textureMatrix, 0, sideAttributes.texture.offsetX + 0.5f, sideAttributes.texture.offsetY + 0.5f, 0.0f);
						float textureRotation = sideAttributes.texture.rotation;
						if (textureRotation != 0.0f) {
							Matrix.rotateM(textureMatrix, 0, textureRotation, 0.0f, 0.0f, 1.0f);
						}
					}
					float red = sideAttributes.color.getRed();
					float green = sideAttributes.color.getGreen();
					float blue = sideAttributes.color.getBlue();
					shininess = sideAttributes.shininess;
					if (drawType == DRAW_TYPE_LEFT_EYE) { // red side
						red = (red * 3 + green + blue) / 5.0f;
						green = 0;
						blue = 0;
					} else if (drawType == DRAW_TYPE_RIGHT_EYE) { // cyan side
						red = 0;
						green = (green * 3 + red) / 4.0f;
						blue = (blue * 3 + red) / 4.0f;
					}
					color = new float[] { red, green, blue, 1.0f - trans };
					String textureUrl = sideAttributes.texture.name;
					int textureId = renderer.getTexture(textureUrl, sideAttributes.texture.pixelated);
					if (textureId != lastTextureId) {
						GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
						GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
						lastTextureId = textureId;
					}
					int bumpTextureId = renderer.getNormalTexture(textureUrl, sideAttributes.texture.pixelated);
					if (bumpTextureId != lastBumpTextureId) {
						GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
						GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpTextureId);
						lastBumpTextureId = bumpTextureId;
					}
					hasAlpha = renderer.textureHasAlpha(textureUrl);
			//	}

				// Note: model is identity so mvMatrix == viewMatrix and sunMvMatrix == sunViewMatrix below
				GLSurface.drawMonolith(shader, groupSurfaces, drawType, modelMatrix, viewMatrix, sunViewMatrix, textureMatrix, color, shininess, sideAttributes.fullBright, hasAlpha);
			}
//		}

	}

}
