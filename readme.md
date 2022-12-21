# HTTPS with OKHTTP library

for edit see: https://github.com/Hydrothermal/better-pastebin/blob/master/lib/better-pastebin.js




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