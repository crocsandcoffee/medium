package com.crocsandcoffee.contactlist.util

import android.view.inputmethod.EditorInfo
import android.widget.EditText

/**
 * @author Omid
 *
 * Helpful kotlin extensions on the [EditText] class
 */

/**
 * Extension function for consuming the [EditorInfo.IME_ACTION_SEND] action
 * and invoking [block]
 */
fun EditText.onSend(block: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_SEND -> {
                block()
                true
            }
            else -> false
        }
    }
}