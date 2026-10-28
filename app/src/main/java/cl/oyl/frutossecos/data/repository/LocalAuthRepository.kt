package cl.oyl.frutossecos.data.repository

import android.util.Base64
import cl.oyl.frutossecos.data.local.*
import cl.oyl.frutossecos.domain.model.*
import java.security.MessageDigest
import java.security.SecureRandom

/** Solo MVP local. Hash SHA-256 con sal para demo; no equivale a autenticación de producción. */
class LocalAuthRepository(private val dao: ClientDao): AuthRepository {
 private val demo = DemoAuthRepository()
 override suspend fun signIn(role: UserRole, alias: String, password: String): DemoUser? {
  demo.signIn(role, alias, password)?.let { return it }
  val key = alias.trim().lowercase()
  val client = dao.byAlias(key) ?: dao.byEmail(key) ?: return null
  if (!MessageDigest.isEqual(digest(client.salt, password).toByteArray(), client.hash.toByteArray())) return null
  return DemoUser(client.alias, client.name, UserRole.CLIENTE)
 }
 suspend fun register(name: String, alias: String, email: String, phone: String, address: String, password: String): DemoUser {
  val key = alias.trim().lowercase()
  val mail = email.trim().lowercase()
  require(key !in listOf("cliente", "admin") && mail !in listOf("admin@admin.cl", "cliente@cliente.cl")) { "Ese correo no está disponible." }
  require(dao.byAlias(key) == null && dao.byEmail(mail) == null) { "Ese correo ya está registrado." }
  val salt = ByteArray(24).also { SecureRandom().nextBytes(it) }
  val encoded = Base64.encodeToString(salt, Base64.NO_WRAP)
  dao.add(ClientEntity(key, name.trim(), mail, phone.trim(), address.trim(), encoded, digest(encoded, password)))
  return DemoUser(key, name.trim(), UserRole.CLIENTE)
 }

 // Perfil del cliente: lectura y edición de teléfono y dirección.
 suspend fun getProfile(alias: String): ClientEntity? = dao.byAlias(alias.trim().lowercase())

 suspend fun updateProfile(alias: String, name: String, phone: String, address: String) =
  dao.updateProfile(alias.trim().lowercase(), name.trim(), phone.trim(), address.trim())

 // El cliente demo no está en la tabla de clientes: se crea su fila (sin contraseña) la primera vez.
 suspend fun ensureProfile(alias: String, displayName: String) {
  val key = alias.trim().lowercase()
  if (dao.byAlias(key) != null) return
  val admin = key == "admin@admin.cl"
  dao.add(ClientEntity(key, displayName, key, if (admin) "+56912341234" else "", if (admin) "Santiago" else "", "", ""))
 }

 suspend fun updateContact(alias: String, name: String, email: String, phone: String) {
  val key = alias.trim().lowercase()
  val mail = email.trim().lowercase()
  require(mail == key || mail !in listOf("admin@admin.cl", "cliente@cliente.cl")) { "Ese correo no está disponible." }
  val other = dao.byEmail(mail)
  require(other == null || other.alias == key) { "Ese correo ya está registrado." }
  dao.updateContact(key, name.trim(), mail, phone.trim())
 }

 private fun digest(salt: String, password: String): String =
  Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest((salt + ":" + password).toByteArray()), Base64.NO_WRAP)
}