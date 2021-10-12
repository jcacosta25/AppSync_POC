package com.apptegy.appsync

import android.content.Context
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient


class ClientFactory {
	
	companion object {
		private lateinit var client:AWSAppSyncClient
		
		@Synchronized
		fun getInstance(context: Context): AWSAppSyncClient {
			if (::client.isInitialized.not()) {
				val awsConfig = AWSConfiguration(context)
				client = AWSAppSyncClient.builder()
					.context(context)
					.awsConfiguration(awsConfig)
					.build()
			}
			return client
		}
	}
}