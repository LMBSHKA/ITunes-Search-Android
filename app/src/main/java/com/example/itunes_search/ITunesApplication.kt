package com.example.itunes_search

import android.app.Application
import com.example.itunes_search.di.AppContainer

class ITunesApplication : Application() {
    // База и кэш имеют область жизни приложения и не пересоздаются при повороте.
    val container: AppContainer by lazy { AppContainer(this) }
}
