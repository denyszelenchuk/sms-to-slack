package com.slacksms.app.data.channels

import com.slacksms.app.data.AppDatabase
import com.slacksms.app.executors.AppExecutors
import java.lang.ref.WeakReference

@Suppress("LABEL_NAME_CLASH")
class ChannelsRepository(appExecutors: AppExecutors, database: AppDatabase) {

    private var mDatabase: AppDatabase? = database
    private var mCachedChannels: List<Channel>? = null
    private val mAppExecutors: AppExecutors = appExecutors

    fun saveChannel(channel: Channel, saveChannelCallback: ChannelCallback) {
        val channelCallback = WeakReference<ChannelCallback>(saveChannelCallback)

        mAppExecutors.diskIO().execute(Runnable {
            mDatabase!!.channelsDao().insertChannel(channel)
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = channelCallback.get() ?: return@Runnable
                callback.onChannelSaved(channel)
            })
        })
    }

    fun getChannels(getChannelsCallback: ChannelsCallback) {
        val channelsCallback = WeakReference<ChannelsCallback>(getChannelsCallback)

        mAppExecutors.diskIO().execute(Runnable {
            val channels = mDatabase!!.channelsDao().channels
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = channelsCallback.get() ?: return@Runnable
                if (channels.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    mCachedChannels = channels
                    callback.onChannelsLoaded(mCachedChannels!!)
                }
            })
        })
    }

    fun deleteChannels(channelsCallback: ChannelsCallback) {
        val callback = WeakReference<ChannelsCallback>(channelsCallback)

        mAppExecutors.diskIO().execute(Runnable {
            mDatabase!!.channelsDao().deleteChannels()
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val mainThreadCallback = callback.get() ?: return@Runnable
                mainThreadCallback.onChannelsDeleted()
            })
        })
    }

    fun deleteChannelById(channelId: String, getChannelCallback: ChannelCallback) {
        val channelCallback = WeakReference<ChannelCallback>(getChannelCallback)

        mAppExecutors.diskIO().execute(Runnable {
            mDatabase!!.channelsDao().deleteChannelById(channelId)
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = channelCallback.get() ?: return@Runnable
                callback.onChannelDeleted()
            })
        })
    }


    fun getChannelById(channelId: String?, getChannelCallback: ChannelCallback) {
        val channelCallback = WeakReference<ChannelCallback>(getChannelCallback)

        mAppExecutors.diskIO().execute(Runnable {
            val channel = mDatabase!!.channelsDao().getChannelById(channelId)
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = channelCallback.get() ?: return@Runnable
                callback.onChannelLoaded(channel)
            })
        })
    }
}
