package org.mozilla.javascript.debug;

public interface DebuggableScript {
    default boolean isFunction() { return false; }
    default String getSourceName() { return null; }
    default int getLineNumber() { return 0; }
    default int getFunctionCount() { return 0; }
    default DebuggableScript getFunction(int index) { return null; }
    default DebuggableScript getParent() { return null; }
    default boolean isTopLevel() { return false; }
    default int[] getLineNumbers() { return null; }
    default String getFunctionName() { return null; }
    default int paramIntCount() { return 0; }
    default String getParamName(int index) { return null; }
    default boolean isScript() { return false; }
    default String[] getParamAndVarNames() { return new String[0]; }
    default int getParamAndVarCount() { return 0; }
}
