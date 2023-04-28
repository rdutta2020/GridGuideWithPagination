package com.jetpack.gridguidedemo

import android.util.Log

class DefaultPaginator<Item>(
    private val firstPageNumber: Int,
    private inline val onLoadUpdated: (Boolean) -> Unit,
    private inline val onRequest: suspend (nextKey: Int, rowNumber: Int) -> Result<List<Item>>,
    private inline val getNextKey: suspend (rowNumber : Int, List<Item>) -> Int,
    private inline val onError: suspend (Throwable?) -> Unit,
    private inline val onSuccess: suspend (items: List<Item>, newKey: Int, rowNumber: Int) -> Unit
) : Paginator<Int, Item> {

    private var currentPageNumber = firstPageNumber
    private var isMakingRequest = false

    override suspend fun loadNextItems(rowNumber: Int) {
        //Log.d("GrideGuide", "Inside loadNextItems==$rowNumber")
        if (isMakingRequest) {
            return
        }
        //Log.d("GrideGuide", "Inside isMakingRequest==$rowNumber")
        isMakingRequest = true
        onLoadUpdated(true)
        val result = onRequest(currentPageNumber, rowNumber)
        isMakingRequest = false
        val items = result.getOrElse {
            onError(it)
            onLoadUpdated(false)
            return
        }
        currentPageNumber = getNextKey(rowNumber, items)
        onSuccess(items, currentPageNumber, rowNumber)
        onLoadUpdated(false)
    }

    override fun reset() {
        currentPageNumber = firstPageNumber
    }

    override fun saveCurrentPageNumber(pageNumber: Int) {
        currentPageNumber = pageNumber
    }
}