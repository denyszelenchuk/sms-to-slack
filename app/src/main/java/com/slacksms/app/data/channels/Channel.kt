package com.slacksms.app.data.channels

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "channels")
class Channel(@Nullable
              @ColumnInfo(name = "title")
              private var title: String, @Nullable
              @ColumnInfo(name = "webhook")
              private var webhook: String?) {

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

    fun setTitle(title: String) {
        this.title = title
    }

    fun setWebhook(webhook: String) {
        this.webhook = webhook
    }

    fun getWebhook(): String? {
        return webhook
    }

    override fun toString(): String {
        return title
    }

    init {
        this.id = UUID.randomUUID().toString()
    }
}