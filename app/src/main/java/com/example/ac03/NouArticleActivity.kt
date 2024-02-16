package com.example.ac03

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ac03.adapters.ArticleAdapter
import com.example.ac03.room.Article
import com.example.ac03.room.ArticleDao
import com.example.ac03.room.MyDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NouArticleActivity : AppCompatActivity() {
    private lateinit var codiEditText: EditText
    private lateinit var descripcioEditText: EditText
    private lateinit var estocEditText: EditText
    private lateinit var preuEditText: EditText
    private lateinit var checkBoxEstoc: CheckBox
    private lateinit var familiaSpinner: Spinner
    private lateinit var articleDao: ArticleDao
    private lateinit var estocTextView: TextView
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nou_article)
        articleDao = (applicationContext as App).db.articleDao()
        codiEditText = findViewById(R.id.et_codi)
        descripcioEditText = findViewById(R.id.et_descripcio)
        estocEditText = findViewById(R.id.et_estoc)
        preuEditText = findViewById(R.id.et_preu)
        checkBoxEstoc = findViewById(R.id.checkBoxEstoc)
        estocTextView = findViewById(R.id.estoc)
        familiaSpinner = findViewById(R.id.spinnerFamilia)
        updateVisibility()
        val enviarButton: Button = findViewById(R.id.enviar)
        enviarButton.setOnClickListener {
            validarYGuardarArticulo()
        }
        checkBoxEstoc.setOnCheckedChangeListener { _, isChecked ->
            updateVisibility()
        }
        articleAdapter = ArticleAdapter(emptyList(), this, (applicationContext as App).db.settingsDao(), articleDao)

    }
    private fun updateVisibility() {
        // Actualiza visibilidad si estoc está desactivado/activado
        val isVisible = checkBoxEstoc.isChecked
        estocEditText.visibility = if (isVisible) View.VISIBLE else View.GONE
        estocTextView.visibility = if (isVisible) View.VISIBLE else View.GONE
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
    private fun validarYGuardarArticulo() {
        val codi = codiEditText.text.toString()
        val descripcio = descripcioEditText.text.toString()
        val estoc = if (checkBoxEstoc.isChecked) {
            estocEditText.text.toString().toFloatOrNull()
        } else {
            null
        }
        val preu = preuEditText.text.toString().toFloatOrNull() ?: 0.0f
        val familia = familiaSpinner.selectedItem.toString()

        if (codi.isBlank() || descripcio.isBlank() || (estoc == null && checkBoxEstoc.isChecked)) {
            Toast.makeText(this, getString(R.string.camps_obli), Toast.LENGTH_SHORT).show()
            return
        }
        if (estoc != null && estoc < 0) {
            Toast.makeText(this, getString(R.string.estoc_positiu), Toast.LENGTH_SHORT).show()
            return
        }
        // Comporvar si el codi ja existeix
        CoroutineScope(Dispatchers.IO).launch {
            val existingArticle = articleDao.getArticleByCode(codi)

            withContext(Dispatchers.Main) {
                if (existingArticle != null) {
                    // Si existeix mostrar error
                    Toast.makeText(this@NouArticleActivity,  getString(R.string.compr_codi), Toast.LENGTH_SHORT).show()
                } else {
                    // Si no crear-lo
                    val nuevoArticulo = Article(
                        codiArticle = codi,
                        descripcio = descripcio,
                        estocActivat = checkBoxEstoc.isChecked,
                        estocActual = estoc,
                        familia = if (familia.isBlank()) null else familia,
                        preuSenseIva = preu
                    )
                    guardarArticuloEnBaseDeDatos(nuevoArticulo)
                }
            }
        }
    }

    private fun guardarArticuloEnBaseDeDatos(articulo: Article) {
        CoroutineScope(Dispatchers.IO).launch {
            articleDao.insert(articulo)

            val articles = articleDao.getArticles()

            withContext(Dispatchers.Main) {
                articleAdapter.setData(articles)
                Toast.makeText(this@NouArticleActivity, getString(R.string.article_afegit), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
