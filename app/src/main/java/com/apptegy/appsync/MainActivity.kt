package com.apptegy.appsync

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amazonaws.amplify.generated.graphql.GetUserBadgesQuery
import com.amazonaws.amplify.generated.graphql.OnUpdateUserBadgesSubscription
import com.amazonaws.amplify.generated.graphql.UpdateUserBadgesMutation
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amplifyframework.core.Amplify
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import type.UpdateUserBadgesInput


class MainActivity : AppCompatActivity() {
	
	lateinit var client: AWSAppSyncClient
	
	lateinit var messagesTotal:TextView
	lateinit var messagesAdd:Button
	lateinit var messagesDelete:Button
	lateinit var streamTotal:TextView
	lateinit var streamAdd:Button
	lateinit var streamDelete:Button
	lateinit var classworkTotal:TextView
	private lateinit var classworkAdd:Button
	lateinit var classworkDelete:Button
	var badges:HashMap<String,BadgeData> = hashMapOf()
	val testSchool = "urn:apptegy:global:school-data:course:woijdalkfneoi"
	private val testClientId = "urn:apptegy:global:sso:client:179"
	private val testUserId = "urn:apptegy:global:sso:user:011c08a8-677e-419e-b60e-41b183dc6c1a"
	private val gson = Gson()
	
	
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		messagesTotal = findViewById(R.id.messages_total)
		messagesAdd = findViewById(R.id.messages_add)
		messagesDelete = findViewById(R.id.messages_delete)
		streamTotal = findViewById(R.id.stream_total)
		streamAdd = findViewById(R.id.stream_add)
		streamDelete = findViewById(R.id.stream_delete)
		classworkTotal = findViewById(R.id.class_work_total)
		classworkAdd = findViewById(R.id.class_work_add)
		classworkDelete = findViewById(R.id.class_work_delete)
		
		
		query()
		
		messagesAdd.setOnClickListener {
			updateBadge(
				message = messagesTotal.text.toString().toInt().inc(),
				stream = streamTotal.text.toString().toInt(),
				classwork = classworkTotal.text.toString().toInt()
			)
		}
		messagesDelete.setOnClickListener {
			updateBadge(
				message = messagesTotal.text.toString().toInt().dec(),
				stream = streamTotal.text.toString().toInt(),
				classwork = classworkTotal.text.toString().toInt()
			)
		}
		streamAdd.setOnClickListener {
			updateBadge(
				message = messagesTotal.text.toString().toInt(),
				stream = streamTotal.text.toString().toInt().inc(),
				classwork = classworkTotal.text.toString().toInt()
			)
		}
		streamDelete.setOnClickListener {
			updateBadge(
				message = messagesTotal.text.toString().toInt(),
				stream = streamTotal.text.toString().toInt().dec(),
				classwork = classworkTotal.text.toString().toInt()
			)
		}
		classworkAdd.setOnClickListener {
			updateBadge(
				message = messagesTotal.text.toString().toInt(),
				stream = streamTotal.text.toString().toInt(),
				classwork = classworkTotal.text.toString().toInt().inc()
			)
		}
		classworkDelete.setOnClickListener {
			updateBadge(
				message = messagesTotal.text.toString().toInt(),
				stream = streamTotal.text.toString().toInt(),
				classwork = classworkTotal.text.toString().toInt().dec()
			)
		}
		subscription()
		
		
	}
	
	override fun onResume() {
		super.onResume()
		
	}
	
	private fun subscription() {
		val subscriptionCallback = object : AppSyncSubscriptionCall.Callback<OnUpdateUserBadgesSubscription.Data> {
			override fun onResponse(response: Response<OnUpdateUserBadgesSubscription.Data>) {
				response.data()?.onUpdateUserBadges()?.let {
					Log.d("User Badge Update", it.badges().toString())
					val badgeType = object : TypeToken<HashMap<String,BadgeData>>(){}.type
					badges.putAll(gson.fromJson(it.badges(),badgeType))
				}
				
				runOnUiThread {
					val badge = badges[testSchool]
					messagesTotal.text = badge?.messages.toString()
					streamTotal.text = badge?.stream.toString()
					classworkTotal.text = badge?.classwork.toString()
				}
			}

			override fun onFailure(e: ApolloException) {
				Log.e("User Subscription Fail",e.toString())
			}

			override fun onCompleted() {
				println("Subscription complete")
			}
		}
		client.subscribe(OnUpdateUserBadgesSubscription.builder()
			.client_id(testClientId)
			.user_id(testUserId)
			.build()).execute(subscriptionCallback)
		
		
	}
	
	private fun query() {
		if (::client.isInitialized.not()) {
			client = ClientFactory.getInstance(applicationContext)
		}

		
		client.query(
			GetUserBadgesQuery.builder()
				.client_id(testClientId)
				.user_id(testUserId)
				.build()
		).responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
			.enqueue(userBadges)
		
	}
	
	private val userBadges = object : GraphQLCall.Callback<GetUserBadgesQuery.Data>() {
		override fun onResponse(response: Response<GetUserBadgesQuery.Data>) {
			
			response.data()?.userBadges?.let {
				Log.d("User Badge", it.badges().toString())
				val badgeType = object : TypeToken<HashMap<String,BadgeData>>(){}.type
				badges.putAll(gson.fromJson(it.badges(),badgeType))
			} ?: run {
				Log.d("User Badge", "no badges")
			}
			
			runOnUiThread {
				val badge = badges[testSchool]
				messagesTotal.text = badge?.messages.toString()
				streamTotal.text = badge?.stream.toString()
				classworkTotal.text = badge?.classwork.toString()
			}
		}
		
		override fun onFailure(e: ApolloException) {
			Log.e("User Badge Fail",e.toString())
		}
		
	}
	
	
	
	private fun updateBadge(message: Int, stream: Int, classwork: Int) {
		val addBadgeCallback = object : GraphQLCall.Callback<UpdateUserBadgesMutation.Data>() {
			override fun onResponse(response: Response<UpdateUserBadgesMutation.Data>) {
				response.data()?.updateUserBadges()?.let {
					Log.d("User Badges Change", it.badges().toString())
				} ?: run {
					Log.d("User Badge", "no badges")
				}
			}
			
			override fun onFailure(e: ApolloException) {
				Log.e("User Badge Change Fail",e.toString())
			}
		}
		badges[testSchool] = BadgeData(classwork,message,stream)
		client.mutate( UpdateUserBadgesMutation.builder()
			.input(
				UpdateUserBadgesInput.builder()
					.badges(gson.toJson(badges).toString())
					.client_id(testClientId)
					.user_id(testUserId)
					.build()
			).build()).enqueue(addBadgeCallback)
	}
	
}