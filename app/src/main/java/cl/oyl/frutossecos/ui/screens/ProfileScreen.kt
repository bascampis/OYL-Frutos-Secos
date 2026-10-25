package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: DemoUser,
    navController: NavController,
    currentRoute: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember(context.applicationContext) {
        context.applicationContext.getSharedPreferences(
            "oyl_local_notices",
            Context.MODE_PRIVATE
        )
    }

    val preferenceKey = "enabled:${user.alias}"

    var notificationsEnabled by remember(user.alias) {
        mutableStateOf(
            preferences.getBoolean(preferenceKey, true)
        )
    }

    var notificationMessage by remember(user.alias) {
        mutableStateOf("")
    }

    fun saveNotificationPreference(enabled: Boolean) {
        preferences.edit()
            .putBoolean(preferenceKey, enabled)
            .apply()

        notificationsEnabled = enabled
    }

    fun openNotificationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    context.packageName
                )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        saveNotificationPreference(granted)

        notificationMessage = if (granted) {
            "Avisos del celular activados."
        } else {
            "Permiso denegado. Puedes habilitarlo en Ajustes de notificaciones."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                isAdmin = user.role == UserRole.ADMINISTRADOR
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(OylMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👩",
                        fontSize = 42.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(OylGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = user.alias,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    ProfileMenuRow(
                        icon = Icons.Default.Place,
                        title = "Mis direcciones"
                    ) {}

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.Payment,
                        title = "Métodos de pago"
                    ) {}

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = OylGreen
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Notificaciones",
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = if (notificationsEnabled) {
                                    "Avisos del celular activados"
                                } else {
                                    "Avisos del celular desactivados"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    saveNotificationPreference(false)

                                    NotificationManagerCompat
                                        .from(context)
                                        .cancelAll()

                                    notificationMessage =
                                        "Avisos desactivados. El historial seguirá disponible."
                                } else if (
                                    NotificationManagerCompat
                                        .from(context)
                                        .areNotificationsEnabled()
                                ) {
                                    saveNotificationPreference(true)

                                    notificationMessage =
                                        "Avisos del celular activados."
                                } else if (
                                    Build.VERSION.SDK_INT >= 33
                                ) {
                                    permissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                } else {
                                    notificationMessage =
                                        "Activa el permiso en Ajustes y vuelve a encender el interruptor."

                                    openNotificationSettings()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OylGreen
                            )
                        )
                    }

                    Text(
                        text = "El historial de la campana se conserva aunque desactives los avisos.",
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (notificationMessage.isNotBlank()) {
                        Text(
                            text = notificationMessage,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(
                        onClick = {
                            openNotificationSettings()
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Ajustes de notificaciones")
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Permisos",
                        subtitle = "Cámara, Ubicación"
                    ) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            ).apply {
                                data = Uri.fromParts(
                                    "package",
                                    context.packageName,
                                    null
                                )
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Ayuda y soporte"
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F)
                    )

                    Text(
                        text = "Cerrar sesión",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OylGreen
            )

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}