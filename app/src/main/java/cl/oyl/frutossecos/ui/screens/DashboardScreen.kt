package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.oyl.frutossecos.R
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: DemoUser,
    navController: NavController,
    currentRoute: String,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onCatalog: () -> Unit,
    onCart: () -> Unit,
    onQr: () -> Unit,
    onMap: () -> Unit,
    onOrders: () -> Unit,
    onAdmin: () -> Unit,
    onNotifications: () -> Unit
) {
    val admin = user.role == UserRole.ADMINISTRADOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "¡Hola, ${user.displayName.substringBefore(" ")}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Qué bueno verte de nuevo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNotifications) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }

                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                isAdmin = admin
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Banner principal con imagen y texto.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Imagen de fondo
                    Image(
                        painter = painterResource(
                            id = R.drawable.banner
                        ),
                        contentDescription = "Frutos secos OYL",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Capa oscura para destacar el texto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )

                    // Texto encima de la imagen
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Compra saludable,",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "vida mejor",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Accesos principales.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = "📦",
                    title = "Catálogo",
                    onClick = onCatalog
                )

                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = if (admin) "📊" else "📍",
                    title = if (admin) "Inventario" else "Mapa de tiendas",
                    onClick = if (admin) onAdmin else onMap
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = "📋",
                    title = "Mis pedidos",
                    onClick = onOrders
                )

                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = "🏷️",
                    title = "Promociones",
                    onClick = onCatalog
                )
            }

            // Productos destacados.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos destacados",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onCatalog) {
                    Text(
                        text = "Ver todos",
                        color = OylGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeaturedProductCard(
                    productId = "OY003",
                    name = "Mix Premium",
                    price = "$3.990",
                    onClick = onCatalog
                )

                FeaturedProductCard(
                    productId = "OY001",
                    name = "Almendras",
                    price = "$2.990",
                    onClick = onCatalog
                )

                FeaturedProductCard(
                    productId = "OY002",
                    name = "Nueces",
                    price = "$3.490",
                    onClick = onCatalog
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAbout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acerca de esta demostración")
            }
        }
    }
}

@Composable
private fun ActionGridCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    val cardColor = when (title) {
        "Catálogo" -> Color(0xFFE4F1E3)
        "Mapa de tiendas" -> Color(0xFFE5F0E9)
        "Mis pedidos" -> Color(0xFFF5EBDC)
        "Promociones" -> Color(0xFFF4E6D9)
        else -> Color(0xFFE4F1E3)
    }

    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 30.sp
                )

                Text(
                    text = "↗",
                    fontSize = 22.sp,
                    color = OylGreen
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF253B2D)
                )

                Text(
                    text = when (title) {
                        "Catálogo" -> "Explora productos"
                        "Mapa de tiendas" -> "Encuéntranos"
                        "Mis pedidos" -> "Revisa tus compras"
                        "Promociones" -> "Ofertas especiales"
                        "Inventario" -> "Gestiona productos"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF637165)
                )
            }
        }
    }
}

@Composable
private fun FeaturedProductCard(
    productId: String,
    name: String,
    price: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(145.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F1E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(
                    id = productImage(productId)
                ),
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )

            Text(
                text = price,
                color = OylGreen,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}