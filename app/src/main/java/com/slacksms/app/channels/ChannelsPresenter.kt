package com.slacksms.app.channels

import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelsCallback
import com.slacksms.app.data.channels.ChannelsRepository

class ChannelsPresenter(dataSource: ChannelsRepository, view: ChannelsView) {

    private var mView: ChannelsView? = view
    private var dateRepository: ChannelsRepository = dataSource

    init {
        channelsCallback = createChannelCallback()
    }

    fun start() {
        dateRepository.getChannels(channelsCallback)
    }

    fun stop() {
        mView = null
    }

    fun createChannelCallback(): ChannelsCallback {
        return object : ChannelsCallback {
            override fun onChannelsDeleted() {
                start()
            }

            override fun onChannelsLoaded(channels: List<Channel>) {
                if (mView != null) {
                    mView!!.showChannels(channels)
                }
            }

            override fun onDataNotAvailable() {
                if (mView != null) {
                    mView!!.hideChannels()
                }
            }
        }
    }

    companion object {
        private lateinit var channelsCallback: ChannelsCallback
    }
}
