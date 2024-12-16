package com.dsw.pam.bookshelf

import android.app.Application
import com.dsw.pam.bookshelf.data.AppContainer
import com.dsw.pam.bookshelf.data.DefaultAppContainer
import com.google.firebase.FirebaseApp

class BooksApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
        FirebaseApp.initializeApp(this)
    }
}