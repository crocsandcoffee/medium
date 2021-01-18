package com.crocsandcoffee.contactlist.main.ui.view

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.bumptech.glide.RequestManager
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem

/**
 * @author Omid
 *
 * [PagingDataAdapter] for presenting paged data for a contact list
 *
 * The items are of type [ContactItem] which are rendered by [ContactItemViewHolder]
 *
 * @param glide [RequestManager] scoped [MainActivity] for loading contact photo thumbnails
 * @param onClick callback to be invoked when "Message" is tapped on a contact list item
 */
class ContactListAdapter(
    private val glide: RequestManager,
    private val onClick: ContactOnClick
) : PagingDataAdapter<ContactItem, ContactItemViewHolder>(ContactItem.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContactItemViewHolder.create(parent, glide, onClick)

    override fun onBindViewHolder(holder: ContactItemViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

typealias ContactOnClick = (ContactItem) -> Unit