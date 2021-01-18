package com.crocsandcoffee.contactlist.main.model

import android.content.ContentResolver
import android.content.Context
import androidx.paging.PagingSource
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import com.crocsandcoffee.contactlist.util.CursorHelper

/**
 * @author Omid
 *
 * [PagingSource] that loads pages of data from the android ContactsProvider using coroutines
 *
 * Keep logic in this class to a minimum so the actual business logic can
 * easily be tested. No need to test the [PagingSource] library itself.
 *
 * @param context ApplicationContext
 * @param cursorHelper Helper object for loading contacts
 */
class ContactsPagingSource(
    context: Context,
    private val cursorHelper: CursorHelper
) : PagingSource<Int, ContactItem>() {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ContactItem> {
        // if key is null, it's the first page load, default to initial page which is 0
        return cursorHelper.loadContactsSuspend(params.key ?: 0, contentResolver)
    }
}