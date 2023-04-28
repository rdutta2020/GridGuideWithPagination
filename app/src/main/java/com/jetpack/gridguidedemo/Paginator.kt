package com.jetpack.gridguidedemo

interface Paginator<Int, Item> {
    suspend fun loadNextItems(rowNumber: Int)
    fun reset()
    fun saveCurrentPageNumber(pageNumber: Int)
}