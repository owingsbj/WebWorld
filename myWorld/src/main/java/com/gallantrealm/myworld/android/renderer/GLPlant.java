package com.gallantrealm.myworld.android.renderer;

import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWPlant;

/**
 * Creates a primitive with four vertical crossed rectangles. These rectangles are considered the four "sides". Given a
 * partially transparent texture, this can represent plants or other symmetrical objects. Either the same texture can be
 * used for all sides, or different textures can be used to give the plant additional complexity.
 */
public class GLPlant extends GLObject {

	public GLPlant(AndroidRenderer renderer, WWPlant plant, long worldTime) {
		super(renderer, plant, worldTime);
		buildRendering();
	}

	public void buildRendering() {
		WWPlant plant = (WWPlant)this.object;
		if (plant.getType().equals(WWPlant.TYPE_TREE)) {

			// There are eight plant sides (4 two-sided planes) that criss-cross.
			for (int i = 0; i < 4; i++) {
				float r = (float)Math.toRadians(i * 45);
				buildPlane(i, 0, 0, r);
			}

		} else if (plant.getType().equals(WWPlant.TYPE_GRASS)) {

			// Max the sides, spread them out randomly to create a cluster
			for (int i = 0; i < 6; i++) {
				buildPlane(i, (float)Math.random(), (float)Math.random(), (float) (Math.random() * 45));
			}
		}

	}

	private void buildPlane(int i, float dx, float dy, float r) {
		WWPlant plant = (WWPlant)this.object;
		float x1 = (float) (-0.5 * (Math.sin(r) - dx)* plant.size.x);
		float y1 = (float) (-0.5 * (Math.cos(r) - dy) * plant.size.y);
		float z1 = (float) (-0.5 * plant.size.z);
		float x2 = (float) (0.5 * (Math.sin(r) + dx) * plant.size.x);
		float y2 = (float) (0.5 * (Math.cos(r)  + dy) * plant.size.y);
		float z2 = (float) (0.5 * plant.size.z);

		GLSurface sideGeometry = new GLSurface(2, 2);
		sideGeometry.setVertex(0, 0, x1, z1, y1);
		sideGeometry.setVertex(0, 1, x1, z2, y1);
		sideGeometry.setVertex(1, 1, x2, z2, y2);
		sideGeometry.setVertex(1, 0, x2, z1, y2);
		adjustGeometry(sideGeometry, 1, 1, 1, 0, 0, 0, 0, 0);
		sideGeometry.generateNormals();
		adjustTextureCoords(sideGeometry, WWObject.SIDE_SIDE1 + i);
		setSide(WWObject.SIDE_SIDE1 + i, sideGeometry);

		sideGeometry = new GLSurface(2, 2);
		sideGeometry.setVertex(0, 0, x2, z1, y2);
		sideGeometry.setVertex(0, 1, x2, z2, y2);
		sideGeometry.setVertex(1, 1, x1, z2, y1);
		sideGeometry.setVertex(1, 0, x1, z1, y1);
		adjustGeometry(sideGeometry, 1, 1, 1, 0, 0, 0, 0, 0);
		sideGeometry.generateNormals();
		adjustTextureCoords(sideGeometry, WWObject.SIDE_INSIDE1 + i);
		setSide(WWObject.SIDE_INSIDE1 + i, sideGeometry);
	}

	public void updateRendering() {
		buildRendering();  // for now..
	}

}
