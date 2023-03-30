package com.gallantrealm.myworld.android.renderer;

public class ShadowMapShader extends Shader {

	public ShadowMapShader() {
		String vs = "" + //
				"#version 300 es\n" + //
				"precision highp float; \n" + //
				"\n" + //
				"in vec4 aPosition; \n" + //
				"in lowp vec2 aTextureCoord;\n" +
				"\n" + //
				"uniform mat4 modelMatrix; \n" + //
				"uniform mat4 sunViewMatrix; \n" + //
				"uniform vec3 viewPosition; \n" + //
				"uniform mat4 textureMatrix;\n" +
				"\n" +
				"out lowp vec2 textureCoord;\n" +
				"\n" + //
				"void main() { \n" + //
				"	gl_Position = sunViewMatrix * modelMatrix * aPosition; \n" + //
				"	textureCoord = (textureMatrix * vec4(aTextureCoord.x, aTextureCoord.y, 1.0, 1.0)).xy;\n" +
				"} \n";
		String fs = "" + //
				"#version 300 es\n" + //
				"precision highp float; \n" + //
				"\n" + //
				"uniform lowp sampler2D colorTexture; // the texture of the material\n" +
				"\n" + //
				"in lowp vec2 textureCoord; // the location on the texture\n" +
				"\n" + //
				"out vec4 fragColor; \n" + //
				"\n" + //
				"void main() { \n" + //
				"	lowp vec4 textureColor = texture(colorTexture, textureCoord); \n" +
				"	if (textureColor.a < 0.5) {\n" +
				"	  discard;\n" +
				"	} \n" +
				"	fragColor = vec4(1.0); // needed or linker errors sometimes \n" + //
				"} \n";
		init(vs, fs);
	}

}
