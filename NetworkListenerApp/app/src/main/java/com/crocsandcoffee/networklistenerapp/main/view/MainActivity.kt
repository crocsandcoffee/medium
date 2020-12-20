package com.crocsandcoffee.networklistenerapp.main.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import com.crocsandcoffee.networklistenerapp.databinding.ActivityMainBinding
import com.crocsandcoffee.networklistenerapp.di.DependencyInjectionHelper
import com.crocsandcoffee.networklistenerapp.main.data.NetworkStatusRepository
import com.crocsandcoffee.networklistenerapp.main.model.NetworkStatusState
import com.crocsandcoffee.networklistenerapp.main.viewmodel.NetworkStatusViewModel

/**
 * @author Omid
 *
 * UI Controller that displays a centered message and an offline bar if
 * the user loses network connectivity
 *
 * Business logic + UI state handling is delegated to [NetworkStatusViewModel]
 * and is consumed in [setupObservers] then displayed in [render]
 */
class MainActivity : AppCompatActivity() {

    /** The main repo handling network callbacks */
    private val repo: NetworkStatusRepository by lazy {
        DependencyInjectionHelper.injectRepo(applicationContext)
    }

    /** ViewModel for consuming network changes */
    private val viewModel: NetworkStatusViewModel by viewModels {
        DependencyInjectionHelper.injectViewModelFactory(repo)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObservers()
    }

    private fun setupObservers() {
        /*
         * convert StateFlow to LiveData to save precious PRECIOUS resources
         * when view is no longer visible
         */
        viewModel.networkState.asLiveData().observe(this) { state ->
            render(state)
        }
    }

    private fun render(state: NetworkStatusState) {
        binding.offlineBarInclude.root.visibility = when (state) {
            NetworkStatusState.NetworkStatusConnected -> View.GONE
            NetworkStatusState.NetworkStatusDisconnected -> View.VISIBLE
        }
    }
}