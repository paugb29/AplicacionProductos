package com.example.ac03.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ac03.MainActivity.Companion.articleAdapter
import com.example.ac03.R
import com.example.ac03.room.Article
import com.example.ac03.room.SettingsDao
import com.example.ac03.ModificarArticleActivity
import com.example.ac03.room.ArticleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleAdapter(
    private var articles: List<Article>,
    private val context: Context,
    private val settingsDao: SettingsDao,
    private val articleDao: ArticleDao
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codi: TextView = itemView.findViewById(R.id.tv_codi)
        val descripcio: TextView = itemView.findViewById(R.id.tv_desc)
        val preu: TextView = itemView.findViewById(R.id.tv_preu)
        val preuIva: TextView = itemView.findViewById(R.id.tv_preu_iva)
        val estoc: TextView = itemView.findViewById(R.id.tv_estoc)
        val familia: TextView = itemView.findViewById(R.id.tv_familia)
        val familia_nom: TextView = itemView.findViewById(R.id.familia)
        val estoc_nom: TextView = itemView.findViewById(R.id.estoc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.articles, parent, false)
        return ViewHolder(itemView)
    }

    fun setData(articles: List<Article>) {
        this.articles = articles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    fun applyFilter(filterText: String, showOnlyEstocActivated: Boolean, sortByDescription: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredArticles = when {
                showOnlyEstocActivated && filterText.isNotEmpty() ->
                    articleDao.getFilteredArticlesWithEstocActivated(filterText)
                showOnlyEstocActivated ->
                    articleDao.getArticlesWithEstocActivated()
                filterText.isNotEmpty() -> {
                    if (sortByDescription) {
                        articleDao.searchArticlesByDescription(filterText)
                    } else {
                        articleDao.getFilteredArticles(filterText)
                    }
                }
                else ->
                    articleDao.getArticlesSortedByCode()
            }
            withContext(Dispatchers.Main) {
                setData(filteredArticles)
            }
        }
    }

    private fun showDeleteConfirmationDialog(article: Article) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.alerta_confirmar))
            .setMessage(context.getString(R.string.alerta_eliminar))
            .setPositiveButton(context.getString(R.string.si)) { _, _ ->
                deleteArticle(article)
            }
            .setNegativeButton(context.getString(R.string.no), null)

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteArticle(article: Article) {
        CoroutineScope(Dispatchers.IO).launch {
            articleDao.delete(article)
            val updatedArticles = articleDao.getArticles()

            withContext(Dispatchers.Main) {
                setData(updatedArticles)
            }
        }
    }

    fun updateIvaSetting() {
        notifyDataSetChanged()
    }

    fun updateArticleData(articles: List<Article>) {
        this.articles = articles
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentArticle = articles[position]

        holder.codi.text = currentArticle.codiArticle
        holder.descripcio.text = currentArticle.descripcio
        holder.preu.text = currentArticle.preuSenseIva.toString()

        // Amagar estoc si no está activado
        if (!currentArticle.estocActivat) {
            holder.estoc.visibility = View.GONE
            holder.estoc_nom.visibility = View.GONE
        } else {
            holder.estoc.visibility = View.VISIBLE
            holder.estoc_nom.visibility = View.VISIBLE
            holder.estoc.text = currentArticle.estocActual.toString()
        }

        holder.itemView.findViewById<ImageView>(R.id.eliminar).setOnClickListener {
            showDeleteConfirmationDialog(currentArticle)
        }

        currentArticle.familia?.let { familia ->
            holder.familia.text = familia
            when (familia) {
                "ROBA" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFamilia1))
                "ELECTRÓNICA" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFamilia2))
                "ALIMENTACIÓ" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFamilia3))
                else -> holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDefault))
            }
            holder.familia.visibility = View.VISIBLE
            holder.familia_nom.visibility = View.VISIBLE
        } ?: run {
            holder.familia.visibility = View.GONE
            holder.familia_nom.visibility = View.GONE
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDefault))
        }

        CoroutineScope(Dispatchers.IO).launch {
            val iva = settingsDao.getSettings()?.iva ?: 0.0f
            val precioConIva = currentArticle.preuSenseIva * (1 + iva / 100)
            withContext(Dispatchers.Main) {
                holder.preuIva.text = precioConIva.toString()
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ModificarArticleActivity::class.java)
            intent.putExtra("codiArticle", currentArticle.codiArticle)
            intent.putExtra("descripcio", currentArticle.descripcio)
            intent.putExtra("familia", currentArticle.familia)
            intent.putExtra("preuSenseIva", currentArticle.preuSenseIva)
            intent.putExtra("estocActivat", currentArticle.estocActivat)
            intent.putExtra("estocActual", currentArticle.estocActual)
            (context as Activity).startActivityForResult(intent, 1)
        }
    }
}
