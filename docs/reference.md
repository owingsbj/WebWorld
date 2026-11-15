# WebWorld Reference

This page describes the JavaScript objects available when building avatar and world scripts.

## Global

These variables and functions are available globally.

#### Variables

- **avatar** -- this is a 3D object representing the user.  It is set in the avatar script and is available in the world script.   You can also set the avatar to a different object in the world script initialization and it will be used as the avatar.
- **console** -- an instance of the Console object (see below).
- **user** -- an instance of the user object for the user currently controlling the avatar.
- **world** -- this is an instance of the World object as described below.

#### Functions

Some functions are available globally:

- **alert(*message*)** -- pop up a message box.  This can be useful for showing a message that the user must see.  Play will stop while the message is displayed.
- **confirm(*message*)** -- displays a message with OK and Cancel buttons.  Returns true if OK is pressed.  Otherwise returns false.
- **prompt(*message*, *defaultText*)** -- displays a message and in input box initially filled in with defaultText.  Returns the entered text or null if cancelled.
- **select(*message*, *selections*, *images*)** -- displays a selection dialog.  *selections* is an array of items (strings) to select from.  *images* are an option array of images to show to represent each item.
)

## Console

The console object provides access to WebWorld's console.  At this time the console is very crude and consists of an overlay on the bottom of the screen of the last 5 console messages.  The messages scroll off the screen and are gone -- there is no way currently to see any console history after it scrolls off the screen.

#### Methods

- **debug(*message*)** -- adds a message to the console if developer debugging is enabled (see the settings).  The message appears with the identifier of DEBUG: in front of it.  You should use this method to log messages for debug purposes.
- **assert(*condition*, *message*)** -- a variant of debug that firsts tests a condition then if true logs the message.  The message appears with the identifier of ASSERT: in front of it.  The message will only appear if developer debugging is enabled (see the settings).
- **log(*message*)** -- adds a message to the console log.   The message appears with no identification of level.
- **info(*message*)** -- logs a message with the identifier of INFO: in front of it.
- **warn(*message*)** -- logs a message with the identifier of WARNING: in front of it.
- **error(*message*)** -- logs a message with the identifer of ERROR: in front of it.

## World

The world object allows you to set general aspects of the web world.  

You never need to create this object, it already exists in the global variable *world*.

#### Properties

These properties are defined:

- **actions** -- an array of Action objects for actions that can be performed on the world.  These will show up on the left side of the screen.
- **allowCameraPositioning** -- if true, the camera can be positioned by the user by touching the screen.  True by default.
- **allowObjectMoving** -- if true, then objects will be able to be moved (when picked) by dragging.
- **ambientLIghtIntensity** -- that's the reflected light that is everywhere, filling shadows so that they aren't completely black.  If it's a cloudy day there's usually more ambient light.  In space there is very little ambient light.
- **fogDensity** -- how foggy is it?  This is usually set to a small amount to add haze and give the appearance of depth.
- **gravity** -- the amount of force of gravity.  In space it would be zero, but on earth it is 9.8 (the default).
- **level** -- the level in the game.  This can be reset by the user to return to a beginner-like state.
- **moveType** -- defines how joystick controls move the avatar in the world.  There are four choices:<ol>
<li>explore -- this has the left joy both move and turn the avatar  The right joy look around but doesn't turn.
<li>confront -- this has the left joy move and the right joy look around *and* turn the avatar.
<li>drive -- this has the left joy steer and brake.  The right joy looks around but doesn't turn.
<li>fly -- the left joy pitch and roll (tilt and lean).  The right joy looks around but doesn't turn.
</ol>
- **objects** -- a read-only property that returns an array of all the objects in the world.
- **persistent** -- if set to true, the world will be saved (every few seconds) allowing the user to restore to the last state when rerunning.  You should set this to true for builder-style worlds.
- **score** -- if it's a game, you can set the score here.  Highest score is maintained across games.  This lets the user try to beat their score from previous games.
- **skyColor** -- this is the color of the sky (duh!)  In space this is black of course.  The default is a light blue.
- **status** -- a message that displays at the top of the screen, to the right of the world name.
- **sunColor** -- this is the color of the light emitted from the sun.  It's usually white, but you can tint it to give the world an alien glow.
- **sunDirection** -- this is the direction of the light coming from the sun.  This is expressed as a vector with x, y, z coordinates pointing to the sun.
- **sunIntensity** -- this is a measure of the intensity of the light coming from the sun.  Turn it down if it's a foggy day or you're on a planet far away.
- **time** -- the time in the world in milliseconds.  This may or may not match real time.  Any test of time should use this value.  Use the JavaScript Date object to convert this to readable dates and times.

#### Methods

There are also these methods on the world object:

- **addObject(*o*)** -- adds an object to the world.  The object ill start rendering and partaking in physics within the world after being added.
- **removeObject(*o*)** -- removes an object from the world.  The object will stop rendering and partaking in physics with the world after being removed.
- **playSound(*file*, *volume*, *pitch*)** -- plays a sound stored in a sound file.  The volume and pitch can also be specified.  This is good for game sounds like winning awards or sounds that are supposed to come from the player.  **Limitation**:  This can only be a built-in sound right now.
- **playSong(*file*, *volume*)** -- this will play a sound file in a looping fashion.   This is useful for background "mood" music.  Only one song will play at a time.
- **stopPlayingSong()** -- this will stop the currently playing song.
- **preloadSound(*file*)** -- this will download and prepare a sound file for playing before the time it needs to be played.  This can avoid a delay when playing the sound the first time in the world.
- **preloadTexture(*file*)** -- this will download and prepare a texture file for rendering before the time it needs to be rendered.  This can avoid a delay in rendering the texture the first time it appears in the world.

#### Events

There is one event on the world object:

- **onRestored** -- this event is called when a saved world is restored.  Any state that would not be stored as part of the world should be restored during this event.

## User

The user object gives you access to the avatar being used by the player.  Since an avatar is chosen outside of the world this is initially set to the users chosen avatar, but it is possible to change the player's avatar to suit the world.

You don't need to create the user object.  It is already available in a global variable called *user*.

#### Properties

These properties are defined:

- **avatar** -- the object being used for the avatar of the user.

There are no methods or events on the user object.

## Action

There is a predefined class to use for creating actions.  Actions are shown as buttons on the screen, on either the left or right side.  The actions
that appear on the left side are called *world* actions and are specified by the **actions** array on the World object.  The actions on the right are avatar or
selected objects and are defined by the **actions** array on the selected 3D object (or avatar if no object is selected).

An action can be constructed like so:

    var action = new Action("jump");
    
It can also be constructed and provided the event to call when the action button is pressed like so:

    var action = new Action("jump", jumpFunction);

#### Properties

The following properties exist on an action:

- **image** -- a background image to use as the button.  If blank the webworld theme button will be used.
- **name** -- the name of the action.  This appears within the button shown on the screen.

#### Events

The following event properties can be set to functions and which will be called:

- **onStart** -- called when the button is first pressed.
- **onEnd** -- called when the button is no longer pressed.
- **onRepeat** -- called very half second repeatedly while the button is pressed.  This can be used to perform an action such as a move or resize.

## Color

Color object is used to describe a color in several of the methods on world and 3D objects.

There are two ways to contruct a Color object.  The first is with three values (from 0 to 1) defining the red, green and blue components of the color:

    var c = new Color(0.5, 1.0, 0.25)

The second is with a single integer value in RGB format.

    var c = new Color(0xF0F0F0)

#### Properties

There are three properties:

- **red**
- **green**
- **blue**

#### Methods

- **mix(color)** - this will mix a color into this color (taking the average).
- **darker()** - this will darken the color (towards black).

## Texture

A Texture object describes an image used for texturing.  It also defines the scaling, rotation, and any dynamic changes to the texture.

You can construct a texture with the file name:

    var t = new Texture("happy.png");

The file name should be a file within the folder of your world.

You can also construct with the scale and rotation:

    var t = new Texture("brick.png", 0.5, 0.5, 90);

And, thirdly, with scale, rotation and offsets:

    var t = new Texture("brick.png", 0.5, 0.5, 90, 0.25, 0.25);

#### Properties

There are only properties for a texture:

- **name** -- the file name of the texture, including the extension (.png, .jpg).  For built-in textures, the name does not have an extension.
- **scaleX** -- scaling of the texture on the X axis.
- **scaleY** -- scaling of the texture on the Y axis.
- **rotation** -- the rotation of the texture in degrees.
- **offsetX** -- the offset of the texture on the X axis.
- **offsetY** -- the offset of the texture on the Y axis.
- **velocityX** -- rate of movement of the offset along the X axis.
- **velocityY** -- rate of movement of the offset along the Y axis.
- **aMomentum** -- rate of change of the rotation.

The velocity and aMomentum properties can be used to move a texture to give the illusion of a liquid flowing or swirling.

#### Methods

A method is provided to more easily define the texture if it is a submapped:

- **subMap(*totalWidth*, *totalHeight*, *width*, *height*, *left*, *right*, *rotation*)** -- this sets the scale, offset and rotation correctly to map the texture to only a pixel submapping.  The values are normally in pixels, but any scale can be used.

## Vector

A Vector is used to represent dimensions and sizes on many properties and methods.  It can be constructed like so:

    var v = new Vector(x, y, z);
    
#### Properties

A Vector has three properties:

- **x** -- the value along the x dimension (left and right)
- **y** -- the value along the y dimension (forward and back)
- **z** -- the value along the z dimension (up and down)

#### Methods

Vector also has quite a few helper methods.  You'll probably not use most of them but it's good to know they exist:

- **add(vector)** -- adds another vector to this vector.
- **antirotate(quaternion)** -- reverse rotates the vector given a rotation quaternion.  This is useful, for instance, to see a vector relative to the avatar's current orientation.
- **clone()** -- creates a duplicate vector.
- **cross(vector)** -- sets the vector to the cross product with another vector.
- **dot(vector)** -- sets the vector to the dot product with another vector.
- **getReflection(normal_vector)** -- returns a vector that is the reflection of this vector relative to a plane whose normal vector is provided.
- **length()** -- returns the magnitude of the vector.
- **normalize()** -- scales the vector so its length is one (or keep it's length zero if it is zero).
- **rotate(quaternion)** -- rotates a vector given a rotation quaternion.
- **scale(factor)** -- scales the vector by the factor provided.
- **subtract(vector)** -- subtracts a vector from this vector.

## Quarternion

A Quaternion object describes the rotation of an object.  (Although a vector could be used there are issues with arithmetic that make a quaternion better.)  A quaternion is constructed like so:

    var q = new Quaternion(angle, x, y, z)

where x, y, z is a direction and angle is an angle in degrees for the rotation in that direction.  It can also be constructed:

    var q = new Quaternion(pitch, roll, yaw)

where pitch, roll, and yaw are the rotations around the x, y and z axis respectively.

#### Properties

The internal state of a quarternion is hidden, so there are no real properties.  However, these values
are computed and presented as read-only properties:

- **pitch** -- a read-only property returning the rotation in degrees around the x axis.
- **roll** -- a read-only property returning the rotation in degrees around the y axis.
- **yaw** -- a read-only property returning the rotation in degrees around the z axis.

#### Methods

- **spin(d, x, y, z)** -- rotates this quaternion around the direction vector represented by x,y,z by d degrees.
- **pitch(d)** -- rotate this quarternion around the x axis by d degrees.
- **roll(d)** -- rotate this quarternion around the y axis by d degrees.
- **yaw(d)** -- rotate this quaternion around the z axis by d degrees.
- **rotate(q)** -- rotate this quaternion by another quaternion q.
- **antirotate(q)** -- performs the opposite rotation on this quaternion by another quaternion q.
- **nlerp(q, f)** -- averages this quaternion with another quaternion q with weighting fraction f, giving a new quaternion that represents a rotation between these two quaternions.


## 3D Objects

There are predefined classes for the different types of 3D objects that can be created and added to the world.  These object may seem simple but they can be shaped and combined in many ways to make more complex structures:

- **Box** -- a block shaped object.  This can also be tapered to form prisms and pyramids.
- **Cylinder** -- a cylindrical object.  This can also be tapered to form a cone.
- **Sphere** -- a ball shaped object.  This can be distorted to form eggs and other interesting blob-like shapes.
- **Torus** -- a tire or doughnut shaped object.
- **Mesh** -- a flat, cylindrical, or spherical shape where one side of the shape can have a topography as specified by an array of values.  Meshes are good for creating the land of a world or complex shaped objects.
- **Translucency** -- this is really a Box object, with special rendering that gives limited visibility when viewed inside of.  It can be useful for merky water effects.
- **Plant** -- a folded surface that can be given a texture to simulate different types of plants.

These objects have common properties, methods and events that can be used to tailor the look and behavior.   The following sections describe the properties, methods and events.

Properties are defined fields on objects that can change the look and behavior of objects.  Most of the properties are common across all types of objects.  A few are unique to certain objects. The lists below describe the properties.

#### Static Properties

Static properties are the first properties you need to know to properly shape and place an object.

- **actions** -- an array of Action objects for actions that can be performed on the object.  These will show up on the right side of the screen when the object is selected.
- **color*Side*** -- the Color object describing the color of one side of the object.  Note that a textured side is also colored will have the texture tinted by the color.
- **cutout** -- is an array of two values [*cutout start*, *cutout end*].  A cutout takes a "slice" out of the object.  For instance, a cutout of a cylinder can make it look like a block of cheese with a wedge missing.  The values range from 0.0 to 1.0, with [0.0, 0.5] cutting off half of the object.
- **fullBright*Side*** -- ignores shading and illuminates the side as if it was lit internally.
- **hollow** -- carves out the middle of the object.  You might not see a hollowed out middle unless the object also has a cutout.  This is a single value in the range 0.0 to 1.0, with 0.5 hollowing out half of the middle.
- **meshSize** -- applies to Mesh objects.  This defines the grid size for the mesh as an array of [rows, columns].
- **name** -- a name for the object.  This has special meaning for child objects for animations as it names the body part for the animation.
- **position** -- the position of the object.  This is a vector of the three axis x, y and z.  Note that z axis is up/down in WebWorld.
- **rotation** -- the rotation of the object.  This is a quarternion from which the rotation relative to any axis can be derived.
- **rotationPoint** -- an offset from the center of the object where rotation is applied.  If default then the object rotates around its center.  Setting this is useful for animations (which generally only rotate appendages and assume the rotation point will make it look right).
- **shear** -- this slides the top of the object, making the sides seem sloped.  An array of two values is provide [*x shear*, *y shear*].
- **shininess*Side*** -- how reflective or glossy the surface.
- **size** -- the size of the object.
- **taper** -- this "pinches" the top of the object making it smaller than the bottom.  There are taper properties for both x and y dimensions.
- **texture*Side*** -- a Texture object describing the texture of a side.
- **twist** -- this twists the object causing it to appear like a twisty piece of taffy.
- **translucencyMask** -- applies only to Translucency objects.  Defines the color and transparency of a filter placed in front of the camera to give the illusion of being in a murky substance.
- **transparency*Side*** -- how transparent a side is.  0.0 is no transparency.  1.0 is full transparency.  A fully transparent side is not drawn, which can be a trick to speeding up rendering of objects with completely unseen surfaces.
- **vertices** -- for objects that have a circular shape, such as cylnders and spheres, this defines the number of vertices used to draw the circle.  Fewer vertices can turn a circle into an obvious polygon shape, like a pentagon, hexagon or octagon.  By default, circles are draw with 16 vertices.

On the properties above, the *Side* can be one of the following: Top, Bottom, Side1, Side2, Side3, Side4, InsideTop, InsideBottom, Inside1, Inside2, Inside3, Inside4, Cutout1, Cutout2.   The *Side* can also be empty, which indicates that all sides should be set to the property.

#### Hierarchy Properties

Objects can be arranged in a hierarchy, with a parent and children.  When associated in this way, the position and rotation of a parent is reflected in the children.  The following properties allow for hierarchies to be made:

- **children** -- the children of this object.  Note that it is better to use addChild/removeChild methods to add and remove children.
- **parent** -- the identifier of a parent object.  This is a read-only property.  Use addChild/removeChild on the parent object to associate children with a parent object.

#### Dynamic Properties

These properties cause an object to move.  They are good for objects mimicking machinery or natural objects that have motion.

- **animations** -- a list of animations associated with this object.
- **angularVelocity** -- the rate of spin of the object in x y and z dimensions.  Setting this to a value will cause the object to start spinning.
- **startPosition** -- defines one corner of a box that limits the range of movement of the object.
- **stopPosition** -- defines the second corner of a box that limits the ranger of movement of the object.
- **startVelocity** -- defines a minimal velocity of an object.
- **stopVelocity** -- defines a maximum velocity of an object.
- **thrust** -- this causes acceleration of an object.  The thrust is a vector of three values pointing to the direction where acceleration should occur.
- **timer** -- this sets a timer on the object that fires the *onTimer* event when it times out.  The timer is in milliseconds.
- **torque** -- this increases angular momentum of an object (spinning).
- **velocity** -- the speed at which the object is moving.  Expressed as a vector of *x velocity*, *y velocity*, *z velocity*.

#### Physics Properties

These properties describe how an object obeys the laws of physics, or influences other physical objects:

- **density** -- how dense an object is.  The default density is 1, which is considered the density of water.  Physical objects will either float or sink if they are within non-solid objects with lower or higher densities.  Gravity also has an impact on density, with objects becoming denser lower in the object.
- **elasticity** -- how springy an object is.  A ball with perfect elasticity would have a value of 1.  Usually you'd want to keep below 1 or the ball will bounce forever.
- **freedomMove** -- defines how free the object is allowed to move.  This is an array of three boolean values [*freedom X*, *freedom Y*, *freedom Z*].
- **freedomRotate** -- defines how free an object is to rotate.  This is an array of three values, describing the freedom to rotate around the x, y and z axis.
- **friction** -- the amount that an object will resist movement of other objects next to it.  The friction of both objects multiply each other to determine the actual friction applied.  By default the friction is 1, which will resist slippage on all most slightly sloping surfaces.
- **phantom** -- if true, the object is completely void of any physical effect.  It renders but it does not interact with physical objects in any way.
- **physical** -- if true, the object will move, bounce, etc, according to the laws of physics.  If false, the object will remain stationary (or move according to current angular momentum and velocity) and ignore the laws of physics.
- **solid** -- if true, the object will not allow physical object to penetrate it.   If false, the object is similar to a liquid or gas and will allow physical objects to travel through it (with viscosity and buoyancy effects).

#### Sound Properties

These properties cause the object to emit sounds.     **Limitation**:  These properties can only use with sounds that are either built-in or preloaded (see world.preloadSound).

- **impactSound** -- a sound made when a physical object makes contact with this object.  Splashes and crashes can be done using this property.
- **slidingSound** -- a sound made when a physical object slides against this object.   Sounds of rolling over a grate or sliding against a wall can be done using this property.
- **sound** -- a sound made constantly by the object.  This is useful for making machinery sounds such as the hum of engines.  And, yes, the sound will change in frequency if emitted by an object in motion (doppler shift).

#### Rendering Properties

These special properties assist the rendering of the object:

- **actions** -- a list of Action objects for the actions that can be performed on the object.  These will show up the right side of the screen when the object is picked.
- **fixed** -- if true, this means that the object never, ever, moves, resizes, or otherwise changes.  By knowing this, the rendering of the object can be optimized and combined with other fixed objects to be drawn quickly.
- **gluedToParent** -- if true, this indicates that the object will never move separately from its parent.  This speeds up rendering by allowing the parent object and the glued child object to be rendered together.
- **penetratable** -- if true, this means that the camera can penetrate the object.  By default this is true, which means that the camera will move right through an object when panning or zooming.  This is usually what you want, except for special objects like the ground, where the feeling of reality can really be broken if the camera can penetrate.  Other objects you might want to consider restricting camera penetration would be walls of a secret structures.
- **pickable** -- if true, an object can be touched or clicked upon and events will be fired.  If false, the events will fire upon the parent object (if any).

#### Methods

These methods exist on 3D objects:

- **addBehavior(*behavior*)** -- adds the behavior *behavior* to the object.  A behavior can respond to actions on its host object and child objects of the host object, animating the objects and performing other effects.   See Behavior below.
- **addChild(*o*)** -- adds a object *o* to this object's list of children.  This has the side-effect of setting the parent of *o* to this object.
- **clone()** -- creates a copy of this object.  If the object has children, those children are cloned as well.
- **createCone(row, column, height, baseSize)** -- applies to Mesh objects.  Creates a cone on the mesh.  *height* is the relative change of the height from its current value.  *baseSize* is how wide the cone is at its base.
- **createHill(row, column, height, baseSize)** -- applies to Mesh objects.  Creates a hill on the mesh.  Good for forming artificial landscapes.  *height* is the relative change of the height from its current value.  *baseSize* is how wide the hill is at its base.
- **createMound(row, column, height, baseSize)** -- applies to Mesh objects.  Creates a mound on the mesh.  Good for forming bowls (using a negative height).  *height* is the relative change of the height from its current value.  *baseSize* is how wide mound the mound is at its base.
- **createPeak(row, column, height, baseSize)** -- applies to Mesh objects.  Creates a peak on the mesh.  Good for forming artificial mountains on landscapes.  *height* is the relative change of the height from its current value.  *baseSize* is how wide the peak is at its base.
- **createPlateau(row, column, height, baseSize)** -- applies to Mesh objects.  Creates a pleatea on the mesh.  This is a raised area with steep slopes.  *height* is the relative change of the height from its current value.  *baseSize* is how wide the plateau is at its base.
- **getChild(*name*)** -- returns the named child or null.
- **getDescendant(*name*)** -- returns the named descendant (child, grandchild, etc) or null if no descendant of that name is found.
- **getMeshCell(position)** -- applies to Mesh objects.  Given a position it returns the nearest cell.  Both position and cell are Vectors.
- **getMeshValue(position)** --  given a position in space this returns the value of the mesh at that exact position.  This is a weighted average of the nearest points.  Use this method to place objects on the surface of a mesh.
- **getMeshPoint(row, column)** -- applies to Mesh objects.  Gets the value of a single point in the mesh.
- **isDescendant(*name*)** -- returns true if there is a descendant with the given name. 
- **removeChild(*o*)** -- removes object *o* from this object's list of children.  The parent of object *o* is also set to null.
- **setMeshPoint(row, column, height)** -- applies to Mesh objects.  Sets the value of a single point on the mesh.  The *row* and *column* define the point on the mesh (from zero to mesh size).  The *height* value defines the factor the mesh height varies from the original height of the mesh, with 0.5 being the default.
- **startAnimation(*animation*, *rate*, *range*)** -- starts running the named animation.  See the [Built-Ins](builtins.md) page for a list of available animations.  *rate* is how fast the animation runs, with 1 being normal speed.  *range* is how much movement the animation makes, with 1 being normal.  Note that animations don't actually move the object, they only move the appearance of the object  Some animations loop and some do not.  Some animations involve multiple objects.  In that case the objects are all child objects of the parent object that has the associated animation, and the child objects will have names that indicate their "part" with respect to animation.
- **stopAnimation()** -- stops any currently running animation on the object.
- **updateRendering()** -- this method should be called after changes to a displayed object that modify its shape.

#### Events

Objects in the world can made to react when touched or when coming into contact with another object.  This is done by defining callback functions to run when certain events happen on the object.  These functions are then set in special properties called event  properties.  Here are the event properties:

- **onTouch(user, side, x, y)** -- triggered when an object is pressed and then released with no movement.
- **onPress** -- triggered when the object is first pressed on (or mouse-down).
- **onDrag** -- triggered when an object that is pressed is dragged (by moving finger or mouse).
- **onRelease** -- triggered when an object is no longer pressed on (or mouse-up).
- **onCollide(object)** -- triggered when an object collides with another objects.
- **onSlide(object)** -- triggered after an object has collided with another object and continues to slide along.
- **onStopSlide(object)** -- triggered once an object has stopped sliding along another object, either because it has become still or because it is no longer in contact with the other object.
- **onTimer** -- triggered according to a timer on the object.  Set the timer using the *timer* property.  This is useful for performing a repeating action on the object.  The timer needs to be reset at the end of the event for repeating onTimer events.

## Behavior

A behavior adds animation and potentially sound and other actions to a 3D object and its children.  They can can quickly make the objects "come alive" and mimic living things.  There are several built-in behaviors.  See the [Built-Ins](builtins.md) page for a list.  For example, to construct the HumanMotionBehavior:

```
var behavior = new HumanMotionBehavior();
```

The behavior is then set on an object using the addBehavior method:

```
avatar.addBehavior(behavior);
```

### *Advanced:*

A custom behavior can be created by subclassing Behavior and implementing the needed methods below.  Follow the documentation on Rhino JavaScript engine to understand how to subclass a Java class using JavaScript.

#### Properties

- **owner** -- the object that this behavior has been added to.
- **timer** -- each behavior has a timer.  This is the current value in milliseconds until the timer "pops" and the timerEvent function is called.

#### Methods

The methods of the Behavior object shouldn't be called.  They are meant to be implemented by subclasses.  They provide the logic of the behavior.   They are similar to the events on a 3D object.  For all of these methods the object parameter is the object that the behavior was added to.

- **collideEvent(*object*, *nearobject*)** -- the object has collided with another object
- **dragEvent(*object*)** -- the object is being dragged along by a user
- **pressEvent(*object*)** -- the user has pressed the object
- **pushEvent(*vector*)** -- the object is pushed along by the user.  This currently can only happen on an avatar.
- **releaseEvent(*object*)** -- the pressed object is released.
- **slideEvent(*object*, *nearobject)** -- the object is sliding along on another object
- **stopSlideEvent(*object*, *nearobject*)** -- the object stopped sliding on another object
- **timerEvent(*object*)** -- the timer on the behavior has timed down.
- **touchEvent(*object*)** -- a press and release on the object is performed.

## ParticleEmitter

A particle emitter is a special object that can be used to simulate smoke, explosions, flames, rain, or similar effects.  It does this by moving images, called particles, through an animation, either once or repeatedly, and with some randomness.  The aspects of the animation are set as properties.  The image used, number of particles used, and rate of animation are also properties.  If the animation is not repeated, it can be triggered with the startAnimation method.

By default, a particle emitter will emit a white square.  This is actually 100 particles all rendering in one place, until you change some of the properties to give the particles an image, spread the particles out and give them velocity.

#### Properties:

- **position** -- the location of the particle emitter.
- **rotation** -- rotation of the particle emitter.  By default, particles emit upwards.
- **particleCount** -- the total number of particles used for the emitter.
- **particleLifetime** -- the total number of seconds before the particle is destroyed or recycled.
- **particleRate** -- the number of particles (re)emitted per second.  A rate of zero means all particles at once (an explosion).
- **particleRateRandom** -- some randomness added to the emit rate.
- **particleDrag** -- slows down particles as they fly
- **particleGravity** -- causes particles to fall with gravity.  A negative value will cause the particles to rise, like smoke.
- **particlePositionRandom** -- randomness added to the initial position of the particle.  For example, for rain, you'd want to randomize the x and y dimensions (otherwise all the rain will come from one point).
- **particleImage** -- the image to use for the particles
- **particleTransparency** -- the transparency of the particle image (initially)
- **particleFadeRate** -- the rate of change in the particles transparency.
- **particleSize** -- the initial size of particles.
- **particleGrowthRate** -- how fast the particle size grows the older the particle gets.
- **particleVelocity** -- how fast particles leaves the emitter, and in what direction.
- **particleVelocityRandom** -- randomness added to the particle velocity to "spray" the particles.

#### Methods

- **startAnimation** -- causes the particle emitter to start emitting particles.  This must be called if any particles are to be seen.
- **stopAnimation** -- stops the particle emitter from emitting particles.
