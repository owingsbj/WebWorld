# myScript

A subset of JavaScript, used for embedding in apps.

This is a heavily pruned copy of **Mozilla Rhino 1.7.8**.

## Included Subsection / Components
- **Core Engine**: Parser, AST (`org.mozilla.javascript.ast`), Interpreter, Context, Scope, ScriptRuntime, and standard native objects.
- **Supporting Subpackages**:
  - V8 Floating Point Conversion (`org.mozilla.javascript.v8dtoa`)
  - Regular Expressions (`org.mozilla.javascript.regexp`)
  - Annotations (`org.mozilla.javascript.annotations`)

## Pruned / Removed Packages & Features
- **CommonJS Module Support** (`org.mozilla.javascript.commonjs.module`)
- **XML / E4X Support** (`org.mozilla.javascript.xml`, `org.mozilla.javascript.xmlimpl`)
- **Typed Arrays** (`org.mozilla.javascript.typedarrays`)
- **Serialization** (`org.mozilla.javascript.serialize`)
- **Debugging API** (`org.mozilla.javascript.debug`)
- **Optimizer Package** (`org.mozilla.javascript.optimizer`)
- **Tools Package** (`org.mozilla.javascript.tools`)

## Custom Modifications
1. **`JavaMembers.java`**: Direct Java field reflection has been disabled/commented out. JavaScript code cannot access public Java fields directly; only Java accessor methods (getters and setters) are permitted.
2. **`Messages.properties`**: Customized error messages for read-only properties (`msg.set.prop.no.setter` changed to report properties as read-only).