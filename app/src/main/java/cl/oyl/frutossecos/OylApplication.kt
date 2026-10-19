package cl.oyl.frutossecos

import android.app.Application
import androidx.room.Room
import cl.oyl.frutossecos.data.local.OylDatabase
import cl.oyl.frutossecos.data.repository.LocalAuthRepository

class OylApplication: Application() {
 val database: OylDatabase by lazy { Room.databaseBuilder(this, OylDatabase::class.java, "oyl_demo.db").build() }
 val authRepository: LocalAuthRepository by lazy { LocalAuthRepository(database.clientDao()) }
}