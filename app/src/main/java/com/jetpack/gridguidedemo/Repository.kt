package com.jetpack.gridguidedemo

import android.util.Log


class Repository {

    companion object {
        val maxRows = 400
        private val maxColumns = 672
    }

    private var channelProgramData = (1..maxRows).map {
        val name = "C$it"
        val programList: ArrayList<String> = ArrayList()
        for (col in 1..maxColumns) {
            programList.add(String.format("P-%d-%d", it, col))
        }
        ChannelProgramData(name = name, programList = programList)
    }

    suspend fun getItems(page: Int, pageSize: Int, rowNumber: Int): Result<List<String>> {
        val startingIndex = page * pageSize
       // Log.d("GrideGuide", "CheckAllPArams ==$page $pageSize $rowNumber $startingIndex")
      //  Log.d("GrideGuide", "Inside getItem IfConditon==${channelProgramData[rowNumber].programList.size}")

        return if (startingIndex + pageSize <= channelProgramData[rowNumber].programList.size) {
          //  Log.d("GrideGuide", "Inside getItem Data==${ channelProgramData[rowNumber].programList.slice(startingIndex until startingIndex + pageSize)}")
            Result.success(
                channelProgramData[rowNumber].programList.slice(startingIndex until startingIndex + pageSize)
            )
        } else Result.success(emptyList())
    }
}