package cl.oyl.frutossecos.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Crea los clientes Retrofit una sola vez (singleton perezoso). */
object RetrofitProvider {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val catalogApi: CatalogApi by lazy { retrofit(ApiConfig.CATALOG_URL).create(CatalogApi::class.java) }
    val orderApi: OrderApi by lazy { retrofit(ApiConfig.ORDERS_URL).create(OrderApi::class.java) }
    val userApi: UserApi by lazy { retrofit(ApiConfig.USERS_URL).create(UserApi::class.java) }
    val exchangeApi: ExchangeRateApi by lazy { retrofit(ApiConfig.EXCHANGE_URL).create(ExchangeRateApi::class.java) }
}
