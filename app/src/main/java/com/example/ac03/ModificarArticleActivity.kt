package com.example.ac03

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ac03.room.Article
import com.example.ac03.room.ArticleDao
import com.example.ac03.room.MyDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.ac03.adapters.ArticleAdapter

class ModificarArticleActivity : AppCompatActivity() {
    private lateinit var codiEditText: EditText
    private lateinit var descripcioEditText: EditText
    private lateinit var estocEditText: EditText
    private lateinit var preuEditText: EditText
    private lateinit var familiaSpinner: Spinner
    private lateinit var articleDao: ArticleDao
    private lateinit var article: Article
    private lateinit var checkBoxEstoc: CheckBox
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nou_article)

        articleDao = (applicationContext as App).db.articleDao()
        codiEditText = findViewById(R.id.et_codi)
        descripcioEditText = findViewById(R.id.et_descripcio)
        estocEditText = findViewById(R.id.et_estoc)
        preuEditText = findViewById(R.id.et_preu)
        familiaSpinner = findViewById(R.id.spinnerFamilia)
        checkBoxEstoc = findViewById(R.id.checkBoxEstoc)

        //Opcions familia
        val familiaOptions = resources.getStringArray(R.array.familia_opcions)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, familiaOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val codiArticle = intent.getStringExtra("codiArticle") ?: ""
        val descripcio = intent.getStringExtra("descripcio") ?: ""
        val familia = intent.getStringExtra("familia") ?: ""
        val preuSenseIva = intent.getFloatExtra("preuSenseIva", 0.0f)
        val estocActivat = intent.getBooleanExtra("estocActivat", false)
        val estocActual = intent.getFloatExtra("estocActual", 0.0f)


        article = Article(
            codiArticle = codiArticle,
            descripcio = descripcio,
            familia = familia,
            preuSenseIva = preuSenseIva,
            estocActivat = estocActivat,
            estocActual = estocActual
        )
        codiEditText.setText(article.codiArticle)
        codiEditText.isEnabled = false
        descripcioEditText.setText(article.descripcio)
        estocEditText.setText(article.estocActual.toString())
        preuEditText.setText(article.preuSenseIva.toString())
        estocEditText.visibility = if (estocActivat) View.VISIBLE else View.GONE
        checkBoxEstoc.isChecked = estocActivat
        checkBoxEstoc.setOnCheckedChangeListener { _, isChecked ->
            // Canvia visibilitat si estoc activat
            estocEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        val enviarButton: Button = findViewById(R.id.enviar)
        enviarButton.setOnClickListener {
            validarYGuardarArticuloModificado()
        }
        articleAdapter = MainActivity.articleAdapter
        familiaSpinner.setSelection(adapter.getPosition(familia))

    }

    private fun validarYGuardarArticuloModificado() {
        val descripcio = descripcioEditText.text.toString()
        val estoc = estocEditText.text.toString().toFloatOrNull()
        val preu = preuEditText.text.toString().toFloatOrNull() ?: 0.0f
        val familia = familiaSpinner.selectedItem.toString()
        val estocActivat = checkBoxEstoc.isChecked

        if (descripcio.isBlank()) {
            Toast.makeText(this,  getString(R.string.descripcio_obl), Toast.LENGTH_SHORT).show()
            return
        }

        if (article.estocActivat && (estoc == null || estoc < 0)) {
            Toast.makeText(this, getString(R.string.estoc_positiu), Toast.LENGTH_SHORT).show()
            return
        }

        val articuloModificado = Article(
            codiArticle = article.codiArticle,
            descripcio = descripcio,
            estocActivat = estocActivat,
            estocActual = estoc,
            familia = if (familia.isBlank()) null else familia,
            preuSenseIva = preu
        )

        guardarArticuloModificadoEnBaseDeDatos(articuloModificado)
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
    private fun guardarArticuloModificadoEnBaseDeDatos(articulo: Article) {
        CoroutineScope(Dispatchers.IO).launch {
            // Pujar l'article actualitzat a bbdd
            articleDao.update(articulo)
            val updatedArticles = articleDao.getArticles()
            withContext(Dispatchers.Main) {
                articleAdapter.updateArticleData(updatedArticles)
                setResult(RESULT_OK, Intent())
                finish()
            }
        }
    }

}
