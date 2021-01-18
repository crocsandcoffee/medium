package com.crocsandcoffee.contactlist.main.model

import android.content.ContentResolver
import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.rxjava3.RxPagingSource
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import com.crocsandcoffee.contactlist.util.CursorHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * @author Omid
 *
 * [PagingSource] that loads pages of data from the android ContactsProvider using Rx
 *
 * @param context ApplicationContext
 */
class ContactsPagingSourceRx(context: Context) : RxPagingSource<Int, ContactItem>() {

    private val contentResolver: ContentResolver = context.contentResolver

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, ContactItem>> {

        val page = params.key ?: 0

        return Single
            .create<List<ContactItem>> { emitter ->
                emitter.onSuccess(CursorHelper.loadContacts(page, contentResolver))
            }
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, ContactItem>> { contacts ->

                val offset = contacts.size

                /**
                 * set prevKey so pagination works when scrolling up
                 * set nextKey so this paging source knows when there is no more data left to fetch
                 * and for the next page to be properly passed up
                 */
                LoadResult.Page(
                    data = contacts,
                    prevKey = if (page == 0) null else page - offset,
                    nextKey = if (contacts.isEmpty() || offset < ContactListRepository.PAGE_SIZE) null else page + offset
                )

            }
            .onErrorReturn { e -> LoadResult.Error(e) }
    }
}