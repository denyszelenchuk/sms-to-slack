package com.slacksms.app.channels

import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelCallback
import com.slacksms.app.data.channels.ChannelsRepository

class ChannelPresenter(dataSource: ChannelsRepository, view: ChannelView) {

    private var mView: ChannelView? = view
    private var dateRepository: ChannelsRepository = dataSource

    init {
        channelCallback = createChannelCallback()
    }

    fun start(channelId: String?) {
        dateRepository.getChannelById(channelId, channelCallback)
    }

    fun stop() {
        mView = null
    }

    fun createChannelCallback(): ChannelCallback {
        return object : ChannelCallback {
            override fun onChannelDeleted() {
                if (mView != null) {
                    mView!!.hideChannel()
                }
            }

            override fun onChannelSaved(channel: Channel) {
                channel.getTitle()
            }


            override fun onChannelLoaded(channel: Channel) {
                if (mView != null) {
                    mView!!.showChannel(channel)
                }
            }

            override fun onDataNotAvailable() {
                // do nothing
            }
        }
    }

    companion object {
        private lateinit var channelCallback: ChannelCallback
    }
}
