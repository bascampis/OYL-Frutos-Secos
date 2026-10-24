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
  require(key !in listOf("cliente", "admin") && mail != "admin@admin.cl") { "Ese correo no está disponible." }
  require(dao.byAlias(key) == null && dao.byEmail(mail) == null) { "Ese correo ya está registrado." }
  val salt = ByteArray(24).also { SecureRandom().nextBytes(it) }
  val encoded = Base64.encodeToString(salt, Base64.NO_WRAP)
  dao.add(ClientEntity(key, name.trim(), mail, phone.trim(), address.trim(), encoded, digest(encoded, password)))
  return DemoUser(key, name.trim(), UserRole.CLIENTE)
 }
 private fun digest(salt: String, password: String): String =
  Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest((salt + ":" + password).toByteArray()), Base64.NO_WRAP)
}