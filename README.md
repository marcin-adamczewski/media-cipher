# Media-Cipher #
Media-Cipher is a library that allows to:
  * Download files and encrypt them on the fly
  * Play encrypted media files by decrypting it on the fly
  
## Using Media-Cipher ##
The library is available through JitPack.

You have to add following line to your project build.gradle
```
allprojects {
 repositories {
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
```

And following line to app build.gradle

```
compile 'com.github.marcin-adamczewski:media-cipher:VERSION'
```
`VERSION` means the version of library you want to use.
It may be one of the Tags (from release section) or commit hash or branch-SNAPSHOT.
More info here https://jitpack.io/

## Dependencies ##
The library depends on two libraries:
 * ExoPlayer - which must be used for playing your media. MediaCipher library provides extension of ExoPlayer's MediaSource class
 so it is easy to decrypt data on the fly. https://github.com/google/ExoPlayer
 * FileDownloader - powerfull library for downloading. MediaCipher library provides custom OutputStream that is responsible
 for encrypting data while being downloaded. https://github.com/lingochamp/FileDownloader
 
 ## Wiki ##
 Basically there is no need to write wiki because everything is included in very short sample.
 Anyway there are three steps you need to make:
 
 * Init library in Application class
 ```
Config config = new Config(BuildConfig.DEBUG ? Config.LogLevel.DEBUG : Config.LogLevel.NONE);
MediaCipher.init(this, config, new Listener() {
    @Override
    public void onKeyCorrupted(final Throwable throwable) {
    }

    @Override
    public void onError(final Throwable throwable) {
    }
});
 ```
 
 * Write your download manager using `FileDownloader` class from FileDownloader library, e.g.
 ```
  final FileDownloader fileDownloader = FileDownloader.getImpl();
  fileDownloader
          .create(url)
          .setPath(DOWNLOAD_PATH)
          .addFinishListener(downloadFinishedListener)
          .setListener(fileDownloadListener)
          .setAutoRetryTimes(1)
          .asInQueueTask()
          .enqueue();
  fileDownloader.start(fileDownloadListener, false);
  ```
 
 * Write your player based on ExoPlayer and provide custom data source from this library
 ```
 exoPlayer.prepare(MediaCipher.getInstance().getEncryptedFileDataSourceFactory(new File(uri.getPath())))
 ```
 
 ## Encryption ##
 Whole encryption and decryption stuff is based of AndroidKeyStore. That's why minSdk of current library version is 18 !
 Encryption algorithm is AES/CTR/NoPadding as I find it most suitable for streaming.
 
 Before using this library you should be aware of limitations of AndroidKeyStore. You can read about it here
 https://doridori.github.io/android-security-the-forgetful-keystore/#sthash.on3ZPwjc.6LLYdFom.dpbs
 
 
 # License

    Copyright [2017] [Marcin Adamczewski <madamczewskix@gmail.com>]
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    	http://www.apache.org/licenses/LICENSE-2.0
        
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



