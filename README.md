![logo](https://raw.githubusercontent.com/dev-labs-bg/fullscreen-video-view/master/logo.png)
===
[![Download](https://api.bintray.com/packages/slavipetrov/maven/fullscreen-video-view/images/download.svg) ](https://bintray.com/slavipetrov/maven/fullscreen-video-view/_latestVersion)
[![Build Status](https://travis-ci.org/dev-labs-bg/fullscreen-video-view.svg?branch=1.0.0)](https://travis-ci.org/dev-labs-bg/fullscreen-video-view)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5d2c2572dd7b4a2fb5eeabd6c2e18fbc)](https://www.codacy.com/app/slavipetrov/fullscreen-video-view?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dev-labs-bg/fullscreen-video-view&amp;utm_campaign=Badge_Grade)

FullscreenVideoView is a custom VideoView Android library which makes loading, setting up and going fullscreen for video views easy.

<img src="https://github.com/dev-labs-bg/fullscreen-video-view/blob/master/preview.gif" width="250" height="445">

Download
===
You can use Gradle:
```gradle
compile 'bg.devlabs.fullscreenvideoview:library:1.1.0'
```
or Maven:
```maven
<dependency>
  <groupId>bg.devlabs.fullscreenvideoview</groupId>
  <artifactId>library</artifactId>
  <version>1.1.0</version>
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

Basic video loading (from URL or from a file)
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
### Pause video

If you want to pause the video programmatically you can use the `pause()` method.
```kotlin
fullscreenVideoView.pause()
```

### Hide fullscreen button

```kotlin
fullscreenVideoView.hideFullscreenButton()
```

Compatibility
===
- Minimum Android SDK: API level 19
- Compile Android SDK: API level 28

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
