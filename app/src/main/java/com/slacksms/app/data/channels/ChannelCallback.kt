package com.slacksms.app.data.channels

import androidx.annotation.MainThread

/**
 * Callback called when the channel was loaded from the repository.
 */
interface ChannelCallback {

    @MainThread
    fun onChannelLoaded(channel: Channel)

    @MainThread
    fun onChannelSaved(channel: Channel)

    @MainThread
    fun onChannelDeleted()

    @MainThread
    fun onDataNotAvailable()
}
