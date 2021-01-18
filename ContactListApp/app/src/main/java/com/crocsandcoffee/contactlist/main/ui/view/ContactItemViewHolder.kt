package com.crocsandcoffee.contactlist.main.ui.view

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.crocsandcoffee.contactlist.R
import com.crocsandcoffee.contactlist.databinding.ContactListItemViewBinding
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import com.crocsandcoffee.contactlist.util.inflate
import java.util.Locale

/**
 * @author Omid
 *
 * An instance of a [RecyclerView.ViewHolder] that binds a [ContactItem] to [ContactListItemViewBinding]
 */
class ContactItemViewHolder(
    private val binding: ContactListItemViewBinding,
    private val glide: RequestManager,
    private val onClick: ContactOnClick
) : RecyclerView.ViewHolder(binding.root) {

    private val context: Context = binding.root.context

    fun bind(item: ContactItem) {

        binding.messageButton.setOnClickListener {
            onClick(item)
        }

        binding.starIcon.isVisible = item.starred

        // bind the contacts name
        binding.contactNameTv.text = item.name

        // bind the contacts last modified date
        binding.lastModifiedTv.text = String.format(
            Locale.US,
            context.getString(R.string.last_formatted_label),
            item.lastModifiedDate
        )

        // load the contacts profile icon if there is one
        glide
            .load(item.thumbnailUri)
            .fallback(R.drawable.ic_account_circle_24)
            .error(R.drawable.ic_account_circle_24)
            .into(binding.contactIconIv)
    }

    companion object {

        /**
         * Create and return an instance of [ContactItemViewHolder]
         */
        fun create(parent: ViewGroup, glide: RequestManager, onClick: ContactOnClick): ContactItemViewHolder {
            return ContactItemViewHolder(
                ContactListItemViewBinding.bind(parent.inflate(R.layout.contact_list_item_view)),
                glide,
                onClick
            )
        }
    }
}