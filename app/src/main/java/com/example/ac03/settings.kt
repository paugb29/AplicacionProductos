package com.example.ac03

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ac03.MainActivity.Companion.articleAdapter
import com.example.ac03.adapters.ArticleAdapter
import com.example.ac03.room.Settings
import com.example.ac03.room.SettingsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class settings : AppCompatActivity() {
    private lateinit var ivaEditText: EditText
    private lateinit var settingsDao: SettingsDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        ivaEditText = findViewById(R.id.editTextIva)
        settingsDao = (applicationContext as App).db.settingsDao()

        val guardarButton: Button = findViewById(R.id.btnGuardar)
        guardarButton.setOnClickListener {
            guardarConfiguracionIva()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_return, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.back -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun guardarConfiguracionIva() {
        val iva = ivaEditText.text.toString().toFloatOrNull()

        if (iva != null) {
            val settings = Settings(iva = iva)
            CoroutineScope(Dispatchers.IO).launch {
                settingsDao.insertSettings(settings)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@settings, getString(R.string.conf_iva), Toast.LENGTH_SHORT).show()
                    (articleAdapter as? ArticleAdapter)?.updateIvaSetting()

                    finish()
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.iva_valid), Toast.LENGTH_SHORT).show()
        }
    }

}