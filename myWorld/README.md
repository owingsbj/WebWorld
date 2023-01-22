# myWorld

A 3D graphics platform for building Android games.

All of the games written by Gallant Realm are based on this homegrown 3D graphics engine.  It's not the best graphics engine by any means, but it gets the job done and was fun to build.

The version embedded in WebWorld has made significant modifications:
- conversion to use quaternions for rotation
- improved physics
- use of OpenGL ES 3.0 for more predicable shader behavior
- setter methods on the model that take simple strings or arrays avoiding needing to make vector, quaternion, texture, color help objects 

You'll also find pieces of a client/server implementation which is likely not working.  This predates my Android programming and was a pure-Java windows implementation which allowed a server world to be mirrored by clients.
I might get this working again so I've left the code in place.
