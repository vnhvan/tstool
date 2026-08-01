package com.offline.saveeditor

import android.app.Application
import com.offline.saveeditor.crash.CrashReporter

class OfflineSaveEditorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
    }
}
