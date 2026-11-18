package com.gallantrealm.myworld.client.renderer;

public interface ITextureRenderer {

    /**
     * Return identifier for a texture with the given name.
     */
    int getTexture(String textureName, boolean pixelate);

    /**
     * Return identifier for a normal texture for the texture with the given name.
     * If there is no normal texture then the identifier for a "flat" texture should be returned.
     */
    int getNormalTexture(String textureName, boolean pixelate);

    /**
     * Returns true if the texture with the given name exists and has transparent or translucent
     * areas within the texture.
     */
    boolean textureHasAlpha(String textureName);

}
