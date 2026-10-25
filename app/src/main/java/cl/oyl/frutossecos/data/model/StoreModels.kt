package cl.oyl.frutossecos.data.model

data class LocalProduct(
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val presentation: String?,
    val price: Int,
    val stock: Int,
    val active: Boolean
)

data class LocalOrderItem(
    val productId: String,
    val quantity: Int
)

data class LocalOrderRequest(
    val clientAlias: String,
    val fulfillment: String,
    val items: List<LocalOrderItem>
)

data class LocalOrderLine(
    val id: Long,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Int
)

data class LocalOrder(
    val id: String,
    val clientAlias: String,
    val createdAt: String,
    val total: Int,
    val status: String,
    val fulfillment: String,
    val lines: List<LocalOrderLine>
)

data class ProductInputDto(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val presentation: String,
    val price: Int,
    val stock: Int,
    val active: Boolean
)

data class StockDeltaDto(
    val delta: Int
)

data class OrderStatusDto(
    val status: String
)