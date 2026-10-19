package cl.oyl.frutossecos.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class RemoteProduct(val id:String,val name:String,val category:String,val description:String?,
 val presentation:String?,val price:Int,val stock:Int,val active:Boolean)
data class RemoteOrderItem(val productId:String,val quantity:Int)
data class RemoteOrderRequest(val clientAlias:String,val fulfillment:String,val items:List<RemoteOrderItem>)
data class RemoteOrderLine(val id:Long,val productId:String,val productName:String,val quantity:Int,val unitPrice:Int)
data class RemoteOrder(val id:String,val clientAlias:String,val createdAt:String,val total:Int,
 val status:String,val fulfillment:String,val lines:List<RemoteOrderLine>)
interface OylApi {
 @GET("api/products") suspend fun products():List<RemoteProduct>
 @POST("api/orders") suspend fun order(@Body request:RemoteOrderRequest):RemoteOrder
 @GET("api/orders") suspend fun orders(@Query("clientAlias") alias:String):List<RemoteOrder>
}
object OylNetwork {
 // Emulador Android -> host del computador con Spring Boot.
 val api:OylApi by lazy {
  Retrofit.Builder().baseUrl("http://10.0.2.2:8080/")
   .addConverterFactory(GsonConverterFactory.create()).build().create(OylApi::class.java)
 }
}