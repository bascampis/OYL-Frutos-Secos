package cl.oyl.frutossecos

import android.app.Application
import androidx.room.Room
import cl.oyl.frutossecos.data.local.OYL_MIGRATION_1_2
import cl.oyl.frutossecos.data.local.OYL_MIGRATION_2_3
import cl.oyl.frutossecos.data.local.OylDatabase
import cl.oyl.frutossecos.data.repository.CatalogRepository
import cl.oyl.frutossecos.data.repository.LocalAuthRepository
import cl.oyl.frutossecos.data.repository.LocalStore

class OylApplication : Application() {

 val database: OylDatabase by lazy {
  Room.databaseBuilder(
   this,
   OylDatabase::class.java,
   "oyl_demo.db"
  )
   .addMigrations(OYL_MIGRATION_1_2, OYL_MIGRATION_2_3)
   .build()
 }

 val authRepository: LocalAuthRepository by lazy {
  LocalAuthRepository(database.clientDao())
 }

 val catalogRepository: CatalogRepository by lazy {
  CatalogRepository(database.catalogDao())
 }

 override fun onCreate() {
  super.onCreate()

  LocalStore.initialize(database)
 }
}