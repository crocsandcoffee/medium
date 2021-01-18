package com.crocsandcoffee.contactlist.main.model

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.rxjava3.RxPagingSource
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import com.crocsandcoffee.contactlist.util.CursorHelper
import io.reactivex.rxjava3.core.Single

/**
 * @author Omid
 *
 * [PagingSource] that loads pages of data from the android ContactsProvider using Rx
 *
 * Keep logic in this class to a minimum so the actual business logic can
 * easily be tested. No need to test the [PagingSource] library itself.
 *
 * @param context ApplicationContext
 * @param cursorHelper Helper object for loading contacts
 */
class ContactsPagingSourceRx(
    private val context: Context,
    private val cursorHelper: CursorHelper
) : RxPagingSource<Int, ContactItem>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, ContactItem>> {
        // if key is null, it's the first page load, default to initial page which is 0
        return cursorHelper.loadContactsSingle(
            page = params.key ?: 0,
            contentResolver = context.contentResolver
        )
    }
}