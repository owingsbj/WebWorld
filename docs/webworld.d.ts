// webworld.d.ts
//
// Starter type declarations for the WebWorld avatar/world JavaScript API,
// written from reference.md. This is a global script file: it deliberately has
// no import/export so every declaration is visible in all scripts.
//
// ASSUMPTIONS TO VERIFY (search for "VERIFY"):
//  - Base class name Object3D (the reference only says "3D object").
//  - Return types of mutating Vector/Color methods (declared void).
//  - taper property names (taperX / taperY), and a few other types marked VERIFY.
//  - The reference spells ambientLightIntensity "ambientLIghtIntensity";
//    the correctly spelled name is declared here.

type EventHandler = (...args: any[]) => void;

/** How the joysticks move the avatar. */
type MoveType = "explore" | "confront" | "drive" | "fly";

// ---------------------------------------------------------------------------
// Globals
// ---------------------------------------------------------------------------

/** The 3D object representing the user. Set in the avatar script; may be reassigned in the world script. */
declare var avatar: Object3D;
/** WebWorld's console. */
declare var console: Console;
/** The user currently controlling the avatar. */
declare var user: User;
/** The world. Already exists; never create it. */
declare var world: World;

/** Pop up a message box. Play stops while it is displayed. */
declare function alert(message: string): void;
/** Show a message with OK and Cancel. Returns true if OK is pressed. */
declare function confirm(message: string): boolean;
/** Show a message and an input box initially filled with defaultText. Returns the text, or null if cancelled. */
declare function prompt(message: string, defaultText?: string): string | null;
/**
 * Show a selection dialog.
 * @param selections Strings to select from.
 * @param images Optional images representing each item.
 */
declare function select(message: string, selections: string[], images?: string[]): any; // VERIFY return type

// ---------------------------------------------------------------------------
// Console
// ---------------------------------------------------------------------------

/** Crude overlay showing the last 5 console messages. */
declare class Console {
    /** Logs with "DEBUG:" prefix, only if developer debugging is enabled. */
    debug(message: any): void;
    /** Logs with "ASSERT:" prefix, only if developer debugging is enabled. */
    assert(condition: boolean, message: any): void;
    /** Logs a message with no level identification. */
    log(message: any): void;
    /** Logs with "INFO:" prefix. */
    info(message: any): void;
    /** Logs with "WARNING:" prefix. */
    warn(message: any): void;
    /** Logs with "ERROR:" prefix. */
    error(message: any): void;
}

// ---------------------------------------------------------------------------
// World
// ---------------------------------------------------------------------------

declare class World {
    /** Actions for the world; shown on the left side of the screen. */
    actions: Action[];
    /** If true, the user can position the camera by touching the screen. True by default. */
    allowCameraPositioning: boolean;
    /** If true, objects can be moved (when picked) by dragging. */
    allowObjectMoving: boolean;
    /** Reflected light everywhere, filling shadows. */
    ambientLightIntensity: number;
    /** How foggy it is; usually a small value to give haze and depth. */
    fogDensity: number;
    /** Force of gravity. Zero in space; 9.8 (default) on earth. */
    gravity: number;
    /** The level in the game. Can be reset by the user. */
    level: number;
    /**
     * How the joysticks move the avatar:
     * explore (left joy moves and turns, right looks), confront (left moves, right looks and turns),
     * drive (left steers and brakes), fly (left pitches and rolls).
     */
    moveType: MoveType;
    /** All objects in the world (read-only). */
    readonly objects: Object3D[];
    /** If true, the world is saved every few seconds and restored on rerun. Set true for builder-style worlds. */
    persistent: boolean;
    /** Game score. The highest score is kept across games. */
    score: number;
    /** Color of the sky. Default is light blue. */
    skyColor: Color;
    /** Message shown at the top of the screen, right of the world name. */
    status: string;
    /** Color of the sunlight. */
    sunColor: Color;
    /** Vector pointing toward the sun. */
    sunDirection: Vector;
    /** Intensity of sunlight. */
    sunIntensity: number;
    /** World time in milliseconds; use for all time tests. Convert with Date. */
    time: number;

    /** Adds an object (or particle emitter) to the world; it then renders and takes part in physics. */
    addObject(o: Object3D | ParticleEmitter): void;
    /** Removes an object from the world. */
    removeObject(o: Object3D | ParticleEmitter): void;
    /** Plays a built-in sound file. */
    playSound(file: string, volume?: number, pitch?: number): void;
    /** Plays a sound file in a loop (background music). Only one song plays at a time. */
    playSong(file: string, volume?: number): void;
    /** Stops the current song. */
    stopPlayingSong(): void;
    /** Downloads and prepares a sound so the first play has no delay. */
    preloadSound(file: string): void;
    /** Downloads and prepares a texture so the first render has no delay. */
    preloadTexture(file: string): void;

    /** Called when a saved world is restored. Restore any state not stored with the world here. */
    onRestored: EventHandler | null;
}

// ---------------------------------------------------------------------------
// User
// ---------------------------------------------------------------------------

declare class User {
    /** The object used as the user's avatar. */
    avatar: Object3D;
}

// ---------------------------------------------------------------------------
// Action
// ---------------------------------------------------------------------------

/** A button shown on screen (left: world actions, right: avatar/selected object actions). */
declare class Action {
    constructor(name: string, onStart?: EventHandler);
    /** Background image for the button; if blank the theme button is used. */
    image: string;
    /** Name shown within the button. */
    name: string;
    /** Called when the button is first pressed. */
    onStart: EventHandler | null;
    /** Called when the button is no longer pressed. */
    onEnd: EventHandler | null;
    /** Called every half second while the button is pressed. */
    onRepeat: EventHandler | null;
}

// ---------------------------------------------------------------------------
// Color, Texture, Vector, Quaternion
// ---------------------------------------------------------------------------

declare class Color {
    /** Components from 0 to 1. */
    constructor(red: number, green: number, blue: number);
    /** Single integer in RGB format, e.g. 0xF0F0F0. */
    constructor(rgb: number);
    red: number;
    green: number;
    blue: number;
    /** Mixes a color into this color (average). */ // VERIFY return type
    mix(color: Color): void;
    /** Darkens this color toward black. */ // VERIFY return type
    darker(): void;
}

declare class Texture {
    /** @param name File within the world's folder (or a built-in texture name, no extension). */
    constructor(name: string);
    constructor(name: string, scaleX: number, scaleY: number, rotation: number);
    constructor(name: string, scaleX: number, scaleY: number, rotation: number, offsetX: number, offsetY: number);
    /** File name including extension (.png, .jpg). Built-in textures have no extension. */
    name: string;
    scaleX: number;
    scaleY: number;
    /** Rotation in degrees. */
    rotation: number;
    offsetX: number;
    offsetY: number;
    /** Rate of movement of the offset along X (flowing liquid effects). */
    velocityX: number;
    /** Rate of movement of the offset along Y. */
    velocityY: number;
    /** Rate of change of the rotation. */
    aMomentum: number;
    /** Sets scale, offset and rotation to map only a sub-region of the image (values normally in pixels). */
    subMap(totalWidth: number, totalHeight: number, width: number, height: number, left: number, right: number, rotation: number): void;
}

/** WebWorld uses a Z-up coordinate system. */
declare class Vector {
    constructor(x: number, y: number, z: number);
    x: number;
    y: number;
    z: number;
    // VERIFY: the mutating methods below are declared void; change to Vector if they return this.
    /** Adds another vector to this vector. */
    add(vector: Vector): void;
    /** Reverse-rotates by a quaternion (e.g. to see a vector relative to the avatar's orientation). */
    antirotate(quaternion: Quaternion): void;
    /** Creates a duplicate vector. */
    clone(): Vector;
    /** Sets this vector to the cross product with another vector. */
    cross(vector: Vector): void;
    /** Dot product with another vector. (The reference says it "sets the vector"; VERIFY.) */
    dot(vector: Vector): number;
    /** Reflection of this vector about a plane with the given normal. */
    getReflection(normal_vector: Vector): Vector;
    /** Magnitude of the vector. */
    length(): number;
    /** Scales to length one (stays zero if zero). */
    normalize(): void;
    /** Rotates by a quaternion. */
    rotate(quaternion: Quaternion): void;
    /** Scales by a factor. */
    scale(factor: number): void;
    /** Subtracts a vector from this vector. */
    subtract(vector: Vector): void;
}

declare class Quaternion {
    /** Rotation of angle degrees around the direction x, y, z. */
    constructor(angle: number, x: number, y: number, z: number);
    /** Rotations (degrees) around the x, y and z axes. */
    constructor(pitch: number, roll: number, yaw: number);
    // The reference lists pitch/roll/yaw both as read-only properties and as methods.
    // The intersection type lets both q.pitch and q.pitch(10) type-check.
    /** Property: rotation in degrees around x. Method: rotate this quaternion around x by d degrees. */
    readonly pitch: number & ((d: number) => void);
    /** Property: rotation in degrees around y. Method: rotate this quaternion around y by d degrees. */
    readonly roll: number & ((d: number) => void);
    /** Property: rotation in degrees around z. Method: rotate this quaternion around z by d degrees. */
    readonly yaw: number & ((d: number) => void);
    /** Rotates this quaternion around direction x,y,z by d degrees. */
    spin(d: number, x: number, y: number, z: number): void;
    /** Rotates this quaternion by another quaternion. */
    rotate(q: Quaternion): void;
    /** Performs the opposite rotation by another quaternion. */
    antirotate(q: Quaternion): void;
    /** Averages with q using weighting fraction f, giving a rotation between the two. */
    nlerp(q: Quaternion, f: number): Quaternion;
}

// ---------------------------------------------------------------------------
// 3D objects
// ---------------------------------------------------------------------------

/** Common base for all 3D objects (VERIFY the real class name). */
declare class Object3D {
    constructor();

    // ----- Static properties -----

    /** Actions for this object; shown on the right side of the screen when it is selected. */
    actions: Action[];
    /** [cutout start, cutout end], each 0.0 to 1.0; takes a slice out of the object. */
    cutout: [number, number];
    /** Carves out the middle of the object (0.0 to 1.0). */
    hollow: number;
    /** Name of the object; for child objects it names the body part for animations. */
    name: string;
    /** Position (Z is up). */
    position: Vector;
    /** Rotation. */
    rotation: Quaternion;
    /** Offset from the center where rotation is applied. */
    rotationPoint: Vector;
    /** Slides the top of the object: [x shear, y shear]. */
    shear: [number, number];
    /** Size of the object. */
    size: Vector;
    /** Pinches the top of the object in the X dimension. VERIFY name and type. */
    taperX: number;
    /** Pinches the top of the object in the Y dimension. VERIFY name and type. */
    taperY: number;
    /** Twists the object like taffy. */
    twist: number;
    /** Number of vertices used for circular shapes (default 16). */
    vertices: number;

    // Per-side properties. Sides: Top, Bottom, Side1-4, InsideTop, InsideBottom,
    // Inside1-4, Cutout1, Cutout2. The un-suffixed name sets all sides.
    /** Color (all sides). */
    color: Color;
    /** Color (Top). */
    colorTop: Color;
    /** Color (Bottom). */
    colorBottom: Color;
    /** Color (Side1). */
    colorSide1: Color;
    /** Color (Side2). */
    colorSide2: Color;
    /** Color (Side3). */
    colorSide3: Color;
    /** Color (Side4). */
    colorSide4: Color;
    /** Color (InsideTop). */
    colorInsideTop: Color;
    /** Color (InsideBottom). */
    colorInsideBottom: Color;
    /** Color (Inside1). */
    colorInside1: Color;
    /** Color (Inside2). */
    colorInside2: Color;
    /** Color (Inside3). */
    colorInside3: Color;
    /** Color (Inside4). */
    colorInside4: Color;
    /** Color (Cutout1). */
    colorCutout1: Color;
    /** Color (Cutout2). */
    colorCutout2: Color;
    /** Ignore shading and light the side as if lit internally (all sides). */
    fullBright: boolean;
    /** Ignore shading and light the side as if lit internally (Top). */
    fullBrightTop: boolean;
    /** Ignore shading and light the side as if lit internally (Bottom). */
    fullBrightBottom: boolean;
    /** Ignore shading and light the side as if lit internally (Side1). */
    fullBrightSide1: boolean;
    /** Ignore shading and light the side as if lit internally (Side2). */
    fullBrightSide2: boolean;
    /** Ignore shading and light the side as if lit internally (Side3). */
    fullBrightSide3: boolean;
    /** Ignore shading and light the side as if lit internally (Side4). */
    fullBrightSide4: boolean;
    /** Ignore shading and light the side as if lit internally (InsideTop). */
    fullBrightInsideTop: boolean;
    /** Ignore shading and light the side as if lit internally (InsideBottom). */
    fullBrightInsideBottom: boolean;
    /** Ignore shading and light the side as if lit internally (Inside1). */
    fullBrightInside1: boolean;
    /** Ignore shading and light the side as if lit internally (Inside2). */
    fullBrightInside2: boolean;
    /** Ignore shading and light the side as if lit internally (Inside3). */
    fullBrightInside3: boolean;
    /** Ignore shading and light the side as if lit internally (Inside4). */
    fullBrightInside4: boolean;
    /** Ignore shading and light the side as if lit internally (Cutout1). */
    fullBrightCutout1: boolean;
    /** Ignore shading and light the side as if lit internally (Cutout2). */
    fullBrightCutout2: boolean;
    /** How reflective/glossy (all sides). */
    shininess: number;
    /** How reflective/glossy (Top). */
    shininessTop: number;
    /** How reflective/glossy (Bottom). */
    shininessBottom: number;
    /** How reflective/glossy (Side1). */
    shininessSide1: number;
    /** How reflective/glossy (Side2). */
    shininessSide2: number;
    /** How reflective/glossy (Side3). */
    shininessSide3: number;
    /** How reflective/glossy (Side4). */
    shininessSide4: number;
    /** How reflective/glossy (InsideTop). */
    shininessInsideTop: number;
    /** How reflective/glossy (InsideBottom). */
    shininessInsideBottom: number;
    /** How reflective/glossy (Inside1). */
    shininessInside1: number;
    /** How reflective/glossy (Inside2). */
    shininessInside2: number;
    /** How reflective/glossy (Inside3). */
    shininessInside3: number;
    /** How reflective/glossy (Inside4). */
    shininessInside4: number;
    /** How reflective/glossy (Cutout1). */
    shininessCutout1: number;
    /** How reflective/glossy (Cutout2). */
    shininessCutout2: number;
    /** Texture (all sides). */
    texture: Texture;
    /** Texture (Top). */
    textureTop: Texture;
    /** Texture (Bottom). */
    textureBottom: Texture;
    /** Texture (Side1). */
    textureSide1: Texture;
    /** Texture (Side2). */
    textureSide2: Texture;
    /** Texture (Side3). */
    textureSide3: Texture;
    /** Texture (Side4). */
    textureSide4: Texture;
    /** Texture (InsideTop). */
    textureInsideTop: Texture;
    /** Texture (InsideBottom). */
    textureInsideBottom: Texture;
    /** Texture (Inside1). */
    textureInside1: Texture;
    /** Texture (Inside2). */
    textureInside2: Texture;
    /** Texture (Inside3). */
    textureInside3: Texture;
    /** Texture (Inside4). */
    textureInside4: Texture;
    /** Texture (Cutout1). */
    textureCutout1: Texture;
    /** Texture (Cutout2). */
    textureCutout2: Texture;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (all sides). */
    transparency: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Top). */
    transparencyTop: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Bottom). */
    transparencyBottom: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Side1). */
    transparencySide1: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Side2). */
    transparencySide2: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Side3). */
    transparencySide3: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Side4). */
    transparencySide4: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (InsideTop). */
    transparencyInsideTop: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (InsideBottom). */
    transparencyInsideBottom: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Inside1). */
    transparencyInside1: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Inside2). */
    transparencyInside2: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Inside3). */
    transparencyInside3: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Inside4). */
    transparencyInside4: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Cutout1). */
    transparencyCutout1: number;
    /** Transparency (0.0 = opaque, 1.0 = fully transparent and not drawn) (Cutout2). */
    transparencyCutout2: number;

    // ----- Hierarchy -----

    /** Children of this object. Prefer addChild/removeChild. */
    children: Object3D[];
    /** Parent object (read-only). Use addChild/removeChild on the parent. */
    readonly parent: Object3D | null;

    // ----- Dynamic properties -----

    /** Animations associated with this object. VERIFY type. */
    animations: string[];
    /** Rate of spin about x, y, z. */
    angularVelocity: Vector;
    /** One corner of a box limiting the object's range of movement. */
    startPosition: Vector;
    /** Opposite corner of the box limiting the object's range of movement. */
    stopPosition: Vector;
    /** Minimal velocity. VERIFY type. */
    startVelocity: Vector;
    /** Maximum velocity. VERIFY type. */
    stopVelocity: Vector;
    /** Acceleration vector. */
    thrust: Vector;
    /** Milliseconds until onTimer fires. Reset it in onTimer for repeating events. */
    timer: number;
    /** Increases angular momentum (spinning). */
    torque: Vector;
    /** Velocity of the object. */
    velocity: Vector;

    // ----- Physics -----

    /** Density; 1 is the density of water. */
    density: number;
    /** Springiness; 1 is perfectly elastic. */
    elasticity: number;
    /** Freedom to move along [x, y, z]. */
    freedomMove: [boolean, boolean, boolean];
    /** Freedom to rotate around [x, y, z]. VERIFY element type. */
    freedomRotate: [boolean, boolean, boolean];
    /** Resistance to movement of adjacent objects (multiplied with the other object's friction). Default 1. */
    friction: number;
    /** If true, renders but has no physical interaction. */
    phantom: boolean;
    /** If true, moves and bounces according to physics. */
    physical: boolean;
    /** If true, physical objects cannot penetrate it. */
    solid: boolean;

    // ----- Sound (built-in or preloaded sounds only) -----

    /** Sound made when a physical object makes contact. */
    impactSound: string;
    /** Sound made when a physical object slides against this one. */
    slidingSound: string;
    /** Sound made constantly by the object. */
    sound: string;

    // ----- Rendering -----

    /** If true, the object never moves or changes, allowing optimized rendering. */
    fixed: boolean;
    /** If true, the object never moves separately from its parent. */
    gluedToParent: boolean;
    /** If true (default), the camera can pass through the object. */
    penetratable: boolean;
    /** If true, the object can be touched/clicked and events fire; otherwise they fire on the parent. */
    pickable: boolean;

    // ----- Events -----

    /** Pressed and released with no movement. `side` type is a VERIFY. */
    onTouch: ((user: User, side: string, x: number, y: number) => void) | null;
    /** Object first pressed (mouse-down). */
    onPress: EventHandler | null;
    /** Pressed object is dragged. */
    onDrag: EventHandler | null;
    /** Object no longer pressed (mouse-up). */
    onRelease: EventHandler | null;
    /** Collided with another object. */
    onCollide: ((object: Object3D) => void) | null;
    /** Continues sliding along another object after a collision. */
    onSlide: ((object: Object3D) => void) | null;
    /** Stopped sliding along another object. */
    onStopSlide: ((object: Object3D) => void) | null;
    /** Fires when the timer property times out. */
    onTimer: EventHandler | null;

    // ----- Methods -----

    /** Adds a behavior to this object and its children. */
    addBehavior(behavior: Behavior): void;
    /** Adds o as a child; sets o's parent to this object. */
    addChild(o: Object3D): void;
    /** Copies this object, including children. */
    clone(): this;
    /** Returns the named child or null. */
    getChild(name: string): Object3D | null;
    /** Returns the named descendant (child, grandchild, ...) or null. */
    getDescendant(name: string): Object3D | null;
    /** True if there is a descendant with the given name. */
    isDescendant(name: string): boolean;
    /** Removes child o; o's parent becomes null. */
    removeChild(o: Object3D): void;
    /**
     * Starts the named animation (see Built-Ins page).
     * @param rate Speed; 1 is normal.
     * @param range Amount of movement; 1 is normal.
     */
    startAnimation(animation: string, rate?: number, range?: number): void;
    /** Stops any running animation. */
    stopAnimation(): void;
    /** Call after changes to a displayed object that modify its shape. */
    updateRendering(): void;
}

declare class Box extends Object3D {
    constructor();
}

declare class Cylinder extends Object3D {
    constructor();
}

declare class Sphere extends Object3D {
    constructor();
}

declare class Torus extends Object3D {
    constructor();
}

declare class Plant extends Object3D {
    constructor();
}

/** A Box with special rendering giving limited visibility when viewed from inside (murky water). */
declare class Translucency extends Box {
    constructor();
    /** Color and transparency of the filter placed in front of the camera. VERIFY type. */
    translucencyMask: Color;
}

/** A flat, cylindrical or spherical shape with a topography. Good for land. */
declare class Mesh extends Object3D {
    constructor();
    /** Grid size: [rows, columns]. */
    meshSize: [number, number];
    // (The reference does not mark getMeshValue as Mesh-only, but it is declared here with the other mesh methods.)
    /** Creates a cone. height is relative to the current value; baseSize is the width at the base. */
    createCone(row: number, column: number, height: number, baseSize: number): void;
    /** Creates a hill. height is relative to the current value; baseSize is the width at the base. */
    createHill(row: number, column: number, height: number, baseSize: number): void;
    /** Creates a mound (negative height forms a bowl). */
    createMound(row: number, column: number, height: number, baseSize: number): void;
    /** Creates a peak (artificial mountains). */
    createPeak(row: number, column: number, height: number, baseSize: number): void;
    /** Creates a plateau: a raised area with steep slopes. */
    createPlateau(row: number, column: number, height: number, baseSize: number): void;
    /** Given a position, returns the nearest cell as a Vector. */
    getMeshCell(position: Vector): Vector;
    /** Value of the mesh at an exact position (weighted average of nearest points). Use to place objects on the surface. */
    getMeshValue(position: Vector): number;
    /** Value of a single point in the mesh. */
    getMeshPoint(row: number, column: number): number;
    /** Sets a single point. height is the factor the mesh height varies from the original (0.5 is default). */
    setMeshPoint(row: number, column: number, height: number): void;
}

// ---------------------------------------------------------------------------
// Behavior
// ---------------------------------------------------------------------------

/**
 * Adds animation, sound and other actions to a 3D object and its children.
 * Subclass and implement the methods to create a custom behavior.
 */
declare class Behavior {
    constructor();
    /** The object this behavior has been added to. */
    owner: Object3D;
    /** Milliseconds until the timer pops and timerEvent is called. */
    timer: number;

    collideEvent(object: Object3D, nearobject: Object3D): void;
    dragEvent(object: Object3D): void;
    pressEvent(object: Object3D): void;
    /** Pushed along by the user (currently only on an avatar). */
    pushEvent(vector: Vector): void;
    releaseEvent(object: Object3D): void;
    slideEvent(object: Object3D, nearobject: Object3D): void;
    stopSlideEvent(object: Object3D, nearobject: Object3D): void;
    timerEvent(object: Object3D): void;
    touchEvent(object: Object3D): void;
}

/** Built-in behavior; the full list of built-ins is on the Built-Ins page (not included in the reference). */
declare class HumanMotionBehavior extends Behavior {
    constructor();
}

// ---------------------------------------------------------------------------
// ParticleEmitter
// ---------------------------------------------------------------------------

/** Simulates smoke, explosions, flames, rain, etc. by animating particle images. */
declare class ParticleEmitter {
    constructor();
    /** Location of the emitter. */
    position: Vector;
    /** Rotation of the emitter. Particles emit upwards by default. */
    rotation: Quaternion;
    /** Total number of particles. */
    particleCount: number;
    /** Seconds before a particle is destroyed or recycled. */
    particleLifetime: number;
    /** Particles (re)emitted per second; zero emits all at once (explosion). */
    particleRate: number;
    /** Randomness added to the emit rate. */
    particleRateRandom: number;
    /** Slows particles as they fly. */
    particleDrag: number;
    /** Gravity on particles; negative values rise, like smoke. */
    particleGravity: number;
    /** Randomness added to the initial position. VERIFY type (likely Vector). */
    particlePositionRandom: Vector;
    /** Image used for the particles. VERIFY type (file name?). */
    particleImage: string;
    /** Initial transparency of the particle image. */
    particleTransparency: number;
    /** Rate of change of particle transparency. */
    particleFadeRate: number;
    /** Initial particle size. */
    particleSize: number;
    /** How fast particle size grows with age. */
    particleGrowthRate: number;
    /** Speed and direction of particles leaving the emitter. */
    particleVelocity: Vector;
    /** Randomness added to velocity to "spray" the particles. VERIFY type. */
    particleVelocityRandom: Vector;
    /** Starts emitting particles. Must be called for any particles to be seen. */
    startAnimation(): void;
    /** Stops emitting particles. */
    stopAnimation(): void;
}