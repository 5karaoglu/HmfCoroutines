
# HmfCoroutineExtensions

Coroutines extensions library for HMS Android SDKs.


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
  private fun cloudFunctions(){
        val function = AGConnectFunction.getInstance()
        val map = mapOf<String,String>()
        val uiScope = CoroutineScope(Dispatchers.Main + Job())
        CoroutineScope(Dispatchers.IO + Job()).launch(Dispatchers.IO) {
            try {
                val result = function.wrap("myhandlerxxxx-$/latest").call(map).await()
                uiScope.launch{ Log.d(TAG, "callCloudFun: $result.") }
            } catch (e: Exception) {
                uiScope.launch{ Log.e(TAG, "callCloudFun: ${e.message}") }
            }
        }
    }
```

