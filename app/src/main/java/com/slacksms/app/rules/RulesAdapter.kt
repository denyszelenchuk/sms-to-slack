package com.slacksms.app.rules

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
import com.slacksms.app.data.rules.Rule


class RulesAdapter(private val dataSet: ArrayList<Rule>, private val context: Context) :
    RecyclerView.Adapter<RulesAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val titleTextView: TextView
        val channelTitleTextView: TextView
        val senderNameTextView: TextView
        val phoneNumberTextView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener {}
            titleTextView = v.findViewById(R.id.rule_title_text_view)
            senderNameTextView = v.findViewById(R.id.rule_sender_name_text_view)
            phoneNumberTextView = v.findViewById(R.id.rule_phone_number_text_view)
            channelTitleTextView = v.findViewById(R.id.rule_channel_title_text_view)
        }
    }

    fun deleteItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rule_item, viewGroup, false)

        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.titleTextView.text = dataSet[position].getTitle()
        viewHolder.titleTextView.text = SpannableStringBuilder(viewHolder.titleTextView.text).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, viewHolder.titleTextView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        viewHolder.channelTitleTextView.text = context.getString(
            R.string.rule_channel_title,
            dataSet[position].getChannelTitle())
        viewHolder.senderNameTextView.text = context.getString(
            R.string.rule_sender_name,
            dataSet[position].getSender())
        viewHolder.phoneNumberTextView.text = context.getString(
            R.string.rule_phone_number,
            dataSet[position].getPhoneNumber())
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    companion object {
        private val TAG = "RulesAdapter"
    }
}
