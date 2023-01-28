package com.gallantrealm.myworld.android.renderer;

public class ShadowMapShader extends Shader {

	public ShadowMapShader() {
		String vs = "" + //
				"precision highp float; \n" + //
				"\n" + //
				"attribute vec4 aPosition; \n" + //
				"attribute vec2 aTextureCoord;\n" +
				"\n" + //
				"uniform mat4 sunMvMatrix; \n" + //
				"uniform vec3 viewPosition; \n" + //
				"uniform mat4 colorTextureMatrix;\n" +
				"\n" +
				"varying vec2 textureCoord;\n" +
				"\n" + //
				"void main() { \n" + //
				"	gl_Position = sunMvMatrix  * aPosition; \n" + //
				"	textureCoord = (colorTextureMatrix * vec4(aTextureCoord.x, aTextureCoord.y, 1.0, 1.0)).xy;\n" +
				"} \n";
		String fs = "" + //
				"precision highp float; \n" + //
				"\n" +
				"uniform sampler2D colorTexture; // the texture of the material\n" +
				"\n" + //
				"varying vec2 textureCoord; // the location on the texture\n" +
				"\n" + //
				"void main() { \n" + //
				"	vec4 textureColor = texture2D(colorTexture, textureCoord); \n" +
				"	//if (textureColor.a < 0.5) {\n" +
				"	//  discard;\n" +
				"	//} \n" +
				"	gl_FragColor = vec4(1.0); // needed or linker errors sometimes \n" + //
				"} \n";
		String afs = "" + //
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
		init(vs, fs, afs);
	}

}
