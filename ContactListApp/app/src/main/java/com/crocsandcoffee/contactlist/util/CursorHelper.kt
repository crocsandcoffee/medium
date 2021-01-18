package com.crocsandcoffee.contactlist.util

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import androidx.paging.PagingSource
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// columns to return from the query
private val PROJECTION = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
    ContactsContract.Contacts.STARRED,
    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
    ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,
    ContactsContract.Data.DATA1
)

// selection clause for only selecting records with Phone data
private const val SELECTION = ContactsContract.Data.MIMETYPE + " = ?"

// selection args used to only return contacts with phone numbers
private val SELECTION_ARGS = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

/**
 * @author Omid
 *
 * Helper class that encapsulates the query logic for loading contacts.
 *
 * Contacts can be loaded using one of the two options:
 *
 * [loadContactsSuspend] for loading contacts via coroutines
 * [loadContactsSingle] for loading contacts via RxJava
 */
class CursorHelper(
    private val pageSize: Int,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val ioScheduler: Scheduler = Schedulers.io(),
    private val sdf: SimpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
) {

    /**
     * Load contacts via coroutines
     */
    @AnyThread
    suspend fun loadContactsSuspend(
        page: Int,
        contentResolver: ContentResolver
    ): PagingSource.LoadResult<Int, ContactItem> {
        return withContext(ioDispatcher) {
            contactsToLoadResult(loadContacts(page, contentResolver), page)
        }
    }

    /**
     * Load contacts via Rx
     */
    @AnyThread
    fun loadContactsSingle(
        page: Int,
        contentResolver: ContentResolver
    ): Single<PagingSource.LoadResult<Int, ContactItem>> {
        return Single
            .create<List<ContactItem>> { emitter ->
                emitter.onSuccess(loadContacts(page, contentResolver))
            }
            .subscribeOn(ioScheduler)
            .map<PagingSource.LoadResult<Int, ContactItem>> { contacts ->
                contactsToLoadResult(contacts, page)
            }
            .onErrorReturn { e -> PagingSource.LoadResult.Error(e) }
    }

    /**
     * Helper function for converting the list [contacts] to a [PagingSource.LoadResult]
     */
    @AnyThread
    private fun contactsToLoadResult(
        contacts: List<ContactItem>,
        page: Int
    ): PagingSource.LoadResult.Page<Int, ContactItem> {

        val offset = contacts.size

        /**
         * set prevKey so pagination works when scrolling up
         * set nextKey so this paging source knows when there is no more data left to fetch
         * and for the next page to be properly passed up
         */
        return PagingSource.LoadResult.Page(
            data = contacts,
            prevKey = if (page == 0) null else page - offset,
            nextKey = if (contacts.isEmpty() || offset < pageSize) null else page + offset
        )
    }

    @WorkerThread
    private fun loadContacts(page: Int, contentResolver: ContentResolver): List<ContactItem> {
        /**
         * This query performs an implicit join on the different Contact tables
         * so that it can return all the columns found in [PROJECTION]
         *
         * Known "issue" - returns "duplicates" because there can be multiple accounts
         * synced with a contact's phone number
         *
         * The sort order clause is where the pagination/sorting magic lies.
         * - It will sort by starred contacts first
         * - LIMIT clause indicates the page size
         * - OFFSET clause indicates which "page" of contacts should be loaded
         */
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            PROJECTION,
            SELECTION,
            SELECTION_ARGS,
            "${ContactsContract.Contacts.STARRED} DESC LIMIT $pageSize OFFSET $page"
        )

        val contacts = mutableListOf<ContactItem>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contacts.add(cursorToContactItem(cursor))
            }
        }

        return contacts.also { cursor?.close() }
    }

    @WorkerThread
    private fun cursorToContactItem(cursor: Cursor): ContactItem {
        return with(cursor) {
            ContactItem(
                id = getLong(getColumnIndex(ContactsContract.Contacts._ID)),
                name = getString(getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)),
                thumbnailUri = getStringOrNull(getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))?.toUri(),
                starred = getInt(getColumnIndex(ContactsContract.Contacts.STARRED)) != 0,
                phoneNumber = getString(getColumnIndex(ContactsContract.Data.DATA1)),
                lastModifiedDate = getLong(getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP))
                    .let { timestamp ->
                        sdf.format(Date(timestamp))
                    }
            )
        }
    }

}