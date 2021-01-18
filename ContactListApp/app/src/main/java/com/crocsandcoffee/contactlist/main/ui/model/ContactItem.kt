package com.crocsandcoffee.contactlist.main.ui.model

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

/**
 * @author Omid
 *
 * UI model for holding metadata for a single contact
 *
 * @param id unique ID of the contact
 * @param name full name of the contact
 * @param lastModifiedDate last modified readable timestamp
 * @param thumbnailUri optional [Uri] for the contacts thumbnail photo
 * @param phoneNumber the contacts phone number details
 * @param starred indicates if this contact is starred/favorited
 */
data class ContactItem(
    val id: Long,
    val name: String,
    val lastModifiedDate: String,
    val thumbnailUri: Uri?,
    val phoneNumber: String,
    val starred: Boolean
) {

    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContactItem>() {

            override fun areItemsTheSame(oldItem: ContactItem, newItem: ContactItem) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ContactItem, newItem: ContactItem) = oldItem == newItem

        }
    }
}
