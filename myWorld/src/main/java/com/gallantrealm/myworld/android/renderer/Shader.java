/**
 * Represents a shader object
 */

package com.gallantrealm.myworld.android.renderer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import android.opengl.GLES30;
import android.opengl.GLES30;

public abstract class Shader {

	// programs, both non-alpha testing (faster) and alpha testing (slower)
	public int program = -1;

	private int aPositionLocation = -1;
	private int aNormalLocation = -1;
	private int aTangentLocation = -1;
	private int aBitangentLocation = -1;
	private int aTextureCoordLocation = -1;

	private int modelMatrixLocation = -1;
	private int viewMatrixLocation = -1;
	private int sunViewMatrixLocation = -1;
	private int textureMatrixLocation = -1;

	private int colorTextureLocation = -1;
	private int shadowMapTextureLocation = -1;
	private int staticShadowMapTextureLocation = -1;
	private int bumpMapTextureLocation = -1;

	private int sunPositionLocation = -1;
	private int sunColorLocation = -1;
	private int sunIntensityLocation = -1;
	private int ambientLightIntensityLocation = -1;
	private int viewPositionLocation = -1;
	private int fullBrightLocation = -1;
	private int colorLocation = -1;
	private int shininessLocation = -1;
	private int fogDensityLocation = -1;
	private int pointDrawLocation = -1;

	public final void setSunPosition(float x, float y, float z) {
		if (sunPositionLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			// must be normalized
			float length = (float) Math.sqrt(x * x + y * y + z * z);
			if (length > 0) {
				x = x / length;
				y = y / length;
				z = z / length;
			}
			GLES30.glUniform3f(sunPositionLocation, x, z, y);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setViewPosition(float x, float y, float z) {
		if (viewPositionLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			// must be normalized
			float length = (float) Math.sqrt(x * x + y * y + z * z);
			if (length > 0) {
				x = x / length;
				y = y / length;
				z = z / length;
			}
			GLES30.glUniform3f(viewPositionLocation, x, z, y);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setSunColor(float red, float green, float blue) {
		if (sunColorLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniform4f(sunColorLocation, red, green, blue, 1.0f);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setSunIntensity(float sunIntensity) {
		if (sunIntensityLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniform1f(sunIntensityLocation, sunIntensity);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setAmbientLightIntensity(float ambientLightIntensity) {
		if (ambientLightIntensityLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniform1f(ambientLightIntensityLocation, ambientLightIntensity);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setFogDensity(float fogDensity) {
		if (fogDensityLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniform1f(fogDensityLocation, fogDensity);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setViewMatrix(float[] viewMatrix) {
		if (viewMatrixLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setSunViewMatrix(float[] sunViewMatrix) {
		if (sunViewMatrixLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniformMatrix4fv(sunViewMatrixLocation, 1, false, sunViewMatrix, 0);
			AndroidRenderer.checkGlError();
		}
	}

	public final void setModelMatrix(float[] modelMatrix) {
		if (modelMatrixLocation >= 0) {
			if (program != lastProgram) {
				GLES30.glUseProgram(program);
				lastProgram = program;
			}
			GLES30.glUniformMatrix4fv(modelMatrixLocation, 1, false, modelMatrix, 0);
			AndroidRenderer.checkGlError();
		}
	}

	static int lastProgram;
	static int lastDrawProgram;
	static int verticesBufferId;
	static int normalsBufferId;
	static int tangentsBufferId;
	static int bitangentsBufferId;
	static int textureCoordsBufferId;
	static int indicesBufferId;
	static int pointVerticesBufferId;
	static FloatBuffer pointVerticesBuffer;
	static int extrasBufferId;
	static ShortBuffer extrasBuffer;

	public static final void setBuffers(int verticesBufferId, int normalsBufferId, int tangentsBufferId, int bitangentsBufferId, int textureCoordsBufferId, int indicesBufferId, int pointVerticesBufferId, FloatBuffer pointVerticesBuffer,
			int extrasBufferId, ShortBuffer extrasBuffer) {
		Shader.verticesBufferId = verticesBufferId;
		Shader.normalsBufferId = normalsBufferId;
		Shader.tangentsBufferId = tangentsBufferId;
		Shader.bitangentsBufferId = bitangentsBufferId;
		Shader.textureCoordsBufferId = textureCoordsBufferId;
		Shader.indicesBufferId = indicesBufferId;
		Shader.pointVerticesBufferId = pointVerticesBufferId;
		Shader.pointVerticesBuffer = pointVerticesBuffer;
		Shader.extrasBufferId = extrasBufferId;
		Shader.extrasBuffer = extrasBuffer;
		lastDrawProgram = -1;
	}

	private float[] lastTextureMatrix = new float[16];

	/**
	 * Draw a strip of triangles.
	 */
	public final void drawTriangleStrip(int nindices, int baseIndex, float[] textureMatrix, float[] color, float shininess, int fullBright) {
		if (program != lastProgram) {
			GLES30.glUseProgram(program);
			lastProgram = program;
		}
		if (textureMatrixLocation >= 0) {
			if (!Arrays.equals(textureMatrix, lastTextureMatrix)) {
				GLES30.glUniformMatrix4fv(textureMatrixLocation, 1, false, textureMatrix, 0);
				AndroidRenderer.checkGlError();
				System.arraycopy(textureMatrix, 0, lastTextureMatrix, 0, 16);
			}
		}
		if (colorLocation >= 0) {
			GLES30.glUniform4fv(colorLocation, 1, color, 0);
			AndroidRenderer.checkGlError();
		}
		if (shininessLocation >= 0) {
			GLES30.glUniform1f(shininessLocation, shininess);
			AndroidRenderer.checkGlError();
		}
		if (fullBrightLocation >= 0) {
			GLES30.glUniform1i(fullBrightLocation, fullBright);
			AndroidRenderer.checkGlError();
		}

		if (program != lastDrawProgram) { // need to rebind
			if (aPositionLocation >= 0) {
				GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, verticesBufferId);
				GLES30.glEnableVertexAttribArray(aPositionLocation);
				GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_HALF_FLOAT, false, 3 * 2, 0); // vertices);
				AndroidRenderer.checkGlError();
			}
			if (aNormalLocation >= 0) {
				GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, normalsBufferId);
				GLES30.glEnableVertexAttribArray(aNormalLocation);
				GLES30.glVertexAttribPointer(aNormalLocation, 3, GLES30.GL_BYTE, false, 3 * 1, 0); // normals);
				AndroidRenderer.checkGlError();
			}
			if (aTangentLocation >= 0) {
				GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, tangentsBufferId);
				GLES30.glEnableVertexAttribArray(aTangentLocation);
				GLES30.glVertexAttribPointer(aTangentLocation, 3, GLES30.GL_BYTE, false, 3 * 1, 0); // tangents);
				AndroidRenderer.checkGlError();
			}
			if (aBitangentLocation >= 0) {
				GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bitangentsBufferId);
				GLES30.glEnableVertexAttribArray(aBitangentLocation);
				GLES30.glVertexAttribPointer(aBitangentLocation, 3, GLES30.GL_BYTE, false, 3 * 1, 0); // bitangents);
				AndroidRenderer.checkGlError();
			}
			if (aTextureCoordLocation >= 0) {
				GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, textureCoordsBufferId);
				GLES30.glEnableVertexAttribArray(aTextureCoordLocation);
				GLES30.glVertexAttribPointer(aTextureCoordLocation, 2, GLES30.GL_HALF_FLOAT, false, 2 * 2, 0); // textureCoords);
				AndroidRenderer.checkGlError();
			}
			GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
			AndroidRenderer.checkGlError();
			lastDrawProgram = program;
		}
		GLES30.glDrawElements(GLES30.GL_TRIANGLE_STRIP, nindices, GLES30.GL_UNSIGNED_INT, baseIndex * 4); // indices);
		AndroidRenderer.checkGlError();
	}

	/**
	 * Draw all points given the id's of the buffers containing vertex information.  This is used for particle emitters.
	 */
	public final void drawPoints(int nindices, float[] pointVertices, short[] extras, float[] textureMatrix, float[] color, float shininess, int fullBright, boolean alphaTest) {
		if (program != lastProgram) {
			GLES30.glUseProgram(program);
			lastProgram = program;
		}

		if (textureMatrixLocation >= 0) {
			GLES30.glUniformMatrix4fv(textureMatrixLocation, 1, false, textureMatrix, 0);
			AndroidRenderer.checkGlError();
		}
		if (colorLocation >= 0) {
			GLES30.glUniform4fv(colorLocation, 1, color, 0);
			AndroidRenderer.checkGlError();
		}
		if (shininessLocation >= 0) {
			GLES30.glUniform1f(shininessLocation, shininess);
			AndroidRenderer.checkGlError();
		}
		if (fullBrightLocation >= 0) {
			GLES30.glUniform1i(fullBrightLocation, 1); // fullBright);
			AndroidRenderer.checkGlError();
		}
		if (pointDrawLocation >= 0) {
			GLES30.glUniform1i(pointDrawLocation, 1);
			AndroidRenderer.checkGlError();
		}

		// use a unique vertex buffer since point data changes
		if (aPositionLocation >= 0) {
			pointVerticesBuffer.clear();
			pointVerticesBuffer.put(pointVertices);
			pointVerticesBuffer.position(0);
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, pointVerticesBufferId);
			GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, nindices * 4 * 3, pointVerticesBuffer, GLES30.GL_DYNAMIC_DRAW);
			GLES30.glEnableVertexAttribArray(aPositionLocation);
			GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 3 * 4, 0); // vertices);
			AndroidRenderer.checkGlError();
		}
		if (aTextureCoordLocation >= 0) {
			extrasBuffer.clear();
			extrasBuffer.put(extras);
			extrasBuffer.position(0);
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, extrasBufferId);
			GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, nindices * 2 * 2, extrasBuffer, GLES30.GL_DYNAMIC_DRAW);
			GLES30.glEnableVertexAttribArray(aTextureCoordLocation);
			GLES30.glVertexAttribPointer(aTextureCoordLocation, 2, GLES30.GL_SHORT, false, 2 * 2, 0); // textureCoords);
			AndroidRenderer.checkGlError();
		}

		GLES30.glDrawArrays(GLES30.GL_POINTS, 0, nindices);
		AndroidRenderer.checkGlError();

		// restore usual vertex and texture buffers
		if (aPositionLocation >= 0) {
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, verticesBufferId);
			GLES30.glEnableVertexAttribArray(aPositionLocation);
			GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_HALF_FLOAT, false, 3 * 2, 0); // vertices);
			AndroidRenderer.checkGlError();
		}
		if (aTextureCoordLocation >= 0) {
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, textureCoordsBufferId);
			GLES30.glEnableVertexAttribArray(aTextureCoordLocation);
			GLES30.glVertexAttribPointer(aTextureCoordLocation, 2, GLES30.GL_HALF_FLOAT, false, 2 * 2, 0); // textureCoords);
			AndroidRenderer.checkGlError();
		}

		if (pointDrawLocation >= 0) {
			GLES30.glUniform1i(pointDrawLocation, 0);
			AndroidRenderer.checkGlError();
		}
	}

	/**
	 * Sets up everything
	 * 
	 * @param vs
	 *            the vertex shader
	 * @param fs
	 *            the fragment shader
	 */
	public final void init(String vs, String fs) {
		System.out.println(">" + this.getClass().getSimpleName() + ".init");

		// create the programs
		program = createProgram(vs, fs);

		// get the locations of all shader vars (assumed same for program and alphaProgram)
		System.out.println(this.getClass().getSimpleName() + ": getting locations of shader vars");

		aPositionLocation = GLES30.glGetAttribLocation(program, "aPosition");
		aNormalLocation = GLES30.glGetAttribLocation(program, "aNormal");
		aTangentLocation = GLES30.glGetAttribLocation(program, "aTangent");
		aBitangentLocation = GLES30.glGetAttribLocation(program, "aBitangent");
		aTextureCoordLocation = GLES30.glGetAttribLocation(program, "aTextureCoord");

		sunPositionLocation = GLES30.glGetUniformLocation(program, "sunPosition");
		sunColorLocation = GLES30.glGetUniformLocation(program, "sunColor");
		sunIntensityLocation = GLES30.glGetUniformLocation(program, "sunIntensity");
		ambientLightIntensityLocation = GLES30.glGetUniformLocation(program, "ambientLightIntensity");
		fullBrightLocation = GLES30.glGetUniformLocation(program, "fullBright");
		modelMatrixLocation = GLES30.glGetUniformLocation(program, "modelMatrix");
		viewMatrixLocation = GLES30.glGetUniformLocation(program, "viewMatrix");
		sunViewMatrixLocation = GLES30.glGetUniformLocation(program, "sunViewMatrix");
		viewPositionLocation = GLES30.glGetUniformLocation(program, "viewPosition");
		textureMatrixLocation = GLES30.glGetUniformLocation(program, "textureMatrix");

		colorLocation = GLES30.glGetUniformLocation(program, "color");
		shininessLocation = GLES30.glGetUniformLocation(program, "shininess");
		colorTextureLocation = GLES30.glGetUniformLocation(program, "colorTexture");
		shadowMapTextureLocation = GLES30.glGetUniformLocation(program, "shadowMapTexture");
		staticShadowMapTextureLocation = GLES30.glGetUniformLocation(program, "staticShadowMapTexture");
		bumpMapTextureLocation = GLES30.glGetUniformLocation(program, "bumpTexture");
		fogDensityLocation = GLES30.glGetUniformLocation(program, "fogDensity");
		pointDrawLocation = GLES30.glGetUniformLocation(program, "pointDraw");

		// Some of the glGetUniforms or glUniforms fail (expected)
		AndroidRenderer.ignoreGlError();

		System.out.println(this.getClass().getSimpleName() + ": Mapping textures to shader vars");
		GLES30.glUseProgram(program);
		AndroidRenderer.checkGlError();
		GLES30.glUniform1i(colorTextureLocation, 0);
		AndroidRenderer.checkGlError();
		GLES30.glUniform1i(shadowMapTextureLocation, 1);
		AndroidRenderer.checkGlError();
		GLES30.glUniform1i(staticShadowMapTextureLocation, 2);
		AndroidRenderer.checkGlError();
		GLES30.glUniform1i(bumpMapTextureLocation, 3);
		AndroidRenderer.checkGlError();

		System.out.println("<" + this.getClass().getSimpleName() + ".init");
	}

	/**
	 * Creates a shader program.
	 */
	private final int createProgram(String vs, String fs) {

		System.out.println(this.getClass().getSimpleName() + ": Compiling vertex shader");
		int _vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vs);

		System.out.println(this.getClass().getSimpleName() + ": Compiling fragment shader");
		int _pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fs);

		// Create the program
		System.out.println(this.getClass().getSimpleName() + ":  Linking shaders ");
		int program = GLES30.glCreateProgram();
		GLES30.glAttachShader(program, _vertexShader);
		AndroidRenderer.checkGlError();
		GLES30.glAttachShader(program, _pixelShader);
		AndroidRenderer.checkGlError();
		GLES30.glLinkProgram(program);
		int[] linkStatus = new int[1];
		GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES30.GL_TRUE) {
			System.err.println(this.getClass().getSimpleName() + ": Linking failed: " + GLES30.glGetProgramInfoLog(program));
			GLES30.glDeleteProgram(program);
		}
		AndroidRenderer.checkGlError();

		return program;
	}

	/**
	 * Loads a shader (either vertex or fragment) given the source
	 * 
	 * @param shaderType
	 *            VERTEX or PIXEL
	 * @param source
	 *            The string data representing the shader code
	 * @return handle for shader
	 */
	private final int loadShader(int shaderType, String source) {
		AndroidRenderer.ignoreGlError();
		int shader = GLES30.glCreateShader(shaderType);
		AndroidRenderer.checkGlError();
		if (shader != 0) {
			GLES30.glShaderSource(shader, source);
			GLES30.glCompileShader(shader);
			AndroidRenderer.checkGlError();
			int[] compiled = new int[1];
			GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				System.err.println(this.getClass().getSimpleName() + " compile failed for shader type " + shaderType + ":" + GLES30.glGetShaderInfoLog(shader));
				AndroidRenderer.checkGlError();
			}
		}
		return shader;
	}

}
