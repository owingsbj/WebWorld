package com.gallantrealm.myworld.android.renderer;

/**
 * This shader is used to fill the depth dimension so that the texture shaders will run more optimally, avoiding invoking
 * the fragement shading for pixels that will not be visible (because the pixel will be redrawn
 */
public class DepthShader extends Shader {
	
	public static final String vs = "\n" +
			"precision highp float; \n" +
			"\n" +
			"attribute vec4 aPosition; \n" +
			"uniform mat4 mvMatrix; \n" +
			"\n" +
			"void main() { \n" +
			"	gl_Position = mvMatrix * aPosition; \n" +
			"}";
	
	public static final String fs = "\n" +
			"precision highp float; \n" +
			"\n" +
			"void main() { \n" +
			"	gl_FragColor = vec4(1.0); // needed or linker errors sometimes \n" +
			"}"; 

	public DepthShader() {
		init(vs, fs, fs);
	}

}
