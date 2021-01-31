package com.slacksms.app.channels

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.slacksms.app.R
import com.slacksms.app.data.channels.Channel


class ChannelsAdapter(private val dataSet: ArrayList<Channel>, private val context: Context) :
    RecyclerView.Adapter<ChannelsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val channelTitleTextView: TextView
        val webhookTextView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener {}
            channelTitleTextView = v.findViewById(R.id.channel_title_text_view)
            webhookTextView = v.findViewById(R.id.channel_webhook_text_view)
        }
    }

    fun deleteItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    fun deleteAllItems() {
        dataSet.clear()
        this.notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.channel_item, viewGroup, false)

        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.channelTitleTextView.text = dataSet[position].getTitle()
        viewHolder.channelTitleTextView.text = SpannableStringBuilder(viewHolder.channelTitleTextView.text).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, viewHolder.channelTitleTextView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        viewHolder.webhookTextView.text = context.getString(R.string.channel_webhook, dataSet[position].getWebhook())
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
