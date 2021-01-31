package com.slacksms.app.channels

import com.slacksms.app.data.channels.Channel

interface ChannelView {

    fun showChannel(channel: Channel)

    fun hideChannel()
}
