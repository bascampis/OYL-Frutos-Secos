package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import cl.oyl.frutossecos.ui.components.saveAvatarPhoto
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.local.UserAvatarEntity
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.validation.RegisterErrors
import cl.oyl.frutossecos.domain.validation.RegisterValidator
import cl.oyl.frutossecos.ui.components.AVATAR_EMOJIS
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import cl.oyl.frutossecos.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch

@Composable
private fun FormField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = { Icon(icon, contentDescription = null) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(vm: RegisterViewModel, onBack: () -> Unit, onDone: (DemoUser) -> Unit) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication
    val scope = rememberCoroutineScope()
    var selectedAvatar by remember { mutableIntStateOf(2) }
    var attempted by remember { mutableStateOf(false) }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) { photo = bitmap; cameraError = null }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) { cameraError = null; cameraLauncher.launch(null) }
        else cameraError = "Necesitas permitir el acceso a la cámara para tomar una foto."
    }

    fun openCamera() {
        val ok = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (ok) cameraLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val errors = if (attempted) {
        RegisterValidator.validate(s.name, s.email, s.phone, s.address, s.password, s.confirm)
    } else RegisterErrors()
    // Errores que vienen del guardado (por ejemplo, correo ya registrado).
    val emailTaken = s.error?.takeIf { it.contains("correo", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar grande: cambia según el avatar elegido abajo.
// Avatar grande: muestra la foto si existe, o el avatar elegido abajo.
            Box(modifier = Modifier.size(100.dp)) {
                val shown = photo
                if (shown != null) {
                    Image(
                        bitmap = shown.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { openCamera() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(OylMint)
                            .clickable { openCamera() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(AVATAR_EMOJIS.getOrElse(selectedAvatar - 1) { "👩" }, fontSize = 48.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(OylGreen)
                        .clickable { openCamera() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            cameraError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (photo != null) {
                TextButton(onClick = { photo = null }) { Text("Quitar foto") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FormField(s.name, { vm.edit("name", it) }, "Nombre completo", Icons.Outlined.Person, errors.name)
            FormField(
                s.email, { vm.edit("email", it) }, "Correo electrónico", Icons.Outlined.Email,
                errors.email ?: emailTaken, KeyboardType.Email
            )
            FormField(
                s.phone, { vm.edit("phone", it) }, "Teléfono", Icons.Outlined.Phone,
                errors.phone, KeyboardType.Phone, placeholder = "+569 1234 5678"
            )
            FormField(s.address, { vm.edit("address", it) }, "Dirección (opcional)", Icons.Outlined.LocationOn, errors.address)
            FormField(
                s.password, { vm.edit("password", it) }, "Contraseña", Icons.Outlined.Lock,
                errors.password, KeyboardType.Password, isPassword = true
            )
            FormField(
                s.confirm, { vm.edit("confirm", it) }, "Confirmar contraseña", Icons.Outlined.Lock,
                errors.confirm, KeyboardType.Password, isPassword = true
            )

            // Selección de avatar
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Selecciona un avatar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1, 2, 3).forEach { index ->
                        val isSelected = selectedAvatar == index
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) OylMint else MaterialTheme.colorScheme.surfaceVariant)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, OylGreen, CircleShape) else Modifier
                                )
                                .clickable { selectedAvatar = index; photo = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(AVATAR_EMOJIS[index - 1], fontSize = 28.sp)
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(OylGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (s.error != null && emailTaken == null) {
                Text(s.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    attempted = true
                    val check = RegisterValidator.validate(
                        s.name, s.email, s.phone, s.address, s.password, s.confirm
                    )
                    if (check.isValid) {
                        vm.submit { newUser ->
                            scope.launch {
                                val path = photo?.let { saveAvatarPhoto(context, newUser.alias, it) }
                                app.database.avatarDao().save(
                                    UserAvatarEntity(newUser.alias, selectedAvatar, path)
                                )
                                onDone(newUser)
                            }
                        }
                    }
                },
                enabled = !s.busy,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OylGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (s.busy) "Creando..." else "Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}