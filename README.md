![logo](https://raw.githubusercontent.com/dev-labs-bg/fullscreen-video-view/master/logo.png)
===
[![Download](https://api.bintray.com/packages/slavipetrov/maven/fullscreen-video-view/images/download.svg) ](https://bintray.com/slavipetrov/maven/fullscreen-video-view/_latestVersion)
[![Build Status](https://travis-ci.org/dev-labs-bg/fullscreen-video-view.svg?branch=master)](https://travis-ci.org/dev-labs-bg/fullscreen-video-view)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5d2c2572dd7b4a2fb5eeabd6c2e18fbc)](https://www.codacy.com/app/slavipetrov/fullscreen-video-view?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dev-labs-bg/fullscreen-video-view&amp;utm_campaign=Badge_Grade)

FullscreenVideoView is a custom VideoView Android library which makes loading, setting up and going fullscreen for video views easy.

<img src="https://github.com/dev-labs-bg/fullscreen-video-view/blob/master/preview.gif" width="250" height="445">

Download
===
You can use Gradle:
```gradle
compile 'bg.devlabs.fullscreenvideoview:library:1.1.4'
```
or Maven:
```maven
<dependency>
  <groupId>bg.devlabs.fullscreenvideoview</groupId>
  <artifactId>library</artifactId>
  <version>1.1.4</version>
  <type>pom</type>
</dependency>
```

How to use FullscreenVideoView?
===
Declare the FullscreenVideoView in the XML layout file
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <bg.devlabs.fullscreenvideoview.FullscreenVideoView
        android:id="@+id/fullscreenVideoView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

The Activity where the FullscreenVideoView is declared should handle configuration changes, which imposes this change in the AndroidManifest.xml file
```xml
<activity
    android:name="your_activity_name"
    android:configChanges="orientation|screenSize" />
```

### Network security configuration on Android API 23 or later

On devices with Android API 23 or later a network security configuration should be set up to allow the cleartext traffic.

- Add the XML tag `android:usesCleartextTraffic="true"` in the application tag in `AndroidManifest.xml`.
- Add a `network_security_config.xml` file in the `res/xml` directory and add the domains that you want to use in your application. To use this network configuration it must be added in the application tag in `AndroidManifest.xml` (`android:networkSecurityConfig="@xml/network_security_config`).

### Basic video loading (from URL or from a file)
```kotlin
// Loading from URL
override fun onCreate(savedInstanceState: Bundle?) {
	...
	val fullscreenVideoView = findViewById(R.id.fullscreenVideoView)
	val videoUrl = "https://clips.vorwaerts-gmbh.de/VfE_html5.mp4"
	fullscreenVideoView.videoUrl(videoUrl)
}

// Loading from file
override fun onCreate(savedInstanceState: Bundle?) {
	...
	val fullscreenVideoView = findViewById(R.id.fullscreenVideoView)
	val videoFile = new File("file_path")
	fullscreenVideoView.videoFile(videoFile)
}
```

### Change controls drawable resources

Java or Kotlin
```kotlin
fullscreenVideoView.videoUrl(videoUrl)
        .playDrawable(R.drawable.ic_play)
        .pauseDrawable(R.drawable.ic_pause)
        .fastForwardDrawable(R.drawable.ic_fast_forward)
        .rewindDrawable(R.drawable.ic_rewind)
        .enterFullscreenDrawable(R.drawable.ic_fullscreen)
        .exitFullscreenDrawable(R.drawable.ic_fullscreen_exit)
```

XML
```xml
<bg.devlabs.fullscreenvideoview.FullscreenVideoView
        android:id="@+id/fullscreenVideoView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:play_drawable="@drawable/ic_play"
        app:pause_drawable="@drawable/ic_pause"
        app:ffwd_drawable="@drawable/ic_fast_forward"
        app:rew_drawable="@drawable/ic_rewind"
        app:enter_fullscreen_drawable="@drawable/ic_fullscreen"
        app:exit_fullscreen_drawable="@drawable/ic_fullscreen_exit"/>
```

### Enable/disable controls

```kotlin
fullscreenVideoView.videoUrl(videoUrl)
        .disablePause()
        .addSeekBackwardButton()
        .addSeekForwardButton()
```

### Enable video auto-start

```kotlin
fullscreenVideoView.videoUrl(videoUrl)
        .enableAutoStart()
```

### Customize fast-forward and/or rewind seconds

```kotlin
fullscreenVideoView.videoUrl(videoUrl)
        .fastForwardSeconds(5)
        .rewindSeconds(5)
```

### Change the playback speed (only for API 23 and above)

There are 7 playback speed values which are added by default, but they can be changed with custom ones when `playbackSpeedOptions` is used.
```kotlin
val playbackOptions = PlaybackSpeedOptions().addSpeeds(0.25f, 0.5f, 0.75f, 1f)

fullscreenVideoView.videoUrl(videoUrl)
        .addPlaybackSpeedButton()
        .playbackSpeedOptions(playbackOptions)
```

### Add thumbnail

This feature supports loading only drawables from the Android project.
```kotlin
val thumbnailResId = R.drawable.video_thumbnail

fullscreenVideoView.videoUrl(videoUrl)
	.thumbnail(thumbnailResId)
```
### Play/Pause video programmatically

If you want to play/pause the video programmatically you can use the `play()/pause()` method.
```kotlin
fullscreenVideoView.play()
// OR
fullscreenVideoView.pause()
```

### Hide progress views

Hiding the progress views can be implemented initially from calling the Builder function or later by calling the view method.

```kotlin
// Hide initially from the Builder
fullscreenVideoView.videoUrl(videoUrl)
        .hideProgress()

// OR

// Hide by calling the view method
fullscreenVideoView.hideProgress()
```

### Hide fullscreen button

Hiding the fullscreen button can be implemented initially from calling the Builder function or later by calling the view method.

```kotlin
// Hide initially from the Builder
fullscreenVideoView.videoUrl(videoUrl)
        .hideFullscreenButton()

// OR

// Hide by calling the view method
fullscreenVideoView.hideFullscreenButton()
```

### Listen for errors

Listening for errors can be implemented initially from calling the Builder function or later by calling the view method.

```kotlin
// Add initially from the Builder
fullscreenVideoView.videoUrl(videoUrl)
        .addOnErrorListener(object : OnErrorListener {
                override fun onError(exception: FullscreenVideoViewException?) {
                        // Handle error
                }        
        })
        
// OR

// Add by calling the view method
fullscreenVideoView.addOnErrorListener(object : OnErrorListener {
        override fun onError(exception: FullscreenVideoViewException?) {
                // Handle error
        }    
})
```

### Listen for Media Controller events

To listen for Media Controller events you should use the Builder function `mediaControllerListener`. You can either pass the `MediaControllerListener` interface or `MediaControllerListenerAdapter` if don't need to use all of the methods of the interface.

```kotlin
// Using the MediaControllerListener interface
fullscreenVideoView.videoUrl(videoUrl)
	.mediaControllerListener(object : MediaControllerListener {
		    override fun onPlayClicked() {
                        // Do something when the play button is clicked
                    }

                    override fun onPauseClicked() {
                        // Do something when the pause button is clicked
                    }

                    override fun onRewindClicked() {
                        // Do something when the rewind button is clicked
                    }

                    override fun onFastForwardClicked() {
                        // Do something when the fast forward button is clicked
                    }

                    override fun onFullscreenClicked() {
                        // Do something when the fullscreen button is clicked
                    }

                    override fun onSeekBarProgressChanged(progressMs: Long) {
                        // Do something when the progress SeekBar is changed by click or a drag event
                    }
	})
	
// Using the MediaControllerListenerAdapter interface with only onPlayClicked and onPauseClicked methods
fullscreenVideoView.videoUrl(videoUrl)
	.mediaControllerListener(object : MediaControllerListenerAdapter() {
		    override fun onPlayClicked() {
			// Do something when the play button is clicked
                    }

                    override fun onPauseClicked() {
			// Do something when the pause button is clicked
                    }
	})
```

### Seek to position

Seek to a selected position by calling the `seekTo` method and passing the time in milliseconds to it.

```
fullscreenVideoView.videoUrl(videoUrl)
	.seekTo(5000)
```

Compatibility
===
- Minimum Android SDK: API level 19
- Compile Android SDK: API level 29

Known issues
===
There is a memory leak in Android 7 (API levels 24 and 25), which is known and [listed](https://github.com/square/leakcanary/issues/721)
in the LeakCanary repository issues.

License
===

```
Copyright 2017 Dev Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
