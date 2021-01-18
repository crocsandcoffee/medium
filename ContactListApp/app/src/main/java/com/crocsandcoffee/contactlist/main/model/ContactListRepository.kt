package com.crocsandcoffee.contactlist.main.model

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import kotlinx.coroutines.flow.Flow

/**
 * @author Omid
 *
 * Repository that exposes a function for loading contacts
 *
 * The actual loading of contacts is delegated to the [ContactsPagingSource] via
 * [getContactsAsFlow] or [getContactsAsFlowRx]
 *
 * @param context ApplicationContext
 */
class ContactListRepository(private val context: Context) {

    /**
     * Use coroutines for loading contacts
     */
    fun getContactsAsFlow(): Flow<PagingData<ContactItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ContactsPagingSource(context) }
        ).flow
    }

    /**
     * Use Rx for loading contacts
     */
    fun getContactsAsFlowRx(): Flow<PagingData<ContactItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ContactsPagingSourceRx(context) }
        ).flow
    }

    companion object {
        // This should be experimented with. In our case, contact
        // list items are small and show a small subset of metadata
        // so we will pick a higher number to reduce overhead of large number of queries
        const val PAGE_SIZE = 30
    }

}