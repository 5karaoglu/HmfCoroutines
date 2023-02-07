package com.besirkaraoglu.hmfcoroutines

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.besirkaraoglu.hmfcoroutineextensions.await
import com.besirkaraoglu.hmfcoroutines.model.Message
import com.besirkaraoglu.hmfcoroutines.model.ObjectTypeInfoHelper
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.huawei.agconnect.function.AGConnectFunction
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private var mCloudDB: AGConnectCloudDB? = null
    private var mConfig: CloudDBZoneConfig? = null
    var mCloudDBZone: CloudDBZone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAGConnectCloudDB(this)

        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        button.setOnClickListener {
            upsertMessage()
        }
        button2.setOnClickListener {
            queryMessages()
        }
    }

    private fun cloudStorage(){
        val storageManagement = AGCStorageManagement.getInstance()
        val reference = storageManagement!!.getStorageReference("images/demo.jpg")
        val uiScope = CoroutineScope(Dispatchers.Main + Job())
        CoroutineScope(Dispatchers.IO + Job()).launch(Dispatchers.IO) {
            try {
                val result = reference.putFile(File("path/images/test.jpg")).await()
                uiScope.launch{ Log.d(TAG, "cloudStorage: $result.") }
            } catch (e: Exception) {
                uiScope.launch{ Log.e(TAG, "cloudStorage: ${e.message}") }
            }
        }
    }

    private fun authService(){
        val uiScope = CoroutineScope(Dispatchers.Main + Job())
        CoroutineScope(Dispatchers.IO + Job()).launch(Dispatchers.IO) {
            try {
                val result = AGConnectAuth.getInstance().signIn(this@MainActivity,
                    AGConnectAuthCredential.HMS_Provider).await()
                uiScope.launch{ Log.d(TAG, "authService: $result.") }
            } catch (e: Exception) {
                uiScope.launch{ Log.e(TAG, "authService: ${e.message}") }
            }
        }
    }

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

    private fun queryMessages(){
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val queryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(Message::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        val uiScope = CoroutineScope(Dispatchers.Main + Job())
        CoroutineScope(Dispatchers.IO + Job()).launch(Dispatchers.IO) {
            try {
                val result = queryTask.await()
                uiScope.launch{ Log.d(TAG, "queryMessages: $result.") }
            } catch (e: Exception) {
                uiScope.launch{ Log.e(TAG, "queryMessages: ${e.message}") }
            }
        }
    }


    private fun upsertMessage() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val message = Message()
        message.uid = UUID.randomUUID().toString()
        message.text = "Hello there. This is the text i guess."
        val upsertTask = mCloudDBZone!!.executeUpsert(message)
        val uiScope = CoroutineScope(Dispatchers.Main + Job())
        CoroutineScope(Dispatchers.IO + Job()).launch(Dispatchers.IO) {
            try {
                val result = upsertTask.await()
                uiScope.launch{ Log.d(TAG, "upsertMessage: $result.") }
            } catch (e: Exception) {
                uiScope.launch{ Log.e(TAG, "upsertMessage: ${e.message}") }
            }
        }

    }

    private fun initAGConnectCloudDB(context: Context) {
        AGConnectCloudDB.initialize(context)
        AGConnectInstance.buildInstance(
            AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.GERMANY).build(context)
        )
        mCloudDB = AGConnectCloudDB.getInstance(AGConnectInstance.getInstance(), AGConnectAuth.getInstance())
        mCloudDB!!.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        mConfig = CloudDBZoneConfig(
            "DBZone1",
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
        mConfig!!.persistenceEnabled = true
        val task = mCloudDB!!.openCloudDBZone2(mConfig!!, true)
        task.addOnSuccessListener {
            Log.i(TAG, "Open cloudDBZone success")
            mCloudDBZone = it
        }.addOnFailureListener {
            Log.w(TAG, "Open cloudDBZone failed for " + it.message)
        }
    }
}