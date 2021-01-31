package com.slacksms.app.data.channels

import androidx.annotation.MainThread

/**
 * Callback called when the channels was loaded from the repository.
 */
interface ChannelsCallback {

    @MainThread
    fun onChannelsLoaded(channels: List<Channel>)

    @MainThread
    fun onChannelsDeleted()

    @MainThread
    fun onDataNotAvailable()
}
