package com.jetpack.gridguidedemo

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        val PAGESIZE = 10
    }
    val repository = Repository()

    var state by mutableStateOf(ScreenState())

    private val paginator = DefaultPaginator(
        firstPageNumber = state.page.size,
        onLoadUpdated = {
            state = state.copy(isLoading = it)
        },
        onRequest = { nextPage, rowNumber ->
            repository.getItems(nextPage, PAGESIZE, rowNumber)
        },
        getNextKey = { rowNumber, items ->
            state.page[rowNumber] + 1
        },
        onError = {
            state = state.copy(error = it?.localizedMessage)
        },
        onSuccess = { items, nextPageNumber, rowNumber ->
            //Log.d("GrideGuide", "GrideGuide=== RowNumber ==$rowNumber items received size ==${items.size} nextPageNumber==$nextPageNumber")
            //Log.d("GrideGuide", "GrideGuide=== BeforeSize ==${state.items[rowNumber].size}")
            state.items[rowNumber] = state.items[rowNumber].plus(items)
            val nextPage = state.page
            nextPage[rowNumber] = nextPageNumber
            state = state.copy(
                items = state.items,
                page = nextPage,
                endReached = items.isEmpty()
            )
           // Log.d("GrideGuide", "GrideGuide=== AfterSize ==${state.items[rowNumber].size}")
        }
    )

    init {
        for (i in 1..Repository.maxRows) {
            state.items.add(emptyList())
            state.page.add(0)
        }
        for (i in 0 until Repository.maxRows) {
            loadNextItems(i)
        }
    }

    fun loadNextItems(rowNumber: Int) {
        viewModelScope.launch {
            paginator.saveCurrentPageNumber(state.page.get(rowNumber))
           Log.d("GrideGuide", "LoadingNextItem for row number $rowNumber")
            paginator.loadNextItems(rowNumber)
        }
    }

    data class ScreenState(
        val isLoading: Boolean = false,
        val items: ArrayList<List<String>> = ArrayList(),
        val error: String? = null,
        val endReached: Boolean = false,
        val page: ArrayList<Int> = ArrayList()
    )
}
