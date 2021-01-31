package com.slacksms.app.executors

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DiskIOThreadExecutor internal constructor() : Executor {
    private val mDiskIO: Executor
    override fun execute(command: Runnable) {
        mDiskIO.execute(command)
    }

    init {
        mDiskIO = Executors.newSingleThreadExecutor()
    }
}
