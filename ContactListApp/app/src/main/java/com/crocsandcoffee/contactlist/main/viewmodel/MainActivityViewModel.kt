package com.crocsandcoffee.contactlist.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crocsandcoffee.contactlist.main.model.ContactListRepository
import com.crocsandcoffee.contactlist.main.ui.model.ContactItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author Omid
 *
 * [ViewModel] for [com.crocsandcoffee.contactlist.main.ui.view.MainActivity]
 *
 * This ViewModel is responsible for fetching the list of contacts from the [repo]
 * and emitting them as part of the [_state]
 *
 * @see [handleAction] for details on how to make calls on this ViewModel
 *
 * The UI State [State] is emitted via [StateFlow]
 * Single-fire [Event] are emitted via [SharedFlow]
 */
class MainActivityViewModel(
    private val repo: ContactListRepository,
    private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _event = MutableSharedFlow<Event>()
    val event: SharedFlow<Event> = _event

    /** A [Job] for cancelling coroutine work done with [lifecycleScope] */
    private var job: Job? = null

    /**
     * A single entry point into this [ViewModel] which only supports types of [Action]
     * Each [action] will either modify the [_state] or emit a new [_event]
     */
    fun handleAction(action: Action) {
        when (action) {
            is Action.MessageContact -> emitEvent(Event.ShowDialog(action.contactItem))
            Action.LoadContacts -> loadContacts()
            Action.ShowRationaleDialog -> emitEvent(Event.ShowRationaleDialog)
        }
    }

    private fun emitEvent(event: Event) = viewModelScope.launch(mainDispatcher) {
        _event.emit(event)
    }

    private fun loadContacts() {

        // Make sure we cancel the previous job before creating a new one
        job?.cancel()

        // Set this to a job in case we were to add refresh logic or a search box
        // which should cancel the existing query + flow and create a new one.
        job = viewModelScope.launch(mainDispatcher) {
            repo
                .getContactsAsFlow() // *** COMMENT OUT to use Rx ***
//                .getContactsAsFlowRx() // *** UNCOMMENT to use Rx ***
                .cachedIn(viewModelScope)
                .collectLatest { _state.value = State(it) }
        }
    }

    /**
     * State object emitted via a [StateFlow] representing the UI state of the screen
     */
    data class State(val contacts: PagingData<ContactItem>? = null)

    /**
     * Sealed class hierarchy representing the different supported Actions the [MainActivityViewModel] supports
     */
    sealed class Action {
        object LoadContacts : Action()
        object ShowRationaleDialog : Action()
        data class MessageContact(val contactItem: ContactItem) : Action()
    }

    /**
     * Sealed class hierarchy representing the different supported [SharedFlow] the [MainActivityViewModel] emits
     */
    sealed class Event {
        data class ShowDialog(val contactItem: ContactItem) : Event()
        object ShowRationaleDialog : Event()
    }

    class Factory(
        private val contactListRepository: ContactListRepository,
        private val mainDispatcher: CoroutineDispatcher
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(contactListRepository, mainDispatcher) as T
        }
    }

}