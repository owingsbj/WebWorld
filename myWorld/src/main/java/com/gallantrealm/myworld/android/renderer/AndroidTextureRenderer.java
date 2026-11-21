package com.gallantrealm.myworld.android.renderer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.gallantrealm.android.HttpFileCache;
import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.client.renderer.ITextureRenderer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.HashMap;

/**
 * Implementation for Android using Opengl.
 *
 * NOTE:  Texure unit 31 (GL_TEXTURE31) is uses a s scratchpad texture unit when loading
 * and configuring textures so it shouldn't be used in shaders.
 */
public class AndroidTextureRenderer implements ITextureRenderer {

    public static HashMap<String, Integer> textureCache = new HashMap<String, Integer>();
    public static HashMap<String, Integer> normalTextureCache = new HashMap<String, Integer>();
    public static HashMap<String, Boolean> hasAlphaCache = new HashMap<String, Boolean>();

    public Context context;
    public boolean simpleRendering;

    public AndroidTextureRenderer(Context context, boolean simpleRendering) {
        this.context = context;
        this.simpleRendering = simpleRendering;
    }

    public final synchronized void clearCaches() {
        textureCache.clear();
        normalTextureCache.clear();
        hasAlphaCache.clear();
    }

    public void destroy() {
        textureCache.clear();
        normalTextureCache.clear();
        hasAlphaCache.clear();
    }

    public synchronized final int getTexture(String textureName, boolean pixelate) {
        if (textureName == null) {
            textureName = "white";
        }
        Integer idInteger = textureCache.get(textureName);
        if (idInteger != null) {
            if (textureName.equals(regenTextureName)) {
                updateTexture(regenBitmap, idInteger);
                regenTextureName = null;
                regenBitmap = null;
            }
            return idInteger;
        } else {
            InputStream is = null;
            boolean isPkm = false;
            try {
                System.out.println("AndroidRenderer.getTexture loading bitmap " + textureName);
                Bitmap bitmap = null;
                if (textureName.equals("surface_select")) { // a special bitmap
                    bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
                    for (int i = 0; i < 512; i++) {
                        int red = ((i & 0x1C0) >> 1) | 0x10;
                        int green = (i & 0x03F) << 2;
                        int blue = 0;
                        int color = 0xFF000000 | (red << 16) | (green << 8) | blue;
                        for (int j = 0; j < 512; j++) {
                            bitmap.setPixel(i, j, color);
                        }
                    }
                } else if (textureName.contains(":")) { // a url
                    Uri uri = Uri.parse(textureName);
                    Bitmap unscaledBitmap = readImageTexture(uri);
                    if (unscaledBitmap != null) {
                        bitmap = Bitmap.createScaledBitmap(unscaledBitmap, 256, 256, false);
                        if (unscaledBitmap != bitmap) {
                            unscaledBitmap.recycle();
                        }
                    }
                } else {
                    if (isPkm) {
                        return genCompressedTexture(textureName);
                    } else {
                        if (textureName.startsWith("/")) { // fully qualified file name
                            try {
                                File file = new File(textureName);
                                is = new BufferedInputStream(new FileInputStream(file), 65536);
                                bitmap = BitmapFactory.decodeStream(is);
                            } catch (Exception e2) {
                                // not there
                            }
                        } else { // open an asset or local file
                            try {
//								try {
//									is = context.getAssets().open(textureName + ".dds"); // dds is faster?
//									return genCompressedTexture(textureName);
//								} catch (Exception e) {
                                is = context.getAssets().open(textureName + ".png"); // asset
                                bitmap = BitmapFactory.decodeStream(is);
//								}
                            } catch (Exception e) {
                                try {
                                    File file = new File(context.getFilesDir(), textureName + ".png"); // local file
                                    is = new BufferedInputStream(new FileInputStream(file), 65536);
                                    bitmap = BitmapFactory.decodeStream(is);
                                } catch (Exception e2) {
                                    // not there
                                }
                            }
                        }
                    }
                }
                if (bitmap == null) { // problems
                    int textureId = getTexture("white", true); // use white texture
                    textureCache.put(textureName, textureId);
                    hasAlphaCache.put(textureName, false);
                    return textureId;
                }
                boolean hasAlpha = bitmap.hasAlpha();

                int textureId = genTexture(bitmap, textureName, pixelate);
                textureCache.put(textureName, textureId);
                hasAlphaCache.put(textureName, hasAlpha);
                return textureId;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }

    final int genTexture(Bitmap bitmap, String textureName, boolean pixelate) {
        System.out.println("AndroidRenderer.genTexture " + textureName);
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE31);  // a scratch texture unit to use just for setting the  parameters
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        if (textureName.equals("surface_select")) {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        } else {
            if (simpleRendering) {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES11.GL_GENERATE_MIPMAP, GLES20.GL_FALSE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, pixelate ? GLES20.GL_NEAREST : GLES20.GL_LINEAR);
            } else {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);
            }
            if (pixelate) {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            } else {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            }
        }
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        // Generate textures (original and mipmaps)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int level = 0;
        try {
            while (height >= 1 || width >= 1) {

                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bitmap, 0);
                // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, GLES20.GL_RGB, bitmap, GLES20.GL_UNSIGNED_SHORT_5_6_5, 0);
                // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0);

                if (height <= 1 || width <= 1) {
                    break;
                }
                if (textureName.equals("surface_select") || height > 1024 || width > 1024) { // avoid mipmapping large textures
                    break;
                }

                // Increase the mipmap level
                level++;

                // gen a scaled mipmap
                height /= 2;
                width /= 2;
                Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);

                // Clean up
                bitmap.recycle();
                bitmap = bitmap2;
            }
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return textureId;
    }

    final int genCompressedTexture(String textureName) {
        System.out.println("AndroidRenderer.genCompressedTexture " + textureName);

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        textureCache.put(textureName, textureId);
        hasAlphaCache.put(textureName, false);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE31);  // a scratch texture unit to use just for setting the  parameters
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        if (simpleRendering) {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES11.GL_GENERATE_MIPMAP, GLES20.GL_FALSE);
        }
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
// GLES20.glTexParameterx(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        try {
            int level = 0;
            while (level <= 9) {
                InputStream is = context.getAssets().open(textureName + "_mip_" + level + ".pkm"); // asset
                ETC1Util.ETC1Texture etc1texture = ETC1Util.createTexture(is);

                // Generate textures (original and mipmaps)
                int width = etc1texture.getWidth();
                int height = etc1texture.getHeight();
                Buffer data = etc1texture.getData();
                int imageSize = data.remaining();
                GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, level, ETC1.ETC1_RGB8_OES, width, height, 0, imageSize, data);
                level++;
            }

        } catch (IOException e) {
            // e.printStackTrace();
        }
        return textureId;
    }

    /**
     * Read the image into a 256x256 bitmap, ready for texture scaling
     *
     * @param selectedImage
     * @return
     */
    public static final Bitmap readImageTexture(Uri selectedImage) {
        if (selectedImage.getScheme() != null && selectedImage.getScheme().startsWith("http")) {
            // from the web
            Bitmap bm = null;
            try {
//				HttpURLConnection connection = (HttpURLConnection) (new URL(selectedImage.toString()).openConnection());
//				InputStream instream = connection.getInputStream();
//				bm = BitmapFactory.decodeStream(instream);
                File imageFile = HttpFileCache.getFile(selectedImage.toString(), ClientModel.getClientModel().getContext());
                if (imageFile != null) {
                    bm = BitmapFactory.decodeFile(imageFile.getPath());
                }
                return bm;
            } catch (Exception e) {
                System.err.println("AndroidRenderer.readImageTexture: " + e.getMessage());
            }
            return bm;
        } else {
            // from assets
            Bitmap bm = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            AssetFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = ClientModel.getClientModel().getContext().getContentResolver().openAssetFileDescriptor(selectedImage, "r");

                // first, get the bitmap size
                options.inJustDecodeBounds = true;
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);

                // then, get a sampled-down bitmap large enough for the texture
                options.inJustDecodeBounds = false;
                options.inSampleSize = Math.max(1, Math.min(options.outWidth, options.outHeight) / 512);
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
            } catch (FileNotFoundException e) {
                System.err.println("AndroidRenderer.readImageTexture: File not found -- " + selectedImage);
            } finally {
                try {
                    if (fileDescriptor != null) {
                        fileDescriptor.close();
                    }
                } catch (IOException e) {
                }
            }

            // Determine the image rotation and antirotate if necessary
            try {
                ExifInterface exif = new ExifInterface(selectedImage.getPath());
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                System.out.println("EXIF rotation = " + exifOrientation);
                int rotation;
                if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    rotation = 90;
                } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    rotation = 180;
                } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotation = 270;
                } else {
                    rotation = 0;
                }
                if (rotation != 0) {
                    android.graphics.Matrix matrix = new android.graphics.Matrix();
                    matrix.preRotate(rotation);
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bm;
        }
    }

    public synchronized final int getNormalTexture(String textureName, boolean pixelate) {
        if (textureName == null) {
            textureName = "flat";
        }
        if (textureName.endsWith(".png")) {
            textureName = textureName.substring(0, textureName.length() - 4) + "_nrm.png";
        } else if (textureName.endsWith(".jpg")) {
            textureName = textureName.substring(0, textureName.length() - 4) + "_nrm.jpg";
        } else {
            textureName = textureName + "_nrm.png";
        }
        Integer idInteger = normalTextureCache.get(textureName);
        if (idInteger != null) {
            return idInteger;
        } else {
            InputStream is = null;
            boolean isPkm = false;
            try {
                System.out.println("AndroidRenderer.getNormalTexture loading bitmap " + textureName);
                Bitmap bitmap = null;
                if (textureName.contains(":")) { // a url
                    Uri uri = Uri.parse(textureName);
                    Bitmap unscaledBitmap = readImageTexture(uri);
                    if (unscaledBitmap != null) {
                        bitmap = Bitmap.createScaledBitmap(unscaledBitmap, 256, 256, false);
                        if (bitmap == null) {
                            bitmap = unscaledBitmap;
                        } else 	if (unscaledBitmap != bitmap) {
                            unscaledBitmap.recycle();
                        }
                    }
                } else {
                    if (isPkm) {
                        return genCompressedTexture(textureName);
                    } else {
                        try {
                            is = context.getAssets().open(textureName); // asset
                        } catch (Exception e) {
                            try {
                                File file = new File(context.getFilesDir(), textureName); // local file
                                is = new BufferedInputStream(new FileInputStream(file), 65536);
                            } catch (Exception e2) {
                                // not there
                            }
                        }
                        if (is != null) {
                            bitmap = BitmapFactory.decodeStream(is);
                        }
                    }
                }
                if (bitmap == null) { // a failure
                    System.out.println("AndroidRenderer.getNormalTexture couldn't find " + textureName + " so will use flat_nrm.png");
                    int textureId = getNormalTexture("flat", true);
                    normalTextureCache.put(textureName, textureId);
                    return textureId;
                }
                int textureId = genTexture(bitmap, textureName, pixelate);
                normalTextureCache.put(textureName, textureId);
                return textureId;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }

    public final synchronized boolean textureHasAlpha(String textureName) {
        Boolean hasAlpha = hasAlphaCache.get(textureName);
        if (hasAlpha == null) {
            return false;
        }
        return hasAlpha;
    }

    final void updateTexture(Bitmap bitmap, int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE31);  // a scratch texture unit to use just for setting the  parameters
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // avoid mipmapping for updated textures to save time
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        // Generate textures (original and mipmaps)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int level = 0;

        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, level, 0, 0, bitmap);
        // GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, level, 0, 0, bitmap, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5);
        // GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, level, 0, 0, bitmap, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE);
    }

    Bitmap regenBitmap;
    String regenTextureName;

    public final synchronized void regenTexture(Bitmap bitmap, String textureName) {
        this.regenBitmap = bitmap;
        this.regenTextureName = textureName;
    }

}
