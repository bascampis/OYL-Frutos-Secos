package cl.oyl.frutossecos.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.R
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import coil.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    ownerAlias: String,
    isAdmin: Boolean,
    navController: NavController,
    currentRoute: String,
    onBack: () -> Unit,
    onCart: () -> Unit,
    onProductClick: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication
    val repo = app.catalogRepository

    val products by repo.products.collectAsState(initial = emptyList())

    val cartFlow = remember(repo, ownerAlias) {
        repo.cart(ownerAlias)
    }

    val cartItems by key(ownerAlias) {
        cartFlow.collectAsState(initial = emptyList())
    }

    val cartCount = cartItems
        .filter { it.quantity > 0 }
        .distinctBy { it.productId }
        .size

    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }

    val scope = rememberCoroutineScope()

    var cartMessage by remember(ownerAlias) {
        mutableStateOf<String?>(null)
    }
    var messageIsError by remember(ownerAlias) {
        mutableStateOf(false)
    }
    var messageVersion by remember(ownerAlias) {
        mutableIntStateOf(0)
    }
    var addingToCart by remember(ownerAlias) {
        mutableStateOf(false)
    }

    LaunchedEffect(ownerAlias, messageVersion) {
        if (cartMessage != null) {
            delay(2500)
            cartMessage = null
        }
    }

    LaunchedEffect(repo) {
        repo.seedDemoProducts()
    }

    val filteredProducts = products.filter { product ->
        val matchesCategory =
            category == "Todos" ||
                    product.category.equals(category, ignoreCase = true)

        val matchesQuery =
            product.name.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)

        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catálogo de Productos",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (!isAdmin) {
                        IconButton(onClick = onCart) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (cartCount > 99) {
                                                    "99+"
                                                } else {
                                                    cartCount.toString()
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito: $cartCount productos"
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                isAdmin = isAdmin
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedContent(
                targetState = cartMessage to messageIsError,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    (
                            slideInVertically(
                                animationSpec = tween(250),
                                initialOffsetY = { -it / 2 }
                            ) + fadeIn(tween(250))
                            ) togetherWith (
                            slideOutVertically(
                                animationSpec = tween(180),
                                targetOffsetY = { -it / 2 }
                            ) + fadeOut(tween(180))
                            )
                },
                label = "Aviso animado del carrito"
            ) { notice ->
                val message = notice.first
                val isError = notice.second

                if (message != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isError) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar productos...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Todos",
                    "Frutos secos",
                    "Semillas",
                    "Deshidratados"
                ).forEach { value ->
                    FilterChip(
                        selected = category == value,
                        onClick = { category = value },
                        label = { Text(value) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(
                    items = filteredProducts,
                    key = { it.id }
                ) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onProductClick(product.id)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F1E8)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val fallback = painterResource(
                                id = productImage(product.id)
                            )

                            AsyncImage(
                                model = product.imageUrl
                                    ?.trim()
                                    ?.takeIf { it.isNotEmpty() },
                                contentDescription = product.name,
                                placeholder = fallback,
                                error = fallback,
                                fallback = fallback,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OylMint),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "$${product.price}",
                                        color = OylGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = "Stock: ${product.stock} un",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                if (!isAdmin) {
                                    var clickAnimation by remember(
                                        ownerAlias,
                                        product.id
                                    ) {
                                        mutableStateOf(false)
                                    }

                                    val interaction = remember(product.id) {
                                        MutableInteractionSource()
                                    }

                                    val hovered by interaction.collectIsHoveredAsState()
                                    val pressed by interaction.collectIsPressedAsState()

                                    val buttonEnabled =
                                        !addingToCart && product.stock > 0

                                    val buttonScale by animateFloatAsState(
                                        targetValue = when {
                                            clickAnimation -> 0.75f
                                            pressed -> 0.82f
                                            hovered && buttonEnabled -> 0.90f
                                            else -> 1f
                                        },
                                        animationSpec = spring(
                                            dampingRatio = 0.65f,
                                            stiffness = 500f
                                        ),
                                        label = "Escala del botón agregar"
                                    )

                                    IconButton(
                                        interactionSource = interaction,
                                        enabled = buttonEnabled,
                                        onClick = {
                                            if (!addingToCart) {
                                                addingToCart = true
                                                clickAnimation = true

                                                scope.launch {
                                                    delay(180)
                                                    clickAnimation = false
                                                }

                                                scope.launch {
                                                    try {
                                                        val dao = app.database.catalogDao()

                                                        val before = dao.getCartItem(
                                                            ownerAlias,
                                                            product.id
                                                        )?.quantity ?: 0

                                                        repo.changeCart(
                                                            ownerAlias,
                                                            product.id,
                                                            1
                                                        )

                                                        val after = dao.getCartItem(
                                                            ownerAlias,
                                                            product.id
                                                        )?.quantity ?: 0

                                                        messageIsError = after <= before

                                                        cartMessage = if (after > before) {
                                                            "Añadido: ${product.name}"
                                                        } else {
                                                            "No puedes agregar más unidades de ${product.name}."
                                                        }

                                                        messageVersion++
                                                    } catch (e: CancellationException) {
                                                        throw e
                                                    } catch (_: Exception) {
                                                        messageIsError = true
                                                        cartMessage =
                                                            "No se pudo añadir ${product.name}."
                                                        messageVersion++
                                                    } finally {
                                                        addingToCart = false
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .hoverable(
                                                interactionSource = interaction,
                                                enabled = buttonEnabled
                                            )
                                            .graphicsLayer {
                                                scaleX = buttonScale
                                                scaleY = buttonScale
                                            }
                                            .clip(CircleShape)
                                            .background(
                                                if (product.stock > 0) {
                                                    OylGreen
                                                } else {
                                                    MaterialTheme.colorScheme.outline
                                                }
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Agregar ${product.name}",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun productImage(productId: String): Int {
    return when (productId) {
        "OY001" -> R.drawable.almendras
        "OY002" -> R.drawable.nueces
        "OY003" -> R.drawable.mix_premium
        "OY004" -> R.drawable.pasas_morenas
        "OY005" -> R.drawable.chia
        "OY006" -> R.drawable.cranberries
        else -> R.drawable.frutos_imagen
    }
}