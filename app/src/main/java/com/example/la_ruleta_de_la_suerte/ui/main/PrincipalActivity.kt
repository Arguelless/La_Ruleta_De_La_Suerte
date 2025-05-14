package com.example.la_ruleta_de_la_suerte.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private lateinit var navigationView: NavigationView
    private lateinit var helpButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient  // Para obtener la ubicación

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)
        database = (applicationContext as App).database
        jugadorDao = database.jugadorDao()
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        playButton = findViewById(R.id.playButton)
        coinsText = findViewById(R.id.coinsText)
        navigationView = findViewById(R.id.navigation_view)
        helpButton = findViewById(R.id.btnHelp)
        navigationView.setNavigationItemSelectedListener(this)

        // Inicializamos el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(toolbar)
        setearTextMonedas()

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔸 Solicitar permiso de ubicación al iniciar
        checkLocationPermission()

        playButton.setOnClickListener {
            jugar()
        }

        helpButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
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

        drawerLayout.closeDrawer(GravityCompat.START)
        item.isChecked = false
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onResume() {
        super.onResume()
        setearTextMonedas()
        navigationView.setCheckedItem(0)
        for (i in 0 until navigationView.menu.size) {
            navigationView.menu[i].isChecked = false
        }
    }

    override fun onStart() {
        super.onStart()
        setearTextMonedas()
        navigationView.setCheckedItem(0)
        for (i in 0 until navigationView.menu.size) {
            navigationView.menu[i].isChecked = false
        }
    }

    private fun setearTextMonedas() {
        val disp = jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorBD ->
                coinsText.text = jugadorBD.cantidadMonedas.toString()
            }, { error ->
                Log.e("JuegoActivity", "Error al obtener jugador", error)
            })
        disposables.add(disp)
    }

    // 🔸 Solicitud de permisos
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    // 🔸 Manejo del resultado de la solicitud
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permisos", "Permiso de ubicación concedido")
        } else {
            Log.d("Permisos", "Permiso de ubicación denegado")
        }
    }

    // 🔸 Obtener la ubicación actual
    private fun obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d("Ubicación", "Latitud: ${location.latitude}, Longitud: ${location.longitude}")
                        guardarUbicacion(location.latitude, location.longitude)
                    } else {
                        Log.d("Ubicación", "Ubicación no disponible")
                    }
                }
        } else {
            Log.d("Permisos", "Permiso de ubicación no concedido")
        }
    }

    // 🔸 Guardar ubicación cuando el jugador gana
    private fun guardarUbicacion(latitude: Double, longitude: Double) {
        // Aquí puedes guardar la ubicación en tu base de datos o realizar alguna acción
        Log.d("Ubicación guardada", "Latitud: $latitude, Longitud: $longitude")
    }
}
