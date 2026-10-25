package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.data.local.DemoNotificationStore
import cl.oyl.frutossecos.domain.checkout.formatOrderDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    alias: String,
    onBack: () -> Unit,
    onOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val store = remember {
        DemoNotificationStore(context.applicationContext)
    }
    val noticeFlow = remember(alias) {
        store.observe(alias)
    }
    val notices by noticeFlow.collectAsState(
        initial = emptyList()
    )

    val scope = rememberCoroutineScope()

    var filter by remember { mutableStateOf("Todas") }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            error = "Puedes consultar el historial aunque no permitas avisos del sistema."
        }
    }

    fun refresh() {
        if (refreshing) return

        refreshing = true
        error = null

        scope.launch {
            try {
                if (!store.refresh()) {
                    error = "No se pudieron actualizar los avisos locales. Vuelve a intentarlo."
                }
            } finally {
                refreshing = false
            }
        }
    }

    LaunchedEffect(alias) {
        refresh()
    }

    val filtered = notices.filter {
        filter == "Todas" || it.category == filter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refresh() },
                        enabled = !refreshing
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        permission.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    } else {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            ).putExtra(
                                Settings.EXTRA_APP_PACKAGE,
                                context.packageName
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Permitir avisos en el celular")
            }

            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        ).putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            context.packageName
                        )
                    )
                }
            ) {
                Text("Abrir ajustes de notificaciones")
            }

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Todas",
                    "Pedidos",
                    "Inventario"
                ).forEach { tab ->
                    FilterChip(
                        selected = filter == tab,
                        onClick = { filter = tab },
                        label = { Text(tab) }
                    )
                }
            }

            if (refreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (filtered.isEmpty() && !refreshing) {
                Text(
                    "Todavía no tienes avisos. Los cambios nuevos aparecerán aquí."
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filtered,
                    key = { it.id }
                ) { notice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                store.markRead(alias, notice.id)
                                onOpen(notice.target)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (notice.read) {
                                    notice.title
                                } else {
                                    "● ${notice.title}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(notice.message)

                            Text(
                                text = formatOrderDate(
                                    notice.createdAt
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = if (notice.read) {
                                    "Leída"
                                } else {
                                    "Sin leer"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}