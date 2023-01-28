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
			"attribute vec2 aTextureCoord;\n" +
			"\n" +
			"uniform mat4 mvMatrix; \n" +
			"uniform mat4 colorTextureMatrix;\n" +
			"\n" +
			"varying vec2 textureCoord;\n" +
			"\n" +
			"void main() { \n" +
			"	gl_Position = mvMatrix * aPosition; \n" +
			"	textureCoord = (colorTextureMatrix * vec4(aTextureCoord.x, aTextureCoord.y, 1.0, 1.0)).xy;\n" +
			"}";
	
	public static final String fs = "\n" +
			"precision highp float; \n" +
			"\n" +
			"void main() { \n" +
			"	gl_FragColor = vec4(1.0); // needed or linker errors sometimes \n" +
			"}";
	public static final String afs = "" + //
			"precision highp float; \n" + //
			"\n" + //
			"uniform sampler2D colorTexture; // the texture of the material\n" +
			"\n" + //
			"varying vec2 textureCoord; // the location on the texture\n" +
			"\n" + //
			"void main() { \n" + //
			"	vec4 textureColor = texture2D(colorTexture, textureCoord); \n" +
			"	if (textureColor.a < 0.5) {\n" +
			"	  discard;\n" +
			"	} \n" +
			"	gl_FragColor = vec4(1.0); // needed or linker errors sometimes \n" + //
			"} \n";

	public DepthShader() {
		init(vs, fs, afs);
	}

}
