package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cl.oyl.frutossecos.MainActivity
import cl.oyl.frutossecos.data.local.DemoNotificationStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(alias: String, onBack: () -> Unit) {
 val context = LocalContext.current
 val store = remember(alias) { DemoNotificationStore(context) }
 var items by remember(alias) { mutableStateOf(store.list(alias)) }
 var message by remember { mutableStateOf("") }
 fun notifyDemo() {
  store.append(alias, "Notificación de prueba de OYL.")
  items = store.list(alias)
  val manager = context.getSystemService(NotificationManager::class.java)
  val channel = "oyl_demo"
  if (Build.VERSION.SDK_INT >= 26)
   manager.createNotificationChannel(NotificationChannel(channel,"OYL avisos demo",NotificationManager.IMPORTANCE_DEFAULT))
  val intent = Intent(context, MainActivity::class.java)
  val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  val notification = NotificationCompat.Builder(context,channel)
   .setSmallIcon(android.R.drawable.ic_dialog_info)
   .setContentTitle("OYL Frutos Secos")
   .setContentText("Aviso de prueba para " + alias)
   .setContentIntent(pending).setAutoCancel(true).build()
  if (ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
   || Build.VERSION.SDK_INT < 33) {
   manager.notify(System.currentTimeMillis().toInt(),notification)
   message = "Aviso local generado."
  } else message = "Permiso de notificaciones denegado. Se guardó el aviso en la lista."
 }
 val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
  if (granted) notifyDemo() else { store.append(alias,"Aviso local (sin permiso de sistema)."); items = store.list(alias); message = "Permiso denegado." }
 }
 Scaffold(topBar = { TopAppBar(title = { Text("Notificaciones") }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
   verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Text("Avisos locales de demostración", style = MaterialTheme.typography.headlineSmall)
   Text("No son notificaciones push del servidor. La lista corresponde únicamente a esta cuenta.")
   Button(onClick = {
    if (Build.VERSION.SDK_INT >= 33 &&
     ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
     permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    else notifyDemo()
   }, modifier = Modifier.fillMaxWidth()) { Text("Enviar notificación de prueba") }
   if (message.isNotBlank()) Text(message)
   items.forEach { item -> Card(Modifier.fillMaxWidth()) { Text(item,Modifier.padding(16.dp)) } }
   OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
  }
 }
}