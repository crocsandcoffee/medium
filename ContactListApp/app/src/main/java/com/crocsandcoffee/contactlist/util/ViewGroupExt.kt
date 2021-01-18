package com.crocsandcoffee.contactlist.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * @author Omid
 *
 * Helpful kotlin extensions on the [ViewGroup] class
 */

/**
 * Extension function for shortening the inflate logic on a [ViewGroup]
 */
fun ViewGroup.inflate(@LayoutRes layoutResId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutResId, this, attachToRoot)
}