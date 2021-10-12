package com.apptegy.appsync

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.Amplify

class AppsyncApp : MultiDexApplication() {
	override fun onCreate() {
		super.onCreate()
		try {
			Amplify.configure(applicationContext)
			Log.i("MyAmplifyApp", "Initialized Amplify")
		} catch (error: AmplifyException) {
			Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
		}
	}
}