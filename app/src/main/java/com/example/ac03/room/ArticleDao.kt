package com.example.ac03.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {


    @Query("SELECT * FROM article ORDER BY codi_article ASC")
    fun getArticles(): MutableList<Article>

    @Query("SELECT * FROM article WHERE codi_article = :code")
    fun getArticleByCode(code: String): Article?

    @Query("SELECT * FROM article WHERE descripcio LIKE '%' || :filterText || '%' ORDER BY codi_article ASC")
    fun getFilteredArticles(filterText: String): List<Article>

    @Query("SELECT * FROM article WHERE estoc_activat = 1 ORDER BY codi_article ASC")
    fun getArticlesWithEstocActivated(): List<Article>

    @Query("SELECT * FROM article WHERE descripcio LIKE '%' || :filterText || '%' AND estoc_activat = 1 ORDER BY codi_article ASC")
    fun getFilteredArticlesWithEstocActivated(filterText: String): List<Article>

    @Query("SELECT * FROM article ORDER BY descripcio ASC, codi_article ASC")
    fun getArticlesSortedByDescription(): List<Article>

    @Query("SELECT * FROM article WHERE descripcio LIKE '%' || :filterText || '%' ORDER BY codi_article ASC")
    fun searchArticlesByDescription(filterText: String): List<Article>

    @Query("SELECT * FROM article ORDER BY CAST(codi_article AS INTEGER) ASC")
    fun getArticlesSortedByCode(): List<Article>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(article: Article): Long

    @Delete
    fun delete(article: Article)

    @Update
    fun update(article: Article)

    @Query("DELETE FROM Article")
    fun deleteAll()

    @Query("select coalesce(max(codi_article),0) from article")
    fun getLastId(): Int
}
