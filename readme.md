# HTTPS with OKHTTP library

for edit see: https://github.com/Hydrothermal/better-pastebin/blob/master/lib/better-pastebin.js

Project development:
- Enter the developer key: enter your own developer key, it is stored securely in EncryptedSharedPreferences
- Enter the user credentials: enter your own user email and password data, it is stored securely in EncryptedSharedPreferences
- Login to Pastebin.com: login to your Pastebin.com account using the user credentials
- Select a paste: all your pastes are listed TODO: jump to a PastesViewActivity
- List your pastes: output of all pastes in a textview
- Post public paste (no expiration)
- Post public paste (10 minutes expiration)
- xxx
- View pastes on Pastebin.com
- Encrypt a string: secure encryption of a string using AES-GCM with key derivation from a passphrase (10000 rounds, HmacSha256)
- Browse a folder in internal storage: list all folders in internal storage, click on folder and retrieve a file list in this foldert



build.gradle (app) (check for updates here: https://square.github.io/okhttp/):
```plaintext
implementation 'com.squareup.okhttp3:okhttp:4.10.0'

// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
implementation group: 'com.fasterxml.jackson.core', name: 'jackson-core', version: '2.14.1'
// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
implementation group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.14.1'
// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations
implementation group: 'com.fasterxml.jackson.core', name: 'jackson-annotations', version: '2.14.1'

// for encrypted storage
implementation 'androidx.security:security-crypto:1.0.0'
// for selectPaste - recyclerview
implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
```

AndroidManifest.xml:
```plaintext
<uses-permission android:name="android.permission.INTERNET"/>
```

General overview: https://square.github.io/okhttp/

Receipes by the library author: https://square.github.io/okhttp/recipes/

Tutorial: https://www.digitalocean.com/community/tutorials/okhttp-android-example-tutorial

```plaintext

```



```plaintext

```


```plaintext

```


```plaintext

```