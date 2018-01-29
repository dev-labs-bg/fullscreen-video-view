![logo](https://raw.githubusercontent.com/dev-labs-bg/fullscreen-video-view/master/logo.png)
==========
[ ![Download](https://api.bintray.com/packages/slavipetrov/maven/fullscreen-video-view/images/download.svg) ](https://bintray.com/slavipetrov/maven/fullscreen-video-view/_latestVersion)

FullscreenVideoView is a custom VideoView Android library which makes loading, setting up and going fullscreen for video views easy.

<img src="https://github.com/dev-labs-bg/fullscreen-video-view/blob/master/preview.gif" width="250" height="445">

Download
==========
You can use Gradle:
```gradle
compile 'bg.devlabs.fullscreenvideoview:library:0.0.6'
```
or Maven:
```maven
<dependency>
  <groupId>bg.devlabs.fullscreenvideoview</groupId>
  <artifactId>library</artifactId>
  <version>0.0.6</version>
  <type>pom</type>
</dependency>
```

How to use FullscreenVideoView?
==========
Declare the FullscreenVideoView in the XML layout file
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
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
```java
// Loading from URL
@Override public void onCreate(Bundle savedInstanceState) {
	...
	FullscreenVideoView fullscreenVideoView = findViewById(R.id.fullscreenVideoView);
	String videoUrl = "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4";
	fullscreenVideoView.videoUrl(videoUrl);
}

// Loading from file
@Override public void onCreate(Bundle savedInstanceState) {
	...
	FullscreenVideoView fullscreenVideoView = findViewById(R.id.fullscreenVideoView);
	File videoFile = new File("file_path");
	fullscreenVideoView.videoFile(videoFile);
}
```

Change controls drawable resources
----------
Java
```java
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

Enable/disable controls
----------
```java
fullscreenVideoView.videoUrl(videoUrl)
        .canPause(true)
        .canSeekBackward(false)
        .canSeekForward(false)
```

Enable video auto-start
----------
```java
fullscreenVideoView.videoUrl(videoUrl)
        .enableAutoStart()
```

Customize fast-forward and/or rewind seconds
----------
```java
fullscreenVideoView.videoUrl(videoUrl)
        .fastForwardSeconds(5)
        .rewindSeconds(5)
```

Compatibility
==========
- Minimum Android SDK: API level 19
- Compile Android SDK: API level 27

Known issues
==========
There is a memory leak in Android 7 (API levels 24 and 25), which is known and [listed](https://github.com/square/leakcanary/issues/721)
in the LeakCanary repository issues.

License
==========
Apache 2.0. See the [LICENSE](https://github.com/dev-labs-bg/fullscreen-video-view/blob/master/LICENSE.txt) file for details.
