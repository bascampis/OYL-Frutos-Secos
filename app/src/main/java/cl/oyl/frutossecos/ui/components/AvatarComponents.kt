package cl.oyl.frutossecos.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import java.io.File

/** Avatares disponibles: id 1, 2 y 3, igual que en la pantalla de registro. */
val AVATAR_EMOJIS = listOf("👧", "👩", "🧑")

/** Muestra la foto del usuario si existe; si no, el avatar elegido. */
@Composable
fun AvatarView(avatarId: Int, photoPath: String?, size: Dp, emojiSize: TextUnit) {
    val bitmap = remember(photoPath) {
        photoPath?.takeIf { File(it).exists() }?.let { BitmapFactory.decodeFile(it) }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto de perfil",
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(OylMint),
            contentAlignment = Alignment.Center
        ) {
            Text(AVATAR_EMOJIS.getOrElse(avatarId - 1) { AVATAR_EMOJIS[1] }, fontSize = emojiSize)
        }
    }
}

/** Guarda la foto en el almacenamiento interno de la app, una por usuario. */
fun saveAvatarPhoto(context: Context, alias: String, bitmap: Bitmap): String {
    val safe = alias.replace(Regex("[^A-Za-z0-9]"), "_")
    val dir = File(context.filesDir, "avatars").apply { mkdirs() }
    val old = Regex("^avatar_${Regex.escape(safe)}_\\d+\\.jpg$")
    dir.listFiles()?.filter { old.matches(it.name) }?.forEach { it.delete() }
    val file = File(dir, "avatar_${safe}_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    return file.absolutePath
}

@Composable
fun AvatarPickerDialog(
    selectedAvatarId: Int,
    onDismiss: () -> Unit,
    onSelectEmoji: (Int) -> Unit,
    onPhotoTaken: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) onPhotoTaken(bitmap) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            cameraLauncher.launch(null)
        } else {
            permissionDenied = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar avatar", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Elige un avatar")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AVATAR_EMOJIS.forEachIndexed { index, emoji ->
                        val id = index + 1
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(OylMint)
                                .then(
                                    if (id == selectedAvatarId) Modifier.border(3.dp, OylGreen, CircleShape)
                                    else Modifier
                                )
                                .clickable { onSelectEmoji(id) },
                            contentAlignment = Alignment.Center
                        ) { Text(emoji, fontSize = 30.sp) }
                    }
                }
                OutlinedButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) cameraLauncher.launch(null)
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar foto con la cámara")
                }
                if (permissionDenied) {
                    Text(
                        "Necesitas permitir el acceso a la cámara para tomar una foto.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
