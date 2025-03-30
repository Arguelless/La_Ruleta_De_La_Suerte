package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import com.google.android.material.navigation.NavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class PrincipalActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var playButton: Button
    private lateinit var coinsText: TextView
    private lateinit var database: AppDatabase
    private lateinit var jugadorDao: JugadorDao
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)
        database = (applicationContext as App).database
        jugadorDao = database.jugadorDao()
        drawerLayout = findViewById(R.id.drawer_layout)  // Aquí asignamos el DrawerLayout
        toolbar = findViewById(R.id.toolbar)            // Aquí asignamos la Toolbar
        playButton = findViewById(R.id.playButton)
        coinsText = findViewById(R.id.coinsText)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        setSupportActionBar(toolbar)  // Esto habilita la Toolbar como ActionBar
        val disp = jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({jugadorBD ->
                coinsText.text = jugadorBD.cantidadMonedas.toString()
            }, { error ->
                // Maneja errores si los hay

                Log.e("JuegoActivity", "Error al obtener jugador", error)
            })
        disposables.add(disp)
        // Ahora configuramos el ActionBarDrawerToggle para que se muestre el icono de hamburguesa
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,       // El DrawerLayout es el que se va a controlar
            toolbar,            // La Toolbar será la que mostrará el ícono de hamburguesa
            R.string.navigation_drawer_open,  // Texto para abrir el menú
            R.string.navigation_drawer_close  // Texto para cerrar el menú
        )

        drawerLayout.addDrawerListener(toggle)  // Añadimos el listener para controlar el toggle
        toggle.syncState()

        playButton.setOnClickListener {
            jugar()
        }
    }

    private fun jugar() {
        val intent = Intent(this, JuegoActivity::class.java)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_inicio -> {
                val intent = Intent(this, PrincipalActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_historial -> {
                val intent = Intent(this, HistorialActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_ajustes -> {
                val intent = Intent(this, AjustesActivity::class.java)
                startActivity(intent)
            }
        }

        // Cerrar el Navigation Drawer después de seleccionar
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

}