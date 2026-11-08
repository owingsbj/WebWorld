package org.mozilla.javascript.debug;

import org.mozilla.javascript.Context;

public interface Debugger {
    void handleCompilationDone(Context cx, DebuggableScript dscript, String debugSource);
    DebugFrame getFrame(Context cx, DebuggableScript dscript);
}
