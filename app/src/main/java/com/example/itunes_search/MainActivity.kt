package com.example.itunes_search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.itunes_search.ui.ITunesApp
import com.example.itunes_search.ui.theme.ITunesSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ITunesSearchTheme {
                ITunesApp()
            }
        }
    }
}
