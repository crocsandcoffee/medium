# ContactListApp

## Summary

This sample app loads the device's full contact list, sorted by starred contacts, using Paging3 library. Each contact list item also contains a "Message" button which launches a dialog for the user to input and send a text message to the contact recipient. The loading of contacts in the background has been implemented in two different ways - one with coroutines and one with RxJava. This app follows MVVM architecture and leverages StateFlow to drive the UI state.
