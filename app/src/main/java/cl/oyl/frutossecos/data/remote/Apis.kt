package cl.oyl.frutossecos.data.remote

import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.model.LocalOrderRequest
import cl.oyl.frutossecos.data.model.LocalProduct
import cl.oyl.frutossecos.data.model.OrderStatusDto
import cl.oyl.frutossecos.data.model.ProductInputDto
import cl.oyl.frutossecos.data.model.StockDeltaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** catalog-service (puerto 8081) */
interface CatalogApi {
    @GET("api/products") suspend fun products(): List<LocalProduct>
    @GET("api/products/{id}") suspend fun product(@Path("id") id: String): LocalProduct
    @POST("api/products") suspend fun create(@Body body: ProductInputDto): LocalProduct
    @PUT("api/products/{id}") suspend fun update(@Path("id") id: String, @Body body: ProductInputDto): LocalProduct
    @DELETE("api/products/{id}") suspend fun delete(@Path("id") id: String): Response<Unit>
    @PATCH("api/products/{id}/stock") suspend fun adjustStock(@Path("id") id: String, @Body body: StockDeltaDto): LocalProduct
}

/** order-service (puerto 8082) */
interface OrderApi {
    @POST("api/orders") suspend fun create(@Body body: LocalOrderRequest): LocalOrder
    @GET("api/orders") suspend fun all(): List<LocalOrder>
    @GET("api/orders") suspend fun byClient(@Query("clientAlias") alias: String): List<LocalOrder>
    @PATCH("api/orders/{id}/status") suspend fun setStatus(@Path("id") id: String, @Body body: OrderStatusDto): LocalOrder
    @DELETE("api/orders/{id}") suspend fun cancel(@Path("id") id: String): Response<Unit>
}

/** user-service (puerto 8083) */
interface UserApi {
    @POST("api/users/register") suspend fun register(@Body body: RegisterBody): RemoteUser
    @POST("api/users/login") suspend fun login(@Body body: LoginBody): RemoteUser
    @GET("api/users") suspend fun users(): List<RemoteUser>
    @PUT("api/users/{id}") suspend fun update(@Path("id") id: Long, @Body body: UpdateUserBody): RemoteUser
    @DELETE("api/users/{id}") suspend fun delete(@Path("id") id: Long): Response<Unit>
}

/** API externa: tipo de cambio USD -> CLP */
interface ExchangeRateApi {
    @GET("v6/latest/USD") suspend fun latestUsd(): ExchangeRateResponse
}
