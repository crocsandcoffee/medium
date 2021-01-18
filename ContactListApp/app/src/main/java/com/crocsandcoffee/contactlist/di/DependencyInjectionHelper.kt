package com.crocsandcoffee.contactlist.di

import android.content.Context
import com.crocsandcoffee.contactlist.main.model.ContactListRepository
import com.crocsandcoffee.contactlist.main.viewmodel.MainActivityViewModel
import kotlinx.coroutines.Dispatchers

/**
 * @author Omid
 *
 * Dependency Injection Black Box
 */
object DependencyInjectionHelper {

    /**
     * Note: [context] should be the [Context.getApplicationContext]
     */
    fun injectRepo(context: Context): ContactListRepository {
        return ContactListRepository(context)
    }

    /**
     * Note: [context] should be the [Context.getApplicationContext]
     */
    fun injectViewModelFactory(context: Context): MainActivityViewModel.Factory {
        return MainActivityViewModel.Factory(injectRepo(context), Dispatchers.Main)
    }
}