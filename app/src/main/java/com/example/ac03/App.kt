package com.example.ac03

import android.app.Application
import androidx.room.Room
import com.example.ac03.adapters.ArticleAdapter
import com.example.ac03.room.MyDb
import kotlinx.coroutines.GlobalScope

class App : Application() {
    lateinit var db: MyDb

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            this,
            MyDb::class.java,
            "articles-db"
        ).build()
    }
}
