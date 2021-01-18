# ContactListApp

## Summary

This sample app loads the device's full contact list via the [Contacts Provider](https://developer.android.com/guide/topics/providers/contacts-provider), sorted by starred contacts first, using [Paging3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) and [RxPagingSource](https://developer.android.com/reference/kotlin/androidx/paging/rxjava3/RxPagingSource) (using RxJava) or [PagingSource](https://developer.android.com/reference/kotlin/androidx/paging/PagingSource) (using coroutines)

Each contact list item also contains a "Message" button which launches a dialog for the user to input and send a text message to the contact recipient. 

This demo app follows [MVVM architecture](https://developer.android.com/jetpack/guide) and leverages [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) to drive the UI state from the [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel).

## Demo

![ContactListApp](demo.gif)
