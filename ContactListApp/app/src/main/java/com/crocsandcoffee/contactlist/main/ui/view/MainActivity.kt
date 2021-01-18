package com.crocsandcoffee.contactlist.main.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crocsandcoffee.contactlist.R
import com.crocsandcoffee.contactlist.databinding.ActivityMainBinding
import com.crocsandcoffee.contactlist.databinding.MessageDialogViewBinding
import com.crocsandcoffee.contactlist.di.DependencyInjectionHelper
import com.crocsandcoffee.contactlist.main.viewmodel.MainActivityViewModel
import com.crocsandcoffee.contactlist.main.viewmodel.MainActivityViewModel.Action
import com.crocsandcoffee.contactlist.util.PreferencesHelper
import com.crocsandcoffee.contactlist.util.onSend
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS)

const val PERMISSIONS_REQUEST_CODE = 10

/**
 * @author Omid
 *
 * UI Controller for displaying a list of contacts with an option to
 * send a message to a contact.
 *
 * The list of contacts are displayed in order by starred contacts first.
 *
 * See [MainActivityViewModel] for business logic handling
 */
class MainActivity : AppCompatActivity() {

    /** A [Job] for cancelling coroutine work done with [lifecycleScope] */
    private var job: Job? = null

    /** ViewModel responsible for all business logic for this activity */
    private val viewModel: MainActivityViewModel by viewModels {
        DependencyInjectionHelper.injectViewModelFactory(this)
    }

    /** RecyclerView adapter used for displaying the contact list items */
    private val adapter: ContactListAdapter by lazy {
        ContactListAdapter(Glide.with(this)) { item ->
            viewModel.handleAction(Action.MessageContact(item))
        }
    }

    private val decoratorSpacing: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.item_decorator_spacing_dp)
    }

    /** preferences wrapper */
    private val pref: PreferencesHelper by lazy { PreferencesHelper(this) }

    /** indicates if the user should be presented the permissions rational dialog */
    private var showPermissionRational: Boolean = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.contactListRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

                // only add top spacing on first item
                if (parent.getChildAdapterPosition(view) == 0) {
                    // double the top spacing
                    outRect.top = decoratorSpacing + decoratorSpacing
                }
                outRect.bottom = decoratorSpacing
            }
        })

        when {
            hasRequiredPermissions() -> {
                toggleViews(true)
                hookupAdapters()

                // reset the flag
                pref.setDontAskAgainContactPermission(false)

                // kick off the initial load
                viewModel.handleAction(Action.LoadContacts)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {

                showPermissionRational = true

                viewModel.handleAction(Action.ShowRationaleDialog)
            }
            else -> {
                // Request the required permission
                requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            }
        }

        setupObservers()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            /** Verify every single element in [grantResults] matches [PackageManager.PERMISSION_GRANTED] */
            val allPermissionsGranted = grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }

            when {
                allPermissionsGranted -> {
                    toggleViews(true)
                    hookupAdapters()
                    viewModel.handleAction(Action.LoadContacts)
                }
                pref.dontAskAgainContactPermission() -> {
                    Toast.makeText(this, R.string.permissions_request_dont_ask, Toast.LENGTH_LONG).show()
                    toggleViews(false)
                }
                else -> {

                    // if showPermissionRational was true, then permissions dialog was presented to the user
                    // if shouldShowRequestPermissionRationale returns false, it means user selected "Don't Ask Again"
                    // from the permissions dialog
                    if (showPermissionRational && !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        pref.setDontAskAgainContactPermission(true)
                    }

                    /** the user denied the permissions request  */
                    Toast.makeText(this, R.string.permissions_request_denied, Toast.LENGTH_LONG).show()
                    toggleViews(false)
                }
            }
        }
    }

    private fun hookupAdapters() {

        // add a loading spinner adapter as a header and footer
        binding.contactListRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ContactsLoadStateAdapter { adapter.retry() },
            footer = ContactsLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->

            // Only show the list if refresh succeeds.
            binding.contactListRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            // Show loading spinner during initial load or refresh.
            binding.groupLoading.isVisible = loadState.source.refresh is LoadState.Loading
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.contactListRecyclerView.scrollToPosition(0) }
        }
    }

    /**
     * Start observing on observable fields on [viewModel]
     *
     * Note: Use [asLiveData] extension to stop listening to changes when view is not visible
     * since StateFlow/SharedFlow do not unregister consumers based on lifecycle state like LiveData
     */
    private fun setupObservers() {

        viewModel.state.asLiveData().observe(this) { state ->
            // Make sure we cancel the previous job before creating a new one
            job?.cancel()
            job = lifecycleScope.launch {
                state.contacts?.let { adapter.submitData(state.contacts) }
            }
        }

        viewModel.event.asLiveData().observe(this) { event ->
            consumeEvent(event)
        }
    }

    private fun sendMessage(msg: String?, phoneNumber: String) {
        try {
            if (!msg.isNullOrEmpty()) {
                // Yes this is a known policy violation if deploying to PlayStore..
                // but just wanted a quick and dirty way to show the end-to-end functionality
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, msg, null, null)
                Toast.makeText(this, R.string.message_sent, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.message_empty, Toast.LENGTH_LONG).show()
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, R.string.message_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun consumeEvent(event: MainActivityViewModel.Event) {
        when (event) {
            is MainActivityViewModel.Event.ShowDialog -> {

                val dialogBinding = MessageDialogViewBinding.inflate(layoutInflater)

                val phoneNumber = event.contactItem.phoneNumber
                val editText = dialogBinding.messageEditText

                val alertDialog = AlertDialog
                    .Builder(this)
                    .setView(dialogBinding.root)
                    .setMessage(R.string.message_dialog_title)
                    .setPositiveButton(R.string.send) { _, _ ->
                        sendMessage(editText.text?.toString(), phoneNumber)
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }.show()

                // listen for Send Action key
                editText.onSend {
                    sendMessage(editText.text?.toString(), phoneNumber)
                    alertDialog.dismiss()
                }
            }

            MainActivityViewModel.Event.ShowRationaleDialog -> {
                AlertDialog
                    .Builder(this)
                    .setMessage(R.string.contact_permission_rationale)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        Toast.makeText(this, R.string.permissions_request_denied, Toast.LENGTH_LONG).show()
                        toggleViews(false)
                    }
                    .show()
            }
        }
    }

    private fun toggleViews(hasPermissions: Boolean) {
        binding.needPermissionsTv.isVisible = !hasPermissions
        binding.groupLoading.isVisible = hasPermissions
        binding.contactListRecyclerView.isVisible = hasPermissions
    }

    // check if all the required permissions have been granted
    private fun hasRequiredPermissions() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}