package com.gallantrealm.myworld.android.renderer;

public class SimpleTextureShader extends Shader {
	
	public static final 		String vs =
			"#version 300 es\n" + //
			"precision highp float;\n" + //
			"\n" + //
			"in vec4 aPosition;\n" + //
			"in lowp vec3 aNormal;\n" + //
			"in lowp vec2 aTextureCoord;\n" + //
			"\n" + //
			"uniform mat4 modelMatrix;\n" + //
			"uniform mat4 viewMatrix;\n" + //
			"uniform mat4 textureMatrix;\n" + //
			"uniform bool pointDraw; \n" + //
			"\n" + //
			"out lowp vec2 textureCoord;\n" + //
			"out lowp vec3 mNormal;\n" + //
			"out float fogDepth;\n" + //
			"out float pointAlpha; \n" + //
			"\n" + //
			"void main() {\n" + //
			"	gl_Position = viewMatrix * modelMatrix * aPosition;\n" + //
			"	textureCoord = (textureMatrix * vec4(aTextureCoord.x, aTextureCoord.y, 1.0, 1.0)).xy;\n" + //
			"	mNormal = normalize((modelMatrix * vec4(aNormal, 0.0)).xyz);\n" + //
			"	fogDepth = gl_Position.z;\n" + //
			"	if (pointDraw) { \n" + // used for rendering particles
			"		gl_PointSize = aTextureCoord.x * 10.0 / (1.0 + gl_Position.z); \n" + //
			"		pointAlpha = aTextureCoord.y / 100.0; \n" + //
			"	} \n" + //
			"}";

	public static final 		String fs =
			"#version 300 es\n" + //
			"precision highp float; \n" + //
			"\n" + //
			"uniform lowp vec4 color; \n" + //
			"uniform lowp sampler2D colorTexture; \n" + //
			"uniform lowp vec4 sunColor;\n" + //
			"uniform vec3 sunPosition;\n" + //
			"uniform lowp float sunIntensity; \n" + //
			"uniform bool fullBright; \n" + //
			"uniform lowp float ambientLightIntensity; \n" + //
			"uniform bool pointDraw; \n" + //
			"\n" + //
			"in lowp vec2 textureCoord; // the location on the texture \n" + //
			"in lowp vec3 mNormal;\n" + //
			"in float pointAlpha; \n" + //
			"\n" + //
			"uniform lowp float fogDensity; \n" + //
			"in float fogDepth; \n" + //
			"\n" + //
			"out vec4 fragColor; \n" + //
			"\n" + //
			"void main() { \n" + //
			"	lowp vec4 textureColor = texture(colorTexture, textureCoord); \n" + //
			"	if (pointDraw) { \n" + //
			"	  textureColor = texture(colorTexture, gl_PointCoord) * vec4(1.0, 1.0, 1.0, pointAlpha); \n" + //
			"	} \n" + //
			"	if (textureColor.a <= 0.5) {\n" + //
			"	  discard;\n" + //
			"	} \n" + //
			"	lowp float diffuseLightIntensity = sunIntensity * max(0.0,dot(sunPosition, -mNormal));\n" +
			"	lowp float sValue = fullBright ? 1.0 : max(ambientLightIntensity, diffuseLightIntensity); \n" + //
			"	fragColor = sunColor * (color * textureColor * vec4(sValue, sValue, sValue, 1.0)); \n" + //
			"	if (fogDensity > 0.0) { \n" + //
			"		lowp float fog = clamp(exp(-fogDensity*fogDensity * abs(fogDepth) * abs(fogDepth) / 5000.0  ), 0.0, 1.0); \n" + //
			"		fragColor = mix(vec4(1.0), fragColor, fog); \n" + //
			"	} \n" + //
			"}";

	public SimpleTextureShader() {
		init(vs, fs);
	}
}
