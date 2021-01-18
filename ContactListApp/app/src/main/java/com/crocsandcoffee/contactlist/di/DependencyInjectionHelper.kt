package com.crocsandcoffee.contactlist.di

import android.content.Context
import com.crocsandcoffee.contactlist.main.model.ContactListRepository
import com.crocsandcoffee.contactlist.main.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * @author Omid
 *
 * Dependency Injection Black Box
 */
object DependencyInjectionHelper {

    fun injectMainDispatcher() : CoroutineDispatcher {
        return Dispatchers.Main
    }

    /**
     * Note: [context] should be Application context e.g. [Context.getApplicationContext]
     */
    fun injectRepo(context: Context): ContactListRepository {
        return ContactListRepository(context)
    }

    /**
     * Note: [context] should be Application context e.g. [Context.getApplicationContext]
     */
    fun injectViewModelFactory(context: Context): MainActivityViewModel.Factory {
        return MainActivityViewModel.Factory(injectRepo(context), injectMainDispatcher())
    }
}