package com.crocsandcoffee.contactlist.main.ui.view

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

/**
 * @author Omid
 *
 * A [LoadStateAdapter] which is used for displaying a RecyclerView item based on [LoadState],
 * such as a loading spinner, or a retry error button.
 *
 * Used by [MainActivity] to display loading indicator when fetching contacts
 *
 */
class ContactsLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<ContactLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: ContactLoadStateViewHolder, loadState: LoadState) = holder.bind(loadState)

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState) = ContactLoadStateViewHolder.create(parent, retry)

}