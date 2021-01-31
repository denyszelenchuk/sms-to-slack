package com.slacksms.app.channels

import com.slacksms.app.data.channels.Channel

interface ChannelsView {

    fun showChannels(loadedChannelsList: List<Channel>)

    fun hideChannels()
}
