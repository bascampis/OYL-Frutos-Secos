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
  if (role != UserRole.CLIENTE) return null
  val key = alias.trim().lowercase()
  val client = dao.byAlias(key) ?: dao.byEmail(key) ?: return null
  if (!MessageDigest.isEqual(digest(client.salt, password).toByteArray(), client.hash.toByteArray())) return null
  return DemoUser(client.alias, client.name, UserRole.CLIENTE)
 }
 suspend fun register(name: String, alias: String, email: String, phone: String, address: String, password: String): DemoUser {
  val key = alias.trim().lowercase()
  val mail = email.trim().lowercase()
  require(key !in listOf("cliente", "admin", "admin@admin.cl")) { "Alias reservado." }
  require(dao.byAlias(key) == null) { "El alias ya está ocupado." }
  require(dao.byEmail(mail) == null) { "El correo ya está registrado." }
  val salt = ByteArray(24).also { SecureRandom().nextBytes(it) }
  val encoded = Base64.encodeToString(salt, Base64.NO_WRAP)
  dao.add(ClientEntity(key, name.trim(), mail, phone.trim(), address.trim(), encoded, digest(encoded, password)))
  return DemoUser(key, name.trim(), UserRole.CLIENTE)
 }
 suspend fun getProfile(alias: String) = dao.byAlias(alias)
 suspend fun updateProfile(alias: String, name: String, phone: String, address: String) =
  dao.updateProfile(alias, name.trim(), phone.trim(), address.trim())
 private fun digest(salt: String, password: String): String =
  Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest((salt + ":" + password).toByteArray()), Base64.NO_WRAP)
}