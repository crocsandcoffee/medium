package com.crocsandcoffee.contactlist.main.model

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import com.crocsandcoffee.contactlist.util.CursorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * @author Omid
 *
 * Repository used by [com.crocsandcoffee.contactlist.main.viewmodel.MainActivityViewModel]
 * for loading a user's full contact list
 *
 * Loading the contact list can be done using one of the two methods:
 *
 * [getContactsAsFlow] uses the [ContactsPagingSource] which leverages coroutines
 * for fetching contacts
 *
 * [getContactsAsFlowRx] uses the [ContactsPagingSourceRx] which leverages RxJava
 * for fetching contacts
 *
 * @param context ApplicationContext
 */
class ContactListRepository(private val context: Context) {

    /**
     * Use coroutines for loading contacts
     */
    fun getContactsAsFlow(pageSize: Int = PAGE_SIZE): Flow<PagingData<ContactItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ContactsPagingSource(context, CursorHelper(pageSize, Dispatchers.IO))
            }
        ).flow
    }

    /**
     * Use Rx for loading contacts
     */
    fun getContactsAsFlowRx(pageSize: Int = PAGE_SIZE): Flow<PagingData<ContactItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ContactsPagingSourceRx(context, CursorHelper(pageSize, Dispatchers.IO))
            }
        ).flow
    }

    companion object {
        /**
         * This should be experimented with. In our case, contact
         * list items are small and show a small subset of metadata
         * so we will pick a higher number to reduce overhead of large number of queries
         */
        private const val PAGE_SIZE = 30
    }

}