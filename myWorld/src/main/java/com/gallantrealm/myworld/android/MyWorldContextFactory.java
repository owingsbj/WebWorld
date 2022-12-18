package com.gallantrealm.myworld.android;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public final class MyWorldContextFactory implements GLSurfaceView.EGLContextFactory {

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int[] attrib_list = new int[] {EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE};
        EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attrib_list);
        System.out.println("MyWorldContextFactory.createContext returning a context for client version 3");
        return context;
    }

    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display,  context);
    }

}