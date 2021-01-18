package com.crocsandcoffee.contactlist.main.ui.view

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.crocsandcoffee.contactlist.R
import com.crocsandcoffee.contactlist.databinding.ContactLoadStateItemViewBinding
import com.crocsandcoffee.contactlist.util.inflate

/**
 * @author Omid
 *
 * [RecyclerView.ViewHolder] for binding a [LoadState] from [ContactsLoadStateAdapter]
 *
 * The [LoadState] describes the current loading state of a PagedList load. This can be observed
 * for UI purposes to display a message or spinner for example, when loading or an error occurs.
 *
 * @see [ContactsLoadStateAdapter] for more details.
 */
class ContactLoadStateViewHolder(
    private val binding: ContactLoadStateItemViewBinding,
    private val retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(loadState: LoadState) {
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState !is LoadState.Loading
        binding.errorMsg.isVisible = loadState !is LoadState.Loading
        binding.retryButton.setOnClickListener { retry() }
    }

    companion object {

        fun create(parent: ViewGroup, retry: () -> Unit): ContactLoadStateViewHolder {
            return ContactLoadStateViewHolder(
                ContactLoadStateItemViewBinding.bind(
                    parent.inflate(R.layout.contact_load_state_item_view)
                ),
                retry
            )
        }
    }
}