package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.navigation.NavController
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.R
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
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
    val repo = (context.applicationContext as OylApplication).catalogRepository

    val products by repo.products.collectAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repo.seedDemoProducts()
        repo.syncFromServer()
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
                            Text("🛒", fontSize = 20.sp)
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
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar productos...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(24.dp),
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OylMint),
                                contentAlignment = Alignment.Center
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
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repo.changeCart(
                                                    ownerAlias,
                                                    product.id,
                                                    1
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(OylGreen)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Agregar",
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

fun productImage(productId: String): Int = when (productId) {
    "OY001" -> R.drawable.almendras
    "OY002" -> R.drawable.nueces
    "OY003" -> R.drawable.mix_premium
    "OY004" -> R.drawable.pasas_morenas
    "OY005" -> R.drawable.chia
    "OY006" -> R.drawable.cranberries
    else -> R.drawable.frutos_imagen
}