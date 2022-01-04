# Volume Provider Example

This example refers to [the bug](https://issuetracker.google.com/issues/201546605) which was introduced in Android 12.

Volume button clicks events are not delivered to [VolumeProviderCompat](https://developer.android.com/reference/androidx/media/VolumeProviderCompat?hl=nl). After configuring `MediaSessionCompat` by calling [setPlaybackToRemote(VolumeProviderCompat volumeProvider)](https://developer.android.com/reference/android/support/v4/media/session/MediaSessionCompat?hl=nl#setPlaybackToRemote(androidx.media.VolumeProviderCompat)), the media session does not receive volume button events. This results in a situation that a user cannot adjust the volume of the media when the device is casting audio.

The behavior is inconsistent with the documentation which clearly says that calling `setPlaybackToRemote` method "configures this session to use remote volume handling. This must be called to receive volume button events, otherwise the system will adjust the current stream volume for this session."


### Steps to reproduce the issue
1. Build the app and run on a device with Android 12/12L.
2. Click "Start Service" button.
3. Get back to system launcher (background the app).
4. Turn off the screen.
5. Click a volume button (e.g. volume up).

### Expected result
`onAdjustVolume` method from `VolumeProvider` class should be invoked and the following message should be shwoing in logcat: 
`D/VolumeProvider: onAdjustVolume, direction: 1`

### Actual result
`onAdjustVolume` method is never called. `VolumeProvider` is not notfied about the volume changes.

### Additional info
Repeting the above steps on a device with Android 11 results in a proper behavior (see `Expected result` section).
