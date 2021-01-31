package com.slacksms.app.data.rules

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "rules")
class Rule(@ColumnInfo(name = "title")
           private var title: String, @ColumnInfo(name = "sender")
           private var sender: String, @ColumnInfo(name = "phoneNumber")
           private var phoneNumber: String, @ColumnInfo(name = "channelTitle")
           private var channelTitle: String?, @ColumnInfo(name = "channelId")
           private var channelId: String?, @ColumnInfo(name = "channelWebhook")
           private var channelWebhook: String?) {

    @PrimaryKey
    @ColumnInfo(name = "entryId")
    @NonNull
    private var id: String? = null

    fun getId(): String? {
        return id
    }

    @NonNull
    fun setId(id: String) {
        this.id = id
    }

    fun getTitle(): String {
        return title
    }

    fun getChannelId(): String? {
        return channelId
    }

    fun setChannelId(channelId: String?) {
        this.channelId = channelId
    }

    fun getChannelWebhook(): String? {
        return channelWebhook
    }

    fun setChannelWebhook(channelWebhook: String?) {
        this.channelWebhook = channelWebhook
    }

    fun getChannelTitle(): String? {
        return channelTitle
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setSender(sender: String) {
        this.sender = sender
    }

    fun getSender(): String {
        return sender
    }

    fun setPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    fun getPhoneNumber(): String {
        return phoneNumber
    }

    override fun toString(): String {
        return "Rule with title $title"
    }

    init {
        this.id = UUID.randomUUID().toString()
    }
}
