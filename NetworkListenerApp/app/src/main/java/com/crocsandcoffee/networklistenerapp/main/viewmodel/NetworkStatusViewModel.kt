package com.crocsandcoffee.networklistenerapp.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crocsandcoffee.networklistenerapp.main.data.NetworkStatusRepository
import com.crocsandcoffee.networklistenerapp.main.model.NetworkStatusState
import kotlinx.coroutines.flow.StateFlow

/**
 * @author Omid
 *
 * A [ViewModel] that provides a [networkState] where consumers can observe changes
 * in the network state and react, such as showing or hiding a offline bar in the UI.
 *
 * See the [NetworkStatusRepository] for how the network state is obtained and managed
 */
class NetworkStatusViewModel(
    private val repo: NetworkStatusRepository
) : ViewModel() {

    /** [StateFlow] emitting a [NetworkStatusState] every time it changes */
    val networkState: StateFlow<NetworkStatusState> = repo.state

    /* Simple getter for fetching network connection status synchronously */
    fun isDeviceOnline() : Boolean = repo.hasNetworkConnection()

    class Factory constructor(
        private val networkRepository: NetworkStatusRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NetworkStatusViewModel(networkRepository) as T
        }
    }
}