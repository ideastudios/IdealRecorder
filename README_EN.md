# IdealRecorder
an ideal WAV PCM recorder library for Android 

Record WAV or PCM voice library on Android, support volume, recording data, recording status, file path callback

## [README of Chinese](https://github.com/ideastudios/IdealRecorder/README.md)

## GIF
<img src="gif/screenrecorder.gif"/>

## feature

- Customizable recording parameters (sampling rate, number of bits, single and dual channel)
- Easy to use, call start to start recording, call stop to stop recording.
- Support for setting the maximum recording voice duration. It will automatically stop when maximum duration comes.
- Support saving WAV files when you set the save path.
- You can set the volume callback interval duration, support return buffer data when recording.
- Callback when no record permission



## Initialization

Add code in the Application or Activity onCreate () method,pass context

```java
  IdealRecorder.getInstance().init(this);
```



## How to use

Start recording
```java
        idealRecorder = IdealRecorder.getInstance();

    	idealRecorder.setRecordFilePath(getSaveFilePath());
        //If you need to save the recording file, set the path and save it automatically.
        //you can save it on you own by getting data in onRecordData callback

        idealRecorder.setRecordConfig(recordConfig).setMaxRecordTime(20000).setVolumeInterval(200);

        idealRecorder.setStatusListener(statusListener);
         //set listener

        idealRecorder.start();
        //start record
```


setRecordFilePath path should be absolutePath

recordConfig look IdealRecorder.RecordConfig class


Please refer to the demo code .


Stop recording
```java

        idealRecorder.stop();

```





## Note

- The project manifest has declared the RECORD_AUDIO and WRITE_EXTERNAL_STORAGE permission .However, after Android 6.0, you still need to request permission on you own.


## Gradle
[![](https://www.jitpack.io/v/ideastudios/IdealRecorder.svg)](https://www.jitpack.io/#ideastudios/IdealRecorder)
1. Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2. Add the dependency
```
	dependencies {
	        implementation'com.github.ideastudios:IdealRecorder:2.0.4'
	}


```


## Thanks

WaveLineView[Jay-Goo/WaveLineView](https://github.com/Jay-Goo/WaveLineView)
AndPermission[yanzhenjie/AndPermission](https://github.com/yanzhenjie/AndPermission)

