package com.crocsandcoffee.networklistenerapp.main.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.RemoteException
import com.crocsandcoffee.networklistenerapp.main.model.NetworkStatusState
import com.crocsandcoffee.networklistenerapp.main.model.NetworkStatusState.NetworkStatusConnected
import com.crocsandcoffee.networklistenerapp.main.model.NetworkStatusState.NetworkStatusDisconnected
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * @author Omid
 *
 * This Repository manages listening to changes in the network state of the device and
 * emits the network change via a [StateFlow].
 *
 * Typical usage should be using the [com.crocsandcoffee.networklistenerapp.main.viewmodel.NetworkStatusViewModel]
 * to listen for changes to the [state].
 *
 * This class should be a [Singleton] and self manages when to register/unregister itself as a
 * listener to networking changes by only registering if it has an active observer of it's
 * state. Once it no longer has active observers it will unregister itself.
 *
 * For devices running [Build.VERSION_CODES.N] or greater, it will use the new [ConnectivityManager.NetworkCallback]
 * and for devices older than N it will use a [BroadcastReceiver].
 *
 * See [NetworkCallbackImpl] and [ConnectivityReceiver] for implementations of the two approaches.
 */
class NetworkStatusRepository constructor(
    private val context: Context,
    private val mainDispatcher: CoroutineDispatcher,
    private val appScope: CoroutineScope
) {

    private val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var callback: ConnectivityManager.NetworkCallback? = null
    private var receiver: ConnectivityReceiver? = null

    private val _state = MutableStateFlow(getCurrentNetwork())
    val state: StateFlow<NetworkStatusState> = _state

    init {
        _state
            .subscriptionCount
            .map { count -> count > 0 } // map count into active/inactive flag
            .distinctUntilChanged() // only react to true<->false changes
            .onEach { isActive ->
                /** Only subscribe to network callbacks if we have an active subscriber */
                if (isActive) subscribe()
                else unsubscribe()
            }
            .launchIn(appScope)
    }

    /* Simple getter for fetching network connection status synchronously */
    fun hasNetworkConnection() = getCurrentNetwork() == NetworkStatusConnected

    private fun getCurrentNetwork(): NetworkStatusState {
        return try {
            cm.getNetworkCapabilities(cm.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .let { connected ->
                    if (connected == true) NetworkStatusConnected
                    else NetworkStatusDisconnected
                }
        } catch (e: RemoteException) {
            NetworkStatusDisconnected
        }
    }

    private fun subscribe() {

        // just in case
        if (callback != null || receiver != null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            callback = NetworkCallbackImpl().also { cm.registerDefaultNetworkCallback(it) }
        } else {
            receiver = ConnectivityReceiver().also {
                context.registerReceiver(it, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            }
        }

        /* emit our initial state */
        emitNetworkState(getCurrentNetwork())
    }

    private fun unsubscribe() {

        if (callback == null && receiver == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            callback?.run { cm.unregisterNetworkCallback(this) }
            callback = null
        } else {
            receiver?.run { context.unregisterReceiver(this) }
            receiver = null
        }
    }

    private fun emitNetworkState(newState: NetworkStatusState) {
        appScope.launch(mainDispatcher) {
            _state.emit(newState)
        }
    }

    private inner class ConnectivityReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            /** emit the new network state */
            intent
                .getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
                ?.isConnectedOrConnecting
                .let { connected ->
                    if (connected == true) emitNetworkState(NetworkStatusConnected)
                    else emitNetworkState(getCurrentNetwork())
                }
        }
    }

    private inner class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) = emitNetworkState(NetworkStatusConnected)

        override fun onLost(network: Network) = emitNetworkState(NetworkStatusDisconnected)
    }
}