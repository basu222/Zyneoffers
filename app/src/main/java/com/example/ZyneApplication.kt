package com.example

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ZyneApplication : Application(), ImageLoaderFactory {

    companion object {
        private const val TAG = "ZyneApplication"
        const val FIREBASE_PROJECT_ID = "zyne-6a559"
        const val FIREBASE_STORAGE_BUCKET = "zyne-6a559.firebasestorage.app"
        const val FIREBASE_APP_ID = "1:939097504919:android:566487a006b8de34e7bdd2"
        const val FIREBASE_API_KEY = "AIzaSyDBlmgXO-kMNyfYU6uZ677fBEF1Ww4Lppw"
    }

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .setApplicationId(FIREBASE_APP_ID)
                    .setApiKey(FIREBASE_API_KEY)
                    .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d(TAG, "Firebase initialized with project: $FIREBASE_PROJECT_ID")
            } else {
                Log.d(TAG, "Firebase automatically initialized via Google Services Provider")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
