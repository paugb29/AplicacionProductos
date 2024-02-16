package com.example.ac03

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ac03.adapters.ArticleAdapter
import com.example.ac03.room.ArticleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    companion object {
        lateinit var articleAdapter: ArticleAdapter
    }

    private lateinit var articleDao: ArticleDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        articleDao = (applicationContext as App).db.articleDao()
        articleAdapter = ArticleAdapter(emptyList(), this, (applicationContext as App).db.settingsDao(), articleDao)

        setupRecyclerView()

        CoroutineScope(Dispatchers.IO).launch {
            val articles = articleDao.getArticles()
            withContext(Dispatchers.Main) {
                articleAdapter.setData(articles)
            }
        }
    }


    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        articleAdapter = ArticleAdapter(emptyList(), this, (applicationContext as App).db.settingsDao(), articleDao)

        recyclerView.adapter = articleAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                CoroutineScope(Dispatchers.IO).launch {
                    val articles = articleDao.getArticles()
                    withContext(Dispatchers.Main) {
                        articleAdapter.setData(articles)
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar, menu)

        // S'utilitza seatchView y un listener per buscar
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                articleAdapter.applyFilter(newText.orEmpty(), false, false)
                return true
            }
        })

        return true
    }

    private fun showFilterDialog() {
        //Switch per els filtres
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.filtrar))
            .setItems(resources.getStringArray(R.array.opcions_filtre)) { _, which ->
                when (which) {
                    0 -> articleAdapter.applyFilter("", true, false)
                    1 -> articleAdapter.applyFilter("", false, false)
                    2 -> articleAdapter.applyFilter("", false, true)
                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                startActivityForResult(Intent(this, NouArticleActivity::class.java), 1)


                return true
            }

            R.id.ajustes -> {
                // Manejar la apertura de la pantalla de configuración
                startActivity(Intent(this, settings::class.java))
                return true
            }
            R.id.action_filter -> {
                showFilterDialog()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}