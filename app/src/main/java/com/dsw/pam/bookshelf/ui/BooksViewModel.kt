package com.dsw.pam.bookshelf.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dsw.pam.bookshelf.BooksApplication
import com.dsw.pam.bookshelf.data.Book
import com.dsw.pam.bookshelf.data.BooksRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface BooksUiState {
    data class Success(val bookSearch: List<Book>) : BooksUiState
    object Error : BooksUiState
    object Loading : BooksUiState
}

data class SearchHistoryEntry(
    val query: String
)

class BooksViewModel(
    private val booksRepository: BooksRepository
) : ViewModel() {

    var booksUiState: BooksUiState by mutableStateOf(BooksUiState.Loading)
        private set

    private val _searchWidgetState = MutableStateFlow(SearchWidgetState.CLOSED)
    val searchWidgetState: StateFlow<SearchWidgetState> = _searchWidgetState

    private val _searchTextState = MutableStateFlow("")
    val searchTextState: StateFlow<String> = _searchTextState

    private val _searchHistory = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory

    fun updateSearchWidgetState(newValue: SearchWidgetState) {
        _searchWidgetState.value = newValue
    }

    fun updateSearchTextState(newValue: String) {
        _searchTextState.value = newValue
    }

    init {
        getBooks()
        loadSearchHistory()
    }

    fun getBooks(query: String = "book", maxResults: Int = 40) {
        viewModelScope.launch {
            booksUiState = BooksUiState.Loading
            booksUiState =
                try {
                    BooksUiState.Success(booksRepository.getBooks(query, maxResults))
                } catch (e: IOException) {
                    BooksUiState.Error
                } catch (e: HttpException) {
                    BooksUiState.Error
                }
        }
    }

    fun saveSearchQuery(query: String) {
        val db = FirebaseFirestore.getInstance()
        val searchEntry = hashMapOf(
            "query" to query,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("searches")
            .add(searchEntry)
            .addOnSuccessListener {
                println("Search query saved successfully.")
                loadSearchHistory()
            }
            .addOnFailureListener {
                println("Error saving search query: $it")
            }
    }

    private fun loadSearchHistory() {
        val db = FirebaseFirestore.getInstance()

        db.collection("searches")
            .get()
            .addOnSuccessListener { documents ->
                val history = documents.map { document ->
                    val query = document.getString("query") ?: ""

                    SearchHistoryEntry(
                        query = query
                    )
                }
                _searchHistory.value = history
            }
            .addOnFailureListener {
                println("Error fetching search history: $it")
            }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BooksApplication)
                val booksRepository = application.container.booksRepository
                BooksViewModel(booksRepository = booksRepository)
            }
        }
    }
}

enum class SearchWidgetState {
    OPENED,
    CLOSED
}