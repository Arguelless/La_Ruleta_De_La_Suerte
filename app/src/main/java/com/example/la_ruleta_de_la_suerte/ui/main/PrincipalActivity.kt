package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.la_ruleta_de_la_suerte.R

class PrincipalActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)
        drawerLayout = findViewById(R.id.drawer_layout)  // Aquí asignamos el DrawerLayout
        toolbar = findViewById(R.id.toolbar)            // Aquí asignamos la Toolbar
        playButton = findViewById(R.id.playButton)

        setSupportActionBar(toolbar)  // Esto habilita la Toolbar como ActionBar

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Si el ActionBarDrawerToggle maneja el ítem, lo abre o cierra
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun jugar() {
        val intent = Intent(this, JuegoActivity::class.java)
        startActivity(intent)
    }

}