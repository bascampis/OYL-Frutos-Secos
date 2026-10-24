package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    ownerAlias: String,
    onBack: () -> Unit,
    onCart: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication

    val products by app.catalogRepository.products.collectAsState(
        initial = emptyList()
    )

    val product = products.find { it.id == productId }

    var quantity by remember(productId) { mutableIntStateOf(1) }
    var isFavorite by remember(productId) { mutableStateOf(false) }
    var addedMessage by remember(productId) { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle del Producto")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isFavorite = !isFavorite }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = "Favorito",
                            tint = if (isFavorite) {
                                Color.Red
                            } else {
                                LocalContentColor.current
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Producto no encontrado.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fotografía del producto.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OylMint
                    )
                ) {
                    Image(
                        painter = painterResource(
                            id = productImage(product.id)
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Información principal.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${product.price}",
                                style = MaterialTheme.typography.titleLarge,
                                color = OylGreen,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = "(${product.presentation})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OylMint
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OylGreen)
                                )

                                Text(
                                    text = if (product.stock > 0) {
                                        "En stock (${product.stock} un)"
                                    } else {
                                        "Sin stock"
                                    },
                                    color = OylGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Descripción.
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = product.description.ifBlank {
                                "Descripción no disponible."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Cantidad y agregado al carrito.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        ) {
                            IconButton(
                                onClick = {
                                    quantity--
                                    addedMessage = false
                                },
                                enabled = quantity > 1
                            ) {
                                Text(
                                    text = "−",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "$quantity",
                                modifier = Modifier.padding(horizontal = 12.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            IconButton(
                                onClick = {
                                    quantity++
                                    addedMessage = false
                                },
                                enabled = quantity < product.stock
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                app.catalogRepository.changeCart(
                                    ownerAlias,
                                    product.id,
                                    quantity
                                )
                                addedMessage = true
                            }
                        },
                        enabled = product.stock > 0 &&
                                quantity <= product.stock,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                            .heightIn(min = 50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OylGreen
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Agregar al carrito",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (addedMessage) {
                    Text(
                        text = "¡Producto agregado al carrito!",
                        color = OylGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Atributos del producto.
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailAttributeRow(
                            label = "Categoría",
                            value = product.category
                        )

                        HorizontalDivider()

                        DetailAttributeRow(
                            label = "Peso / Presentación",
                            value = product.presentation
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Carrito de Compras")
                }
            }
        }
    }
}

@Composable
private fun DetailAttributeRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = value,
            fontWeight = FontWeight.SemiBold
        )
    }
}