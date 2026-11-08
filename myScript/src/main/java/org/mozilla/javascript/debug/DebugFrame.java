package org.mozilla.javascript.debug;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public interface DebugFrame {
    void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args);
    void onLineChange(Context cx, int lineNumber);
    void onExceptionThrow(Context cx, Throwable ex);
    void onExceptionThrown(Context cx, Throwable ex);
    void onExit(Context cx, boolean byWayOfThrow, Object result);
    void onDebuggerStatement(Context cx);
}
