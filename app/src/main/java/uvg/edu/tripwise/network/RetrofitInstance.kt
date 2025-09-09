package uvg.edu.tripwise.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log

object RetrofitInstance {
    private const val BASE_URL = "https://trip-wise-backend.vercel.app/api/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("RetrofitInstance", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: UserApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Adding OkHttp client with logging
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApiService::class.java)
    }

    val PropertyApi: PropertyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Adding OkHttp client with logging
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PropertyApiService::class.java)
    }
}
