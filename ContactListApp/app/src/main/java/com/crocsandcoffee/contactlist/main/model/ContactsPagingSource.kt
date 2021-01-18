package com.crocsandcoffee.contactlist.main.model

import android.content.ContentResolver
import android.content.Context
import androidx.paging.PagingSource
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import com.crocsandcoffee.contactlist.util.CursorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Omid
 *
 * [PagingSource] that loads pages of data from the android ContactsProvider using coroutines
 *
 * @param context ApplicationContext
 */
class ContactsPagingSource(context: Context) : PagingSource<Int, ContactItem>() {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ContactItem> {

        // if key is null, it's the first page load, default to initial page which is 0
        val page = params.key ?: 0

        val contacts = withContext(Dispatchers.IO) {
            CursorHelper.loadContacts(page, contentResolver)
        }

        val offset = contacts.size

        /**
         * set prevKey so pagination works when scrolling up
         * set nextKey so this paging source knows when there is no more data left to fetch
         * and for the next page to be properly passed up
         */
        return LoadResult.Page(
            data = contacts,
            prevKey = if (page == 0) null else page - offset,
            nextKey = if (contacts.isEmpty() || offset < ContactListRepository.PAGE_SIZE) null else page + offset
        )
    }
}