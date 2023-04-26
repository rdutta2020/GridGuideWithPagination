// Checked


package com.jetpack.gridguidedemo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpack.gridguidedemo.ui.theme.GridGuideDemoTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var state: ScrollState
    var fixedColumnWidth: Int = 0
    var lazyRowWidth: Int = 0
    val timeslotsCount: Int = 100 //672

    @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter",
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()

        setContent {
            state = rememberScrollState()
            fixedColumnWidth = 128
            lazyRowWidth = LocalConfiguration.current.screenWidthDp - fixedColumnWidth
            GridGuideDemoTheme(darkTheme = false) {
                val lazyListState = rememberLazyListState()
                Scaffold(
                    content = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            MainContent(lazyListState = lazyListState)
                            TopBar(lazyListState = lazyListState)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun MainContent(lazyListState: LazyListState) {
        val padding by animateDpAsState(
            targetValue = if (lazyListState.isScrolled) 0.dp else TOP_BAR_HEIGHT,
            animationSpec = tween(durationMillis = 300)
        )

        Column(
            modifier = Modifier.padding(top = padding)
        ) {
            DrawHeader()
            DrawGrid(lazyListState)
        }
    }

    @Composable
    fun TopBar(lazyListState: LazyListState) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.primary)
                .animateContentSize(animationSpec = tween(durationMillis = 300))
                .height(height = if (lazyListState.isScrolled) 0.dp else TOP_BAR_HEIGHT),
            contentPadding = PaddingValues(start = 16.dp)
        ) {
            Text(
                text = "GridGuidePOC",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    color = MaterialTheme.colors.surface
                )
            )
        }
    }

    @Composable
    fun DrawGrid(scrollState: LazyListState) {
        LazyColumn(
            state = scrollState
        ) {
            items(channelProgramData) { channelProgram ->
                DrawGridRowItem(item = channelProgram)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DrawHorizontalList(list: ArrayList<String>, height: Int) {

        val lazyListState = rememberLazyListState()
        val snappingLayout =
            remember(lazyListState) { CreateSnapLayoutInfoProvider(timeslotsCount, state) }
        val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

        LazyRow(
            modifier = Modifier
                .horizontalScroll(state)
                .width((timeslotsCount * lazyRowWidth).dp),
            state = lazyListState,
            flingBehavior = flingBehavior
        ) {
            items(list) { item ->
                DrawRowListItem(name = item, height = height)
            }
        }
    }

    @ExperimentalFoundationApi
    fun CreateSnapLayoutInfoProvider(
        itemCount: Int,
        scrollState: ScrollState,
    ): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {
        override fun Density.calculateApproachOffset(initialVelocity: Float): Float = 0f
        override fun Density.calculateSnapStepSize(): Float {
            return 0f
        }

        override fun Density.calculateSnappingOffsetBounds(): ClosedFloatingPointRange<Float> {
            val bound0 = -scrollState.value % snapStepSize()
            val bound1 = snapStepSize() + bound0

            return (if (bound0 >= 0 && bound1 < 0) bound1.rangeTo(bound0) else bound0.rangeTo(bound1))
        }

        fun snapStepSize(): Float =
            scrollState.maxValue.toFloat() / (itemCount - 1)
    }

    @Composable
    fun DrawRowListItem(
        name: String,
        height: Int
    ) {
        var fraction = 1F
        if (name == "P04a" || name == "P07a") {
            fraction = 0.5F
        }
        if (name == "P02b" || name == "P08b" || name == "P01c" || name == "P05c") {
            fraction = 0.5F
        }
        Box(
            modifier = Modifier
                .width((lazyRowWidth * fraction).dp)
                .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                .height(height.dp)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )
        }
    }

    @Composable
    fun DrawGridRowItem(
        item: ChannelProgramData
    ) {
        Box(modifier = Modifier.height(30.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(fixedColumnWidth.dp)
                        .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                        .height(30.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }
                DrawHorizontalList(timeslots, 30)
            }
        }
    }


    @Composable
    fun DrawHeader() {
        Box(modifier = Modifier.height(30.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(fixedColumnWidth.dp)
                        .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                        .height(30.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = "Channel Filter",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }
                DrawHorizontalList(timeslots, 30)
            }
        }
    }


    private var channelProgramData: ArrayList<ChannelProgramData> = ArrayList()

    private var timeslots: ArrayList<String> = ArrayList()

    private fun initialize() {
        for (row in 1..400) {
            val name = "C$row"
            val programList: ArrayList<String> = ArrayList()
            for (col in 1..672) {
                programList.add(String.format("P-%d-%d", row, col))
            }
            channelProgramData.add(ChannelProgramData(name, programList))
        }
        for (t in 1..100) {
            timeslots.add("T$t")
        }
    }
}

val TOP_BAR_HEIGHT = 56.dp
val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

data class ChannelProgramData(
    val name: String,
    val programList: ArrayList<String>
)