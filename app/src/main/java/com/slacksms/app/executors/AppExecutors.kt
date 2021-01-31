package com.slacksms.app.executors

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class AppExecutors internal constructor(private val diskIO: Executor, private val mainThread: Executor) {
    constructor() : this(DiskIOThreadExecutor(), MainThreadExecutor()) {}

    fun diskIO(): Executor {
        return diskIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
