# WebWorld Writer's Guide

This guide will help you to create your own avatars and worlds.

## Enable Storage Permission

WebWorld cannot read local files by default.  You need to give it this permission in order for it to read local avatars and worlds that you create:

- Using Settings (the gear), go to Apps.
- Find the WebWorld application and press to see its app info.
- Press on Permissions
- Press on Storage to give WebWorld the permission to read files. 

## Webworlds Folders

The first thing you need to do is create the folders needed to contain custom avatars and worlds you will make:

- Using a file manager, create a *webworlds* folder on the sdcard (usually /sdcard or /storage/emulated/0).
- Within the webworlds folder, create two folders: *avatars* and *worlds*.
- Within the avatars folder, create a folder for each avatar you want to create.  You can name the folders anything you want.
- Within the worlds folder, create a folder for each world you want to create.  You can name the folders anything you want.
- In the webworlds folder, create two plain-text files: listAvatars.jsp and listWorlds.jsp.  In these place a list of the folders with the avatars and worlds.

## avatar.properties

Each avatar folder must contain a file called *avatar.properties*.  This file provides a description of the avatar and points to an image (usually a screen snapshot) of the avatar.  Lastly, it has the name of a JavaScript file that builds the avatar and provides its behavior.  Here's an example:

```
name: Egg
author: BJ
description: A simple egg avatar, from EggWorld.
picture: happy.png
script: EggAvatar.js
```

Here's a description of each of the fields:

- **name** -- the name of the avatar.  It is shown at the top of the screen when playing the game.
- **author** -- the name you'd like to be called when the avatar is shared.
- **description** -- a paragraph describing the avatar.  Keep the text of the paragraph all on one line.
- **picture** -- the name of an image file containing a picture of the avatar.  This is usually a screen snapshot zoomed in on the avatar.
- **script** -- the file name of the avatar script -- a JavaScript file that will build the avatar and provide its behavior.

## world.properties

Likewise, every world folder must contain a file called *world.properties*.  This file provides a description of the world and also points to an image (usually a screen snapshot) of the world.  Lastly, it has the name of the JavaScript file that builds and runs your world.  Here's an example of the world.properties:

```
name: Blocks
author: BJ
description: A simple world to create things with blocks.
picture: world1.png
script: BlockWorld.js
```

Here's a description of each of the fields:

- **name** -- the name of the world.  It is shown at the top of the screen when playing the game.
- **author** -- the name you'd like to be called when the world is shared.
- **description** -- a paragraph describing the world.  Keep the text all on one line.
- **picture** -- the name of an image file containing a picture of the world.  This is usually a screen snapshot while playing in the world.
- **script** -- the file name of the world script -- a JavaScript file that will build and run the world.

Both the picture and the script files should be in the same folder as world.properties -- the world folder.  Files are always looked for relative to the world folder, so you shouldn't need any path information in the folder name.   (So for instance, use myworld.js rather than /user/username/myworld/myworld.js.)

## Scripts

The scripts create the avatar and world and provide behaviors to the avatar and objects in the world.   They are written in JavaScript, so all built-in JavaScript functions are available to use, such as those in the Math object.  However, since WebWorld is not a browser for web pages, the browser scripting objects document and window are not available.   Instead, you have the built-in WebWorld objects *world* and *user*.  In addition, there are predefined classes to create different types of 3D objects composing the avatar and world and attach event functions to them to give the objects behavior.  These are described in the [Reference](reference.md). 

The world, user and 3D objects contains fields of three types:

- **properties** -- these are simple fields with a data value
- **methods** -- these are functions that can be invoked to perform an action on the object
- **events** -- these are fields that can be assigned to a function.  That function will then be invoked when the particular situation on the object occurs.

There are also some helper objects:

- **Vector** -- for describing the dimensions for positions and rotations.
- **Texture** -- for describing a texture pattern.
- **Color** -- for describing a color.

## Developing Your Avatars and Worlds

There are two ways to develop: using only Android or using an Android Emulator along with your Mac or PC:

#### Using only Android

You can develop your WebWorld completely on your Android device.  This is easier with a large screened device such as a tablet, especially when editing the JavaScript source.   You'll need to find a good editor app.  For JavaScript, the [Notepad Plus Code Editor](https://play.google.com/store/apps/details?id=com.woop.notepad) app seems like a pretty good editor.   For textures and sounds you'll need to find appropriate apps or obtain good files from other sources.  (Remember that many files have copyright, so you'll need to obtain the rights to distribute the files.)

You can use the Spck Editor app to edit all your avatars and worlds and maintain them on github.  If you do this, you can clone the webworlds project at https://github.com/owingsbj/webworlds, then add your own worlds.
In the WebWorld settings, use the /sdcard/Android/data/io.spck/files/webworlds as the local folder.

#### Using an Android Emulator and sharing with Mac or PC

You can run an Android Emulator, like [Genymotion](https://genymotion.com) on your Mac or PC and then edit your worlds with your favorite JavaScript editor on the Mac or PC and run them in the emulator.

The easiest way to share avatars and worlds between the emulator and your Mac or PC is with a WebDAV file server running on your Mac or PC.  For Mac, I've found WebDAVNav (in the app store) to be easy to set up and use.
Then, in the WebWorld app set the shared server URL to the root of the WebDAV server's URL.  (In my case, it is http://10.0.0.2:8080.)

## Debugging

Unfortunately, at this time there isn't a good debugging console to use within WebWorld, so you might find debugging hard to do.  I suggest that you add a little code, then run it to see if it has the effect you want, then add some more and run it, and so on.  This way you won't get too far ahead with coding before running the code to make sure it works.

You can use the **world.alert** method to popup a message.  This can help to let you know of situations during debugging.  You'll need to remove these alerts though after debugging.

## Publishing Your Avatar or World

When you finally have a cool avatar or world working and you want to share it with the WebWorld users, you can upload it to gallantrealm.com/webworld.  Follow these instructions:

1.  First, use a zip utility and zip up the avatar or world folder.
2.  Navigate to the [Share](https://gallantrealm.com/webworld/#!share.md) page.
2.  Sign-in to Google using your Google credentials.
3.  Select the file location of your zip file and press **Upload**. 

The avatar or world will be added to the lists of avatars and worlds.  Although it is uniquely identified by your Google user name, the name you used in the author field of the avatar or world properties will be shown to users.  If you've already uploaded the world and you upload it again, it will be replaced.


