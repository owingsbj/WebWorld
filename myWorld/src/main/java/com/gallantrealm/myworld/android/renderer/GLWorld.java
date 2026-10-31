package com.gallantrealm.myworld.android.renderer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.gallantrealm.myworld.client.renderer.IRenderer;
import com.gallantrealm.myworld.client.renderer.IRendering;
import com.gallantrealm.myworld.model.WWConstant;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWWorld;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GLWorld implements IRendering {

	private static final String TAG = "GLWorld";

	private final AndroidRenderer renderer;
	private final WWWorld world;

	// Reusable scratch array to avoid per-frame GC allocations when drawing single surfaces
	private final GLSurface[] singleSurfaceScratch = new GLSurface[1];

	// Reusable static comparator to avoid per-frame allocations during sorting
	private static final Comparator<GLSurface> SURFACE_Z_COMPARATOR =
			(lhs, rhs) -> Float.compare(rhs.sortVec[2], lhs.sortVec[2]);

	@SuppressWarnings("unused")
	public GLWorld(AndroidRenderer renderer, WWWorld world, long worldTime) {
		this.renderer = renderer;
		this.world = world;
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Record the location of all objects in the world at the given time
	 * 
	 * @param worldTime world time in milliseconds
	 */
	public void snap(long worldTime) {
		WWObject[] objects = world.objects;
		int lastObjectIndex = world.lastObjectIndex;
		for (int i = 0; i <= lastObjectIndex; i++) {
			WWObject object = objects[i];
			if (object != null && object.renderit && object.rendering != null) {
				((GLRendering) object.rendering).snap(worldTime);
			}
		}
	}

	// These vars are related to the group drawing optimization (which works on fixed objects with like texture)
	public boolean drawnOnce;
	public GLSurface[][] drawGroups;
	public WWObject[] drawGroupsObject;

	public void draw(Shader shader, float[] viewMatrix, long worldTime, int drawType) {

		// And all the objects
		WWObject[] objects = world.objects;
		int lastObjectIndex = world.lastObjectIndex;

		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDepthMask(true);

		// draw non-translucent (non-groups)
		int largestGroup = 0;
		for (int i = 0; i <= lastObjectIndex; i++) {
			WWObject object = objects[i];
			if (object != null && object.renderit && object.rendering != null) {
				largestGroup = Math.max(largestGroup, object.group);
				if (!drawnOnce || ((drawType == DRAW_TYPE_PICKING || object.group == 0))) {
					if (!object.shadowless || drawType != DRAW_TYPE_SHADOW) {
						((GLRendering) object.rendering).draw(shader, viewMatrix, worldTime, drawType, false, object.renderLod);
					}
				}
			}
		}

		// draw non-translucent groups
		if (drawType != DRAW_TYPE_PICKING && drawnOnce) {
			if (drawGroups == null) {
				drawGroups = new GLSurface[largestGroup][];
				drawGroupsObject = new WWObject[largestGroup];
				for (int g = 1; g <= largestGroup; g++) {
					WWObject tokenObject = null;
					ArrayList<GLSurface> surfacesList = new ArrayList<GLSurface>();
					for (int i = 0; i <= lastObjectIndex; i++) {
						WWObject object = objects[i];
						if (object != null && object.renderit && object.rendering != null && object.group == g) {
							tokenObject = object;
							GLSurface[] sides = ((GLObject) object.rendering).sides;
							for (GLSurface side : sides) {
								if (side != null) {
									surfacesList.add(side);
								}
							}
						}
					}
					if (tokenObject != null) {
						GLSurface[] surfaces = new GLSurface[surfacesList.size()];
						surfacesList.toArray(surfaces);
						drawGroups[g - 1] = surfaces;
						drawGroupsObject[g - 1] = tokenObject;
					}
				}
			}
			for (int g = 0; g < largestGroup; g++) {
				WWObject tokenObject = drawGroupsObject[g];
				if (tokenObject != null && tokenObject.renderit && tokenObject.rendering != null && tokenObject.sideAttributes[WWConstant.SIDE_ALL].transparency == 0) {
					if (!tokenObject.shadowless || drawType != DRAW_TYPE_SHADOW) {
						((GLObject) tokenObject.rendering).drawSurfaces(shader, drawGroups[g], viewMatrix, worldTime, drawType, false);
					}
				}
			}
		}

		if (drawType != DRAW_TYPE_SHADOW && !(shader instanceof DepthShader)) {

			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glDepthMask(false);

			// draw translucent (non-groups)
			for (int i = 0; i <= lastObjectIndex; i++) {
				WWObject object = objects[i];
				if (object != null && object.renderit && object.rendering != null && (object.group == 0 || !drawnOnce)) {
					((GLRendering) object.rendering).draw(shader, viewMatrix, worldTime, drawType, true, WWObject.RENDER_LOD_FULL);
				}
			}

			// draw translucent groups (sorted for better effect)
			if (drawType != DRAW_TYPE_PICKING && drawnOnce) {
				for (int g = 0; g < drawGroups.length; g++) {
					WWObject tokenObject = drawGroupsObject[g];
					if (tokenObject != null && tokenObject.renderit && tokenObject.rendering != null && tokenObject.sideAttributes[WWConstant.SIDE_ALL].transparency > 0) {
						if (tokenObject.rendering instanceof GLObject) {
							sortSurfaces(drawGroups[g], viewMatrix);
							GLObject glObject = (GLObject) tokenObject.rendering;
							for (int s = 0; s < drawGroups[g].length; s++) {
								singleSurfaceScratch[0] = drawGroups[g][s];
								glObject.drawSurfaces(shader, singleSurfaceScratch, viewMatrix, worldTime, drawType, true);
							}
						}
					}
				}
			}

			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glDepthMask(true);

		}
		drawnOnce = true;
	}

	private void sortSurfaces(GLSurface[] surfaces, float[] viewMatrix) {
		for (GLSurface surface : surfaces) {
			Matrix.multiplyMV(surface.sortVec, 0, viewMatrix, 0, surface.firstVertex, 0);
		}
		try {
			Arrays.sort(surfaces, SURFACE_Z_COMPARATOR);
		} catch (Exception e) {
			Log.e(TAG, "Error sorting translucent surfaces", e);
		}
	}
	
	public void updateRendering() {
		// not implemented
	}
	
}
