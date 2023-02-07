
# HmfCoroutineExtensions

Coroutine extensions library for AGC Android SDK.


## Deployment

Add it in your root build.gradle at the end of repositories:

```bash
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency

```bash
  dependencies {
	         implementation 'com.github.5karaoglu:HmfCoroutines:0.9.1'
	}
```

Usage:

```bash
  mCloudDBZone!!.executeUpsert(message).await()
  function.wrap("myhandlerxxxx-$/latest").call(map).asDeferred()
```

