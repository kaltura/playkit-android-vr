[![CI Status](https://travis-ci.org/kaltura/playkit-android-vr.svg?branch=develop)](https://travis-ci.org/kaltura/playkit-android-vr)
[ ![Download](https://api.bintray.com/packages/kaltura/android/vrplugin/images/download.svg) ](https://bintray.com/kaltura/android/vrplugin/_latestVersion)
[![License](https://img.shields.io/badge/license-AGPLv3-black.svg)](https://github.com/kaltura/playkit-android-vr/blob/master/LICENSE)
![Android](https://img.shields.io/badge/platform-android-green.svg)

# playkit-android-vr

## About Kaltura VR

The VR module gives ability to play VR and 360 streams
based on the MD360Player4Android having the same playkit player API

## VR Integration  

VR Module is build on top of the Kaltura Playkit SDK; therefore, to get started, you'll need to add dependencies in your application build.gradle file.

```
dependencies {
    implementation 'com.kaltura:playkit-android-vr:XXX' //instead of XXX use latest version. 
   
}

repositories {
    maven { url 'https://jitpack.io' }
}
```

Next, lets see how to use the VR Module in your application.

There are 2 possibilites to Generate PKMeidaEntry
* Using Providers (OVP/Phoenix)
* Creating it manualy

VR Module can work in 2 Modes 360 Player or Cardbord Player Mode
for each mode application can cofigure the IntractionMode for each Player
The default InteractionMode is VRInteractionMode.Touch

```java 
public enum VRInteractionMode {
    Motion,                  //360 Player
    Touch,                   //360 Player
    MotionWithTouch,         //360 Player
    CardboardMotion,         //Cardbord Player
    CardboardMotionWithTouch //Cardbord Player
}
```

### Using Providers:
in case the media entry is generated using the Providers if 360 meida was requested the callback of the provider will create a VRPKMediaEntry instead of 
the PKMediaEntry that is created for regular media.
The `VRPKMediaEntry` will be created with default VRSettings which application can set again or to change specific value using setter methods.


```java
//define app VR configuration
((VRPKMediaEntry) vrMediaEntry).getVrSettings().setVrModeEnabled(isVrModeEnabled());
((VRPKMediaEntry) vrMediaEntry).getVrSettings().setFlingEnabled(isFlingEnabled());
((VRPKMediaEntry) vrMediaEntry).getVrSettings().setInteractionMode(getInteractionMode());
((VRPKMediaEntry) vrMediaEntry).getVrSettings().setZoomWithPinchEnabled(isZoomWithPinchEnabled());
``` 

### Manual Creation:
```java
//define app VR configuration
VRSettings vrSettings = new VRSettings()
           .setVrModeEnabled(isVrModeEnabled())
           .setFlingEnabled(isFlingEnabled())
           .setInteractionMode(VRInteractionMode.MotionWithTouch)
           .setZoomWithPinchEnabled(isZoomWithPinchEnabled());


PKMediaEntry vrMediaEntry = new VRPKMediaEntry().setVRParams(vrSettings);
        //Set additional data for the entry.
        vrMediaEntry.setId("entryId");
        vrMediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Vod);
        List<PKMediaSource> mediaSources = createMediaSources();
        vrMediaEntry.setSources(mediaSources);```
```

## VR Controller

Once Player is set there are several operartions that can control the VR Module behaviour:

```java
public interface VRController extends PKController {


    /**
     * When set to true, will split screen in a way that fit to cardboard devices.
     * When set to false, content will be displayed in an ordinary 360 way.
     * @param shouldEnable - if enable VRMode.
     */
    void enableVRMode(boolean shouldEnable);

    /**
     * When set to true, will allow some continiues motion effect when surface is swiped.
     * If disabled, motion will stop whenever MotionEvent.ACTION_UP/MotionEvent.ACTION_CANCEL detected.
     * @param shouldEnable - if enable this.
     */
    void setFlingEnabled(boolean shouldEnable);

    /**
     * Set the Interaction mode with 360/VR surface.
     * Before applying, application must check if requested mode is supported by the device.
     * @param mode - requested mode.
     */
    void setInteractionMode(VRInteractionMode mode);

    /**
     * Allows to enable zooming of the surface with pinch motion.
     * @param shouldEnable - if this should be enabled.
     */
    void setZoomWithPinchEnabled(boolean shouldEnable);

    /**
     * Set clickListener on the 360/VR surface. When application wants to detect
     * click input on the 360/VR surface it is required to pass it here instead of
     * just attaching it to the containerView.
     * @param onClickListener - View.ClickListener
     */
    void setOnClickListener(View.OnClickListener onClickListener);

    /**
     * @return - true if in vr mode. otherwise false.
     */
    boolean isVRModeEnabled();

    /**
     * @return - true if pinch is enabled.
     */
    boolean isPinchEnabled();

    /**
     * @return - true if enabled.
     */
    boolean isFlingEnabled();

    /**
     *
     * @return - current interaction mode.
     */
    VRInteractionMode getInteractionMode();
}
```
#### Getting access to the controller
```
VRController vrController = mPlayer.getController(VRController.class);
```
### Validating Mode Change is supported by device. 
Before applying InteractionMode(no matter if by calling api setInteractionMode() or change in in VRSettings setInteractionMode()) you should first check if this mode is supported by the device. In some cases device hardware does not support requested mode(for example InteractionMode.Motion not supported for devices without Gyro)

``` java 
 boolean modeSupported = VRUtil.isModeSupported(context, VRInteractionMode.CardboardMotion);
 if (modeSupported) {
 	vrController.setInteractionMode(VRInteractionMode.CardboardMotion);
 }
                        
```

### Example - Change The VR Mode During Playback:

```java
 case TOGGLE_VR:
                VRController vrController = mPlayer.getController(VRController.class);
                boolean currentState = vrController.isVRModeEnabled();
                vrController.enableVRMode(!currentState);
                break;
        }

```

### Example - Listen to clicks on surface 
App must pass the click listener via the controller

```java
VRController vrController = mPlayer.getController(VRController.class);
            mPlayerControlsView.setVrIconVisibility(true);                                               mPlayerControlsView.setVRActivated(vrController.isVRModeEnabled());
            vrController.setOnClickListener(new View.OnClickListener() {
                @Override
               public void onClick(View v) {
                     //application code for handaling ui operations
                     handleContainerClick();
               }
           });
}
```
### VR Controlers  

