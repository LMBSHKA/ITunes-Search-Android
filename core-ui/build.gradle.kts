plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.itunes_search.core.ui"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }
}
