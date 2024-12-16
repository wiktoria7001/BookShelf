package com.dsw.pam.bookshelf.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsw.pam.bookshelf.ui.screens.HomeScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dsw.pam.bookshelf.data.Book

@Composable
fun BooksApp(
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit
) {
    val booksViewModel: BooksViewModel = viewModel(factory = BooksViewModel.Factory)
    val searchWidgetState by booksViewModel.searchWidgetState.collectAsState()
    val searchTextState by booksViewModel.searchTextState.collectAsState()
    val searchHistory by booksViewModel.searchHistory.collectAsState()
    var showHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainAppBar(
                searchWidgetState = searchWidgetState,
                searchTextState = searchTextState,
                onTextChange = { booksViewModel.updateSearchTextState(newValue = it) },
                onCloseClicked = { booksViewModel.updateSearchWidgetState(newValue = SearchWidgetState.CLOSED) },
                onSearchClicked = { booksViewModel.getBooks(it) },
                onSearchTriggered = { booksViewModel.updateSearchWidgetState(newValue = SearchWidgetState.OPENED) },
                onHistoryClicked = { showHistoryDialog = true },
                viewModel = booksViewModel
            )
        }
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                booksUiState = booksViewModel.booksUiState,
                retryAction = { booksViewModel.getBooks() },
                modifier = modifier,
                onBookClicked = onBookClicked
            )
        }
    }

    if (showHistoryDialog) {
        SearchHistoryDialog(
            searchHistory = searchHistory,
            onDismissRequest = { showHistoryDialog = false }
        )
    }
}
