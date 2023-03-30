package com.gallantrealm.myworld.android.renderer;

public class ShadowingTextureShader extends Shader {
	
	public static final 		String vs = //
			"#version 300 es\n" + //
			"precision highp float;\n" + //
			"\n" + //
			"in vec4 aPosition;\n" + //
			"in lowp vec3 aNormal;\n" + //
			"in lowp vec3 aTangent;\n" + //
			"in lowp vec3 aBitangent;\n" + //
			"in lowp vec2 aTextureCoord;\n" + //
			"\n" + //
			"uniform vec3 sunPosition;\n" + //
			"uniform lowp vec4 sunColor;\n" + //
			"uniform lowp float sunIntensity;\n" + //
			"uniform lowp float ambientLightIntensity;\n" + //
			"uniform mat4 modelMatrix;\n" + //
			"uniform mat4 viewMatrix;\n" + //
			"uniform mat4 sunViewMatrix;\n" + //
			"uniform mat4 textureMatrix;\n" + //
			"uniform vec3 viewPosition; \n" + //
			"uniform bool pointDraw; \n" + //
			"\n" + //
			"out lowp vec2 textureCoord;\n" + //
			"out highp vec4 shadowCoord;\n" + //
			"out lowp vec3 mNormal;\n" + //
			"out lowp vec3 mTangent;\n" + //
			"out lowp vec3 mBitangent;\n" + //
			"out vec3 surfaceToCamera; \n" + //
			"out vec3 surfaceToLight; \n" + //
			"out float fogDepth;\n" + //
			"out float pointAlpha; \n" + //
			"\n" + //
			"void main() {\n" + //
			"	gl_Position = viewMatrix * modelMatrix * aPosition;\n" + //
			"	shadowCoord = sunViewMatrix * modelMatrix * aPosition; \n" + //
			"	shadowCoord = (shadowCoord / shadowCoord.w + 1.0) /2.0;\n" + //
			"	mNormal = normalize((modelMatrix * vec4(aNormal, 0.0)).xyz);\n" + //
			"	mTangent = normalize((modelMatrix * vec4(aTangent, 0.0)).xyz);\n" + //
			"	mBitangent = normalize((modelMatrix * vec4(aBitangent, 0.0)).xyz);\n" + //
			"	surfaceToCamera = normalize(viewPosition); \n" + //
			"	surfaceToLight = normalize(sunPosition); \n" + //
			"	textureCoord = (textureMatrix * vec4(aTextureCoord.x, aTextureCoord.y, 1.0, 1.0)).xy;\n" + //
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
			"uniform lowp float shininess; \n" + //
			"uniform lowp float sunIntensity; \n" + //
			"uniform bool fullBright; \n" + //
			"uniform lowp float ambientLightIntensity; \n" + //
			"uniform highp sampler2DShadow shadowMapTexture; \n" + //
			"uniform vec3 sunPosition;\n" + //
			"uniform vec3 viewPosition;\n" + //
			"uniform lowp sampler2D bumpTexture; \n" + //
			"uniform bool pointDraw; \n" + //
			"\n" + //
			"in lowp vec2 textureCoord; // the location on the texture \n" + //
			"in lowp vec3 mNormal;\n" + //
			"in lowp vec3 mTangent;\n" + //
			"in lowp vec3 mBitangent;\n" + //
			"in vec3 surfaceToCamera; \n" + //
			"in vec3 surfaceToLight; \n" + //
			"in highp vec4 shadowCoord; // location on the shadow map \n" + //
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
			"  lowp float shadow = texture(shadowMapTexture, shadowCoord.xyz); \n" + //
			"  lowp float diffuseLightIntensity = 0.0; \n" + //
			"  lowp float specularLightIntensity = 0.0; \n" + //
			"  if (fullBright) { \n" + //
			"    diffuseLightIntensity = 1.0; \n" + //
			"  } else { \n" + //
			"      lowp vec4 bump = (texture(bumpTexture, textureCoord) - 0.5) * 2.0; \n" + //
			"      lowp vec3 bumpNormal = bump.r * mTangent + bump.g * mBitangent + bump.b * mNormal; \n" + //
			"	    diffuseLightIntensity = shadow * sunIntensity * max(-0.0,dot(sunPosition, -bumpNormal));\n" + //
			"	    lowp float specular = max(0.0, dot(surfaceToCamera, reflect(-surfaceToLight, bumpNormal))); \n" + //
			"	    specularLightIntensity = shadow * sunIntensity * clamp(pow(specular, 1.0+ 50.0* shininess) * 2.5 * shininess, 0.0, 1.0); \n" + //
			"  } \n" + //
			"	lowp float sValue = max(ambientLightIntensity, diffuseLightIntensity); \n" + //
			"	fragColor = sunColor * (specularLightIntensity * vec4(1.0) + color * textureColor * vec4(sValue, sValue, sValue, 1.0)); \n" + //
			"	if (fogDensity > 0.0) { \n" + //
			"		lowp float fog = clamp(exp(-fogDensity*fogDensity * abs(fogDepth) * abs(fogDepth) / 5000.0  ), 0.0, 1.0); \n" + //
			"		fragColor = mix(vec4(1.0), fragColor, fog); \n" + //
			"	} \n" + //
			"}";

	public ShadowingTextureShader() {
		init(vs, fs);
	}
	
}
