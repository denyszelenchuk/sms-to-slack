package com.slacksms.app.data.channels

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChannelsDao {

    /**
     * Select all channels from the channels table.
     *
     * @return all channels.
     */
    @get:Query("SELECT * FROM channels")
    val channels: List<Channel>

    /**
     * Select a task by id.
     *
     * @param channelId the task id.
     * @return the task with taskId.
     */
    @Query("SELECT * FROM channels WHERE entryId = :channelId")
    fun getChannelById(channelId: String?): Channel

    /**
     * Insert a channel in the database. If the channel already exists, replace it.
     *
     * @param channel the channel to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannel(channel: Channel)

    /**
     * Update a channel.
     *
     * @param channel channel to be updated
     * @return the number of channels updated. This should always be 1.
     */
    @Update
    fun updateChannel(channel: Channel): Int

    /**
     * Delete a task by id.
     *
     * @return the number of channels deleted. This should always be 1.
     */
    @Query("DELETE FROM channels WHERE entryId = :channelId")
    fun deleteChannelById(channelId: String): Int

    /**
     * Delete all channels.
     */
    @Query("DELETE FROM channels")
    fun deleteChannels()
}
