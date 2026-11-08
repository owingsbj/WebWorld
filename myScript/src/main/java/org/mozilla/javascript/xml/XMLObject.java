package org.mozilla.javascript.xml;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.Scriptable;

public abstract class XMLObject extends IdScriptableObject {
    public abstract Object addValues(Context cx, boolean thisLeft, Object value);

    public Object get(Context cx, Object elem) {
        return null;
    }
    public void put(Context cx, Object elem, Object value) {}
    public boolean has(Context cx, String id) {
        return false;
    }
    public Scriptable enterWith(Scriptable scope) {
        return null;
    }
    public Scriptable enterDotQuery(Scriptable scope) {
        return null;
    }
    public Ref memberRef(Context cx, Object elem, int memberTypeFlags) {
        return null;
    }
    public Ref memberRef(Context cx, Object namespace, Object elem, int memberTypeFlags) {
        return null;
    }
}
