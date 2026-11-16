package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.client.renderer.IRenderer;

/**
 * Represents a torus (tire) shape. Variants allow for pipes and other ring shapes.
 */
public class WWTorus extends WWSimpleShape {
	static final long serialVersionUID = 1L;

	public WWTorus() {
		super();
	}

	public WWTorus(float sizeX, float sizeY, float sizeZ) {
		super(sizeX, sizeY, sizeZ);
	}

	public void createRendering(IRenderer renderer, long worldTime) {
		rendering = renderer.createTorusRendering(this, worldTime);
	}

	/**
	 * Returns an array of points describing the edges of the torus
	 */
	@Override
	protected WWVector[] getEdgePoints() {
		if (edgePoints == null) {
			// TODO this is copied from cylinder. tune for torus
			float sx2 = size.x / 2.0f;
			float sy2 = size.y / 2.0f;
			float sz2 = size.z / 2.0f;
			edgePoints = new WWVector[] {
					// - six center side points
					new WWVector(sx2, 0, 0), new WWVector(-sx2, 0, 0), new WWVector(0, sy2, 0), new WWVector(0, -sy2, 0), new WWVector(0, 0, sz2), new WWVector(0, 0, -sz2),
					// - eight corners
					new WWVector(0.7f * sx2, 0.7f * sy2, sz2), new WWVector(-0.7f * sx2, 0.7f * sy2, sz2), new WWVector(0.7f * sx2, -0.7f * sy2, sz2), new WWVector(-0.7f * sx2, -0.7f * sy2, sz2), new WWVector(0.7f * sx2, 0.7f * sy2, -sz2), new WWVector(-0.7f * sx2, 0.7f * sy2, -sz2), new WWVector(0.7f * sx2, -0.7f * sy2, -sz2), new WWVector(-0.7f * sx2, -0.7f * sy2, -sz2),
					// - twelve half edge points
					new WWVector(0.7f * sx2, 0.7f * sy2, 0), new WWVector(-0.7f * sx2, 0.7f * sy2, 0), new WWVector(0.7f * sx2, -0.7f * sy2, 0), new WWVector(-0.7f * sx2, -0.7f * sy2, 0), new WWVector(sx2, 0, sz2), new WWVector(-sx2, 0, sz2), new WWVector(sx2, 0, -sz2), new WWVector(-sx2, 0, -sz2), new WWVector(0, sy2, sz2), new WWVector(0, -sy2, sz2), new WWVector(0, sy2, -sz2), new WWVector(0, -sy2, -sz2) };
		}
		return edgePoints;
	}

	/**
	 * Returns a vector giving the amount of penetration of a point within the object, or null if the point does not
	 * penetrate.
	 */
	@Override
	public void getPenetration(WWVector point, WWVector position, WWQuaternion rotation, long worldTime, WWVector tempPoint, WWVector penetration) {

		// Anti-transform
		tempPoint.x = point.x;
		tempPoint.y = point.y;
		tempPoint.z = point.z;
		antiTransform(tempPoint, position, rotation, worldTime);

		// Scale the point down to unit scale, just to make it easier
		tempPoint.scale(1.0f / size.x, 1.0f / size.y, 1.0f / size.z);

		tempPoint.scale(1.0f, 1.0f, 0.1875f / 0.5f); // adjust for z ring thickness difference

		// Doesnt overlap if x,y is in cutout area
		if (cutoutStart != 0 || cutoutEnd != 1) {
			float theta = FastMath.atan2(tempPoint.x, tempPoint.y);
			float cutPoint = (1.0f - TODEGREES * theta / 360.0f) % 1.0f;
			if (cutPoint < cutoutStart || cutPoint > cutoutEnd) {
				penetration.zero();
				return;
			}
		}

		// Determine the radius distance. This is the distance to the center of the torus
		float radiusDistance = (float)Math.sqrt(tempPoint.x * tempPoint.x + tempPoint.y * tempPoint.y);

		// Determine the nearest point on the inner 'ring' of the torus.
		WWVector ringPoint;
		if (radiusDistance > 0.00001f) {
			ringPoint = new WWVector(tempPoint.x / radiusDistance * 0.375f, tempPoint.y / radiusDistance * 0.375f, 0.0f);
		} else {
			ringPoint = new WWVector(0.375f, 0.0f, 0.0f);
		}

		// Determine the penetration vector by determining the difference between a vector that is 0.25 length and the vector
		// that connects the ring core point to the transformed point.
		WWVector ringCoreDistance = new WWVector(tempPoint.x - ringPoint.x, tempPoint.y - ringPoint.y, tempPoint.z - ringPoint.z);
		WWVector ringCoreThicknessVector = ringCoreDistance.clone();
		if (ringCoreDistance.length() > 0.00001f) {
			ringCoreThicknessVector.normalize();
			ringCoreThicknessVector.scale(0.1875f); // ring thickness (unadjusted in z dimension)
		} else {
			ringCoreThicknessVector.set(0, 0, 0.1875f);
		}
		if (ringCoreDistance.length() >= ringCoreThicknessVector.length()) {
			penetration.zero();
			return;
		}
		ringCoreDistance.copyInto(penetration);
		penetration.subtract(ringCoreThicknessVector);

		penetration.scale(Math.min(Math.min(size.x, size.y), size.z)); // alternate scaling that doesn't distort penetration 
		rotate(penetration, rotation);
	}
}
