package org.mozilla.javascript.xml;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.Scriptable;

public abstract class XMLLib {
    public static abstract class Factory {
        public static Factory create(final String implementationClassName) {
            return new Factory() {
                @Override
                public String getImplementationClassName() {
                    return implementationClassName;
                }
            };
        }
        public abstract String getImplementationClassName();
    }
    public static XMLLib extractFromScope(Scriptable scope) {
        return null;
    }
    public Object toDefaultXmlNamespace(Context cx, Object namespace) {
        return null;
    }
    public String escapeAttributeValue(Object value) {
        return "";
    }
    public String escapeTextValue(Object value) {
        return "";
    }
    public Ref nameRef(Context cx, Object name, Scriptable scope, int memberTypeFlags) {
        return null;
    }
    public Ref nameRef(Context cx, Object namespace, Object name, Scriptable scope, int memberTypeFlags) {
        return null;
    }
    public boolean isXMLName(Context cx, Object name) {
        return false;
    }
}
