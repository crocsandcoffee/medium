package com.crocsandcoffee.contactlist.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * @author Omid
 *
 * Wrapper class around [SharedPreferences] for setting app wide preferences
 *
 */
class PreferencesHelper(private val context: Context) {

    private val pref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * User indicated to not be presented with a dialog requesting contact permission ever again
     * @param dontAskAgain indicates the user's choice
     */
    fun setDontAskAgainContactPermission(dontAskAgain: Boolean) {
        pref.edit { putBoolean(PREF_CONTACT_PERMISSION_KEY, dontAskAgain) }
    }

    /**
     * @return true if the user indicated to no longer be shown the contact permissions dialog
     */
    fun dontAskAgainContactPermission() = pref.getBoolean(PREF_CONTACT_PERMISSION_KEY, false)

    companion object {
        private const val TAG = "PrefManager"
        private const val PREF_CONTACT_PERMISSION_KEY = "$TAG.PREF_CONTACT_PERMISSION_KEY"
    }

}
