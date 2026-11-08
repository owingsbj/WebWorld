package org.mozilla.javascript.debug;

import org.mozilla.javascript.Scriptable;

public interface DebuggableObject {
    Object[] getAllIds();
    Object get(String id, Scriptable start);
    boolean has(String id, Scriptable start);
    void put(String id, Scriptable start, Object value);
}
