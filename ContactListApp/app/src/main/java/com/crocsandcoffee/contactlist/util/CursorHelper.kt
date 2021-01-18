package com.crocsandcoffee.contactlist.util

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.annotation.WorkerThread
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.crocsandcoffee.contactlist.main.model.ContactListRepository
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
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

private const val SELECTION = ContactsContract.Data.MIMETYPE + " = ?"

// only return contacts with phone numbers
private val SELECTION_ARGS = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

/**
 * @author Omid
 *
 * Helper class that encapsulates the query logic for loading contacts
 */
object CursorHelper {

    /** Used to format the last modified timestamp */
    private val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    @WorkerThread
    fun loadContacts(page: Int, contentResolver: ContentResolver): List<ContactItem> {
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
            "${ContactsContract.Contacts.STARRED} DESC LIMIT ${ContactListRepository.PAGE_SIZE} OFFSET $page"
        )

        val contacts = mutableListOf<ContactItem>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contacts.add(cursor.toContactItem(sdf))
            }
        }

        cursor?.close()
        return contacts
    }

}

fun Cursor.toContactItem(sdf: SimpleDateFormat): ContactItem {
    return ContactItem(
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