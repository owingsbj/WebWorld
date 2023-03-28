package com.gallantrealm.myworld.android.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Half;

import com.gallantrealm.myworld.model.WWObject;

/**
 * Encapsulates the creation of triangles for a surface and the normals and texture coordinates for it.
 */
public final class GLSurface {

	public static final int MAX_VERTICES = 1000000;    // TODO -- consider making this an advanced settings
	public static final int MAX_POINT_VERTICES = 1000;
	private static boolean buffersAllocated;
	private static ShortBuffer vertices;
	private static FloatBuffer pointVertices;
	private static ByteBuffer normals;
	private static ByteBuffer tangents;
	private static ByteBuffer bitangents;
	private static ShortBuffer textureCoords;
	private static ShortBuffer extras;
	private static IntBuffer indices;
	private static int nextFreeVertex = 0;
	private static int nextFreeIndex = 0;

	private static int verticesBufferId;
	private static int pointVerticesBufferId;
	private static int extrasBufferId;
	private static int normalsBufferId;
	private static int tangentsBufferId;
	private static int bitangentsBufferId;
	private static int textureCoordsBufferId;
	private static int indicesBufferId;

	public float[] firstVertex = new float[4];
	public float[] sortVec = new float[4];

	public boolean equals(Object o) {
		return this.sortVec[2] == ((GLSurface)o).sortVec[2];
	}

	public static float halfToFloat(short h) {
		return (new Half(h)).floatValue();
	}

	public static short floatToHalf(float f) {
		return (short)(Half.toHalf(f));
	}

	public static void initializeVertexBuffers() {
		System.out.println(">GLSurface.initializeVertexBuffers");

		if (!buffersAllocated) {
			GLES20.glDeleteBuffers(6, new int[] { verticesBufferId, normalsBufferId, tangentsBufferId, bitangentsBufferId, textureCoordsBufferId, indicesBufferId }, 0);

			ByteBuffer bb = ByteBuffer.allocateDirect(MAX_VERTICES * 2 * 3);
			bb.order(ByteOrder.nativeOrder());
			vertices = bb.asShortBuffer();

			bb = ByteBuffer.allocateDirect(MAX_POINT_VERTICES * 4 * 3);
			bb.order(ByteOrder.nativeOrder());
			pointVertices = bb.asFloatBuffer();

			bb = ByteBuffer.allocateDirect(MAX_VERTICES * 1 * 3);
			bb.order(ByteOrder.nativeOrder());
			normals = bb;

			bb = ByteBuffer.allocateDirect(MAX_VERTICES * 1 * 3);
			bb.order(ByteOrder.nativeOrder());
			tangents = bb;

			bb = ByteBuffer.allocateDirect(MAX_VERTICES * 1 * 3);
			bb.order(ByteOrder.nativeOrder());
			bitangents = bb;

			bb = ByteBuffer.allocateDirect(MAX_VERTICES * 2 * 2);
			bb.order(ByteOrder.nativeOrder());
			textureCoords = bb.asShortBuffer();

			bb = ByteBuffer.allocateDirect(MAX_POINT_VERTICES * 2 * 2);
			bb.order(ByteOrder.nativeOrder());
			extras = bb.asShortBuffer();

			bb = ByteBuffer.allocateDirect(MAX_VERTICES * 4 * 6 * 4);
			bb.order(ByteOrder.nativeOrder());
			indices = bb.asIntBuffer();

			int[] bufferIds = new int[8];
			GLES20.glGenBuffers(8, bufferIds, 0);
			verticesBufferId = bufferIds[0];
			normalsBufferId = bufferIds[1];
			tangentsBufferId = bufferIds[2];
			bitangentsBufferId = bufferIds[3];
			textureCoordsBufferId = bufferIds[4];
			indicesBufferId = bufferIds[5];
			pointVerticesBufferId = bufferIds[6];
			extrasBufferId = bufferIds[7];
			buffersAllocated = true;
		}

		vertices.clear();
		normals.clear();
		tangents.clear();
		bitangents.clear();
		textureCoords.clear();
		indices.clear();
		nextFreeVertex = 0;
		nextFreeIndex = 0;

		bindBuffers();

		System.out.println("<GLSurface.initializeVertexBuffers");
	}

	int width; // number of vertices wide
	int height; // number of vertices high
	int baseVertex;
	int nvertices;
	int baseIndex;
	int nindices;
	int baseIndexMini;
	int nindicesMini;
	int baseIndexMicro;
	int nindicesMicro;
	int baseIndexNano;
	int nindicesNano;
	boolean needsBufferUpdating;

	public GLSurface(int width, int height) {
		if (!buffersAllocated) {
			initializeVertexBuffers();;
		}
		this.width = width;
		this.height = height;
		this.nvertices = width * height;
		if (nextFreeVertex + nvertices >= MAX_VERTICES) {
			System.err.println("No more free vertices!!!");
			baseVertex = -1;
			return;
		}
		baseVertex = nextFreeVertex;
		nextFreeVertex += nvertices;

		generateTextureCoords();

		generateIndices();
	}

	public int getVertexCount() {
		return nvertices;
	}

	public void getVertex(int x, int y, Point3f point) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		point.x = halfToFloat(vertices.get(vertex * 3));
		point.y = halfToFloat(vertices.get(vertex * 3 + 1));
		point.z = halfToFloat(vertices.get(vertex * 3 + 2));
	}

	public float getVertexX(int x, int y) {
		if (baseVertex < 0) { // overflow
			return 0;
		}
		int vertex = baseVertex + (y * width + x);
		return halfToFloat(vertices.get(vertex * 3));
	}

	public float getVertexY(int x, int y) {
		if (baseVertex < 0) { // overflow
			return 0;
		}
		int vertex = baseVertex + (y * width + x);
		return halfToFloat(vertices.get(vertex * 3 + 1));
	}

	public float getVertexZ(int x, int y) {
		if (baseVertex < 0) { // overflow
			return 0;
		}
		int vertex = baseVertex + (y * width + x);
		return halfToFloat(vertices.get(vertex * 3 + 2));
	}

	public void setVertex(int x, int y, float px, float py, float pz) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		vertices.put(vertex * 3, floatToHalf(px));
		vertices.put(vertex * 3 + 1, floatToHalf(py));
		vertices.put(vertex * 3 + 2, floatToHalf(pz));
		if (vertex == baseVertex) {
			firstVertex[0] = px;
			firstVertex[1] = py;
			firstVertex[2] = pz;
		}
		needsBufferUpdating = true;
	}

	public void getVertex(int vertex, Point3f point) {
		if (baseVertex < 0) { // overflow
			return;
		}
		vertex += baseVertex;
		point.x = halfToFloat(vertices.get(vertex * 3));
		point.y = halfToFloat(vertices.get(vertex * 3 + 1));
		point.z = halfToFloat(vertices.get(vertex * 3 + 2));
	}

	public void setVertex(int vertex, Point3f point) {
		if (baseVertex < 0) { // overflow
			return;
		}
		vertex += baseVertex;
		vertices.put(vertex * 3, floatToHalf(point.x));
		vertices.put(vertex * 3 + 1, floatToHalf(point.y));
		vertices.put(vertex * 3 + 2, floatToHalf(point.z));
		if (vertex == baseVertex) {
			firstVertex[0] = point.x;
			firstVertex[1] = point.y;
			firstVertex[2] = point.z;
		}
		needsBufferUpdating = true;
	}

	public void getNormal(int x, int y, Point3f normal) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		normal.x = normals.get(vertex * 3) / 127.0f;
		normal.y = normals.get(vertex * 3 + 1) / 127.0f;
		normal.z = normals.get(vertex * 3 + 2) / 127.0f;
	}

	public void setNormal(int x, int y, Point3f normal) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		normals.put(vertex * 3, (byte) (normal.x * 127));
		normals.put(vertex * 3 + 1, (byte) (normal.y * 127));
		normals.put(vertex * 3 + 2, (byte) (normal.z * 127));
		needsBufferUpdating = true;
	}

	public void setTangent(int x, int y, Point3f tangent) {  // the tangent to the normal
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		tangents.put(vertex * 3, (byte) (tangent.x * 127));
		tangents.put(vertex * 3 + 1, (byte) (tangent.y * 127));
		tangents.put(vertex * 3 + 2, (byte) (tangent.z * 127));
		needsBufferUpdating = true;
	}

	public void setBitangent(int x, int y, Point3f bitangent) {  // another tangent to the normal, perpendicular to tangent
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		bitangents.put(vertex * 3, (byte) (bitangent.x * 127));
		bitangents.put(vertex * 3 + 1, (byte) (bitangent.y * 127));
		bitangents.put(vertex * 3 + 2, (byte) (bitangent.z * 127));
		needsBufferUpdating = true;
	}

	// only done once, at surface creation.  The index buffer is also updated.
	private void generateIndices() {
		if (baseVertex < 0) { // overflow
			return;
		}

		// the full set of vertices
		baseIndex = nextFreeIndex;
		for (int y = 0; y < height - 1; y++) {
			for (int x = 0; x < width - 1; x++) {
				int vertex = baseVertex + (y * width + x);
				indices.put(nextFreeIndex++, vertex);
				indices.put(nextFreeIndex++, vertex + 1);
				indices.put(nextFreeIndex++, vertex + width);
				indices.put(nextFreeIndex++, vertex + 1);
				indices.put(nextFreeIndex++, vertex + width + 1);
				indices.put(nextFreeIndex++, vertex + width);
			}
		}
		nindices = nextFreeIndex - baseIndex;

		// the mini set of vertices.  Divides the vertices into a 16x8 grid
		baseIndexMini = nextFreeIndex;
		int xInc = Math.max(width / 16, 1);
		int yInc = Math.max(height / 8, 1);
		for (int y = 0; y < height - yInc; y += yInc) {
			for (int x = 0; x < width - xInc; x += xInc) {
				int vertex = baseVertex + (y * width + x);
				indices.put(nextFreeIndex++, vertex);
				indices.put(nextFreeIndex++, vertex + xInc);
				indices.put(nextFreeIndex++, vertex + yInc * width);
				indices.put(nextFreeIndex++, vertex + xInc);
				indices.put(nextFreeIndex++, vertex + xInc + yInc * width);
				indices.put(nextFreeIndex++, vertex + yInc * width);
			}
		}
		nindicesMini = nextFreeIndex - baseIndexMini;

		// the micro set of vertices.  Divides the vertices into a 8x4 grid, which makes most things cuboidal
		baseIndexMicro = nextFreeIndex;
		xInc = Math.max(width / 8, 1);
		yInc = Math.max(height / 4, 1);
		for (int y = 0; y < height - yInc; y += yInc) {
			for (int x = 0; x < width - xInc; x += xInc) {
				int vertex = baseVertex + (y * width + x);
				indices.put(nextFreeIndex++, vertex);
				indices.put(nextFreeIndex++, vertex + xInc);
				indices.put(nextFreeIndex++, vertex + yInc * width);
				indices.put(nextFreeIndex++, vertex + xInc);
				indices.put(nextFreeIndex++, vertex + xInc + yInc * width);
				indices.put(nextFreeIndex++, vertex + yInc * width);
			}
		}
		nindicesMicro = nextFreeIndex - baseIndexMicro;

		// the nano set of vertices.  Divides the vertices into a 4x2 grid, which makes most things cuboidal
		baseIndexNano = nextFreeIndex;
		xInc = Math.max(width / 4, 1);
		yInc = Math.max(height / 2, 1);
		for (int y = 0; y < height - yInc; y += yInc) {
			for (int x = 0; x < width - xInc; x += xInc) {
				int vertex = baseVertex + (y * width + x);
				indices.put(nextFreeIndex++, vertex);
				indices.put(nextFreeIndex++, vertex + xInc);
				indices.put(nextFreeIndex++, vertex + yInc * width);
				indices.put(nextFreeIndex++, vertex + xInc);
				indices.put(nextFreeIndex++, vertex + xInc + yInc * width);
				indices.put(nextFreeIndex++, vertex + yInc * width);
			}
		}
		nindicesNano = nextFreeIndex - baseIndexNano;

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
		indices.position(baseIndex);
		GLES20.glBufferSubData(GLES20.GL_ELEMENT_ARRAY_BUFFER, baseIndex * 4, (nextFreeIndex - baseIndex) * 4, indices);
		checkGLError();
	}

	/**
	 * Initially generates the texture coordinates based on
	 * the vertex position.
	 */
	private void generateTextureCoords() {
		if (baseVertex < 0) { // overflow
			return;
		}
		int index = baseVertex;
		for (int y = 0; y < height; y++) {
			float ycoord = 1.0f / (height - 1) * (height - y - 1) - 0.5f;
			for (int x = 0; x < width; x++) {
				float xcoord = 0.5f - 1.0f / (width - 1) * x;
				textureCoords.put(index * 2, floatToHalf(xcoord));
				textureCoords.put(index * 2 + 1, floatToHalf(ycoord));
				index++;
			}
		}
		needsBufferUpdating = true;
	}

	/**
	 * Generate texture coords based on the vertex values in the X and Y dimensions.
	 */
	public void generateTextureCoordsXY(float xScale, float yScale) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int index = baseVertex;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				textureCoords.put(index * 2, floatToHalf(xScale * getVertexX(x, y)));
				textureCoords.put(index * 2 + 1, floatToHalf(yScale * getVertexY(x, y)));
				index++;
			}
		}
	}

	/**
	 * Generate texture coords based on the vertex values.
	 */
	public void generateTextureCoordsXZ(float xScale, float zScale) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int index = baseVertex;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				textureCoords.put(index * 2, floatToHalf(xScale * getVertexX(x, y)));
				textureCoords.put(index * 2 + 1, floatToHalf(zScale * getVertexZ(x, y)));
				index++;
			}
		}
	}

	/**
	 * Generate texture coords based on the vertex values in the Y and Z dimensions.
	 */
	public void generateTextureCoordsYZ(float yScale, float zScale) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int index = baseVertex;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				textureCoords.put(index * 2, floatToHalf(yScale * getVertexY(x, y)));
				textureCoords.put(index * 2 + 1, floatToHalf(zScale * getVertexZ(x, y)));
				index++;
			}
		}
	}

	/**
	 * Use after generateTextureCoords to adjust the pre-generated texture coords,
	 * which assume an evenly spaced grid.
	 */
	public void setTextureCoordinate(int x, int y, float px, float py) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int vertex = baseVertex + (y * width + x);
		textureCoords.put(vertex * 2, floatToHalf(px));
		textureCoords.put(vertex * 2 + 1, floatToHalf(py));
	}

	public void adjustTextureCoords(float[] textureMatrix) {
		if (baseVertex < 0) { // overflow
			return;
		}
		int index = baseVertex;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float xcoord = textureCoords.get(index * 2);
				float ycoord = textureCoords.get(index * 2 + 1);

				float[] rhsVec = new float[] { xcoord, ycoord, 1, 1 };
				float[] resultVec = new float[4];
				Matrix.multiplyMV(resultVec, 0, textureMatrix, 0, rhsVec, 0);
				xcoord = resultVec[0];
				ycoord = resultVec[1];

				textureCoords.put(index * 2, floatToHalf(xcoord));
				textureCoords.put(index * 2 + 1, floatToHalf(ycoord));
				index++;
			}
		}
	}

	public void generateNormals() {
		generateNormals(false, false);
	}

	public void generateNormals(boolean stitchx, boolean stitchy) {
		if (baseVertex < 0) { // overflow
			return;
		}
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		Point3f p3 = new Point3f();
		Point3f p4 = new Point3f();
		Point3f v1 = new Point3f();
		Point3f v2 = new Point3f();
		Point3f normal = new Point3f();

		// calculate all the mid-point normals
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				getSafeVertex(x - 1, y - 1, p1, stitchx, stitchy);
				getSafeVertex(x + 1, y - 1, p2, stitchx, stitchy);
				getSafeVertex(x - 1, y + 1, p3, stitchx, stitchy);
				getSafeVertex(x + 1, y + 1, p4, stitchx, stitchy);
				calculateVector(p1, p4, v1);
				calculateVector(p2, p3, v2);
				normalize(v1);
				normalize(v2);
				calculateCrossProduct(v1, v2, normal);
				normalize(normal);
				setNormal(x, y, normal);
				setTangent(x, y, v1);
				setBitangent(x, y, v2);
			}
		}
	}

	private void getSafeVertex(int x, int y, Point3f p, boolean stitchx, boolean stitchy) {
		if (x < 0) {
			if (stitchx) {
				x = width - 2;
			} else {
				x = 0;
			}
		} else if (x >= width) {
			if (stitchx) {
				x = 1;
			} else {
				x = width - 1;
			}
		}
		if (y < 0) {
			if (stitchy) {
				y = height - 2;
			} else {
				y = 0;
			}
		} else if (y >= height) {
			if (stitchy) {
				y = 1;
			} else {
				y = height - 1;
			}
		}
		getVertex(x, y, p);
	}

	private void calculateVector(Point3f p1, Point3f p2, Point3f v) {
		v.x = p2.x - p1.x;
		v.y = p2.y - p1.y;
		v.z = p2.z - p1.z;
	}

	private void calculateCrossProduct(Point3f v1, Point3f v2, Point3f v3) {
		v3.x = v1.y * v2.z - v2.y * v1.z;
		v3.y = v1.z * v2.x - v2.z * v1.x;
		v3.z = v1.x * v2.y - v2.x * v1.y;
	}

	private void normalize(Point3f v) {
		float mag = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
		v.x = v.x / mag;
		v.y = v.y / mag;
		v.z = v.z / mag;
	}

	/**
	 * Binds the buffers so that they will be able to be "sent" to the GPU.
	 */
	private static void bindBuffers() {
		System.out.println("GLSurface.bindBuffers");

		// the vertex coordinates
		vertices.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesBufferId);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.capacity() * 2, vertices, GLES20.GL_DYNAMIC_DRAW);
		checkGLError();

		// the normal info
		normals.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBufferId);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normals.capacity(), normals, GLES20.GL_DYNAMIC_DRAW);
		checkGLError();

		// the tangents info
		tangents.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, tangentsBufferId);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tangents.capacity(), tangents, GLES20.GL_DYNAMIC_DRAW);
		checkGLError();

		// the bitangents info
		bitangents.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bitangentsBufferId);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bitangents.capacity(), bitangents, GLES20.GL_DYNAMIC_DRAW);
		checkGLError();

		// texture coordinates
		textureCoords.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordsBufferId);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureCoords.capacity() * 2, textureCoords, GLES20.GL_DYNAMIC_DRAW);
		checkGLError();

		// indices
		indices.position(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 4, indices, GLES20.GL_DYNAMIC_DRAW);
		checkGLError();

		Shader.setBuffers(verticesBufferId, normalsBufferId, tangentsBufferId, bitangentsBufferId, textureCoordsBufferId, indicesBufferId, pointVerticesBufferId, pointVertices, extrasBufferId, extras);
	}

	/**
	 * Sends this surfaces buffer data to the GPU to update the rendering of this surface.
	 */
	private  void updateBuffers() {
		System.out.println("GLSurface.updateBuffers");

		// the vertex coordinates
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesBufferId);
		vertices.position(baseVertex * 3);
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, baseVertex * 2 * 3 , nvertices * 2 * 3 , vertices);
		checkGLError();

		// the normal info
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBufferId);
		normals.position(baseVertex * 3);
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, baseVertex * 1 * 3, nvertices * 1 * 3, normals);
		checkGLError();

		// the tangents info
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, tangentsBufferId);
		tangents.position(baseVertex * 3);
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, baseVertex * 1 * 3, nvertices * 1 * 3, tangents);
		checkGLError();

		// the bitangents info
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bitangentsBufferId);
		bitangents.position(baseVertex * 3);
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, baseVertex * 1 * 3, nvertices * 1 * 3, bitangents);
		checkGLError();

		// texture coordinates
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordsBufferId);
		textureCoords.position(baseVertex * 2);
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, baseVertex * 2 * 2, nvertices * 2 * 2, textureCoords);
		checkGLError();

		// Note:  The indices buffer is independent of the vertex values so it doesn't need updating

		needsBufferUpdating = false;
	}

	private static void checkGLError() {
		int error = GLES20.glGetError();
		if (error != 0) {
			System.out.println("-- GL Error "+error);
		}
	}

	/**
	 * Draws the surface
	 */
	public void draw(Shader shader, int drawType, float[] textureMatrix, float[] color, float shininess, boolean fullBright, boolean alphaTest, int lod) {

		if (baseVertex < 0) { // overflow
			return;
		}

		if (needsBufferUpdating) {
			updateBuffers();
		}

		if (lod == WWObject.RENDER_LOD_FULL) {
			shader.drawTriangles(nindices, baseIndex, textureMatrix, color, shininess, fullBright ? 1 : 0, alphaTest);
		} else if (lod == WWObject.RENDER_LOD_MINI) {
			shader.drawTriangles(nindicesMini, baseIndexMini, textureMatrix, color, shininess, fullBright ? 1 : 0, alphaTest);
		} else if (lod == WWObject.RENDER_LOD_MICRO) {
			shader.drawTriangles(nindicesMicro, baseIndexMicro, textureMatrix, color, shininess, fullBright ? 1 : 0, alphaTest);
		} else if (lod == WWObject.RENDER_LOD_NANO) {
			shader.drawTriangles(nindicesNano, baseIndexNano, textureMatrix, color, shininess, fullBright ? 1 : 0, alphaTest);
		}
	}

}
