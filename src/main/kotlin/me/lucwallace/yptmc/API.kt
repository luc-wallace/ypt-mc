package me.lucwallace.yptmc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json

@Serializable
data class GroupResponse(
    @SerialName("s")
    val success: Boolean,

    @SerialName("ms")
    val users: List<YPTUser>
)

@Serializable
data class YPTUser(
    @SerialName("ud")
    val userID: Int,

    @SerialName("n")
    val username: String,

    @SerialName("dl")
    val status: YPTStatus? = null
)

@Serializable
data class YPTStatus(
    @SerialName("sm")
    val msElapsed: Int,

    @SerialName("is")
    val isStudying: Boolean
)

class API(private val token: String, private val groupID: Int) {
    private var client: OkHttpClient? = null
    private var jsonOptions = Json { ignoreUnknownKeys = true }

    init {
        val headerInterceptor = Interceptor { chain ->
            val ogRequest = chain.request()
            val newRequest = ogRequest.newBuilder()
                .addHeader("User-Agent", "Dart/3.5 (dart:io)")
                .addHeader("Authorization", token)
                .build()
            chain.proceed(newRequest)
        }
        client = OkHttpClient.Builder().addInterceptor(headerInterceptor).build()
    }

    fun getStudyStatus(): List<YPTUser> {
        val req = Request.Builder()
            .url("https://pi.tgclab.com/logs/group/members?groupID=${groupID.toString()}")
            .build()

        val res = client?.newCall(req)?.execute()
        val response = jsonOptions.decodeFromString<GroupResponse>(res?.body!!.string())
        return response.users
    }
}


