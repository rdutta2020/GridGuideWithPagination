// Checked


package com.jetpack.gridguidedemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jetpack.gridguidedemo.ui.theme.GridGuideDemoTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var state: ScrollState
    var fixedColumnWidth: Int = 0
    var maxProgramCellWidth: Int = 0
    val timeslotsCount: Int = 100
    lateinit var viewModel: MainViewModel
    var snapStepSize : Float = -1F

    @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter",
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            state = rememberScrollState()
            fixedColumnWidth = 128
            maxProgramCellWidth = LocalConfiguration.current.screenWidthDp - fixedColumnWidth
            GridGuideDemoTheme(darkTheme = false) {
                viewModel = viewModel<MainViewModel>()
                val viewModelState = viewModel.state

                val lazyListState = rememberLazyListState()
                Scaffold(
                    content = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            MainContent(lazyListState = lazyListState, viewModelState, viewModel)
                            TopBar(lazyListState = lazyListState)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun MainContent(
        lazyListState: LazyListState,
        viewModelState: MainViewModel.ScreenState,
        viewModel: MainViewModel
    ) {
        val padding by animateDpAsState(
            targetValue = if (lazyListState.isScrolled) 0.dp else TOP_BAR_HEIGHT,
            animationSpec = tween(durationMillis = 300)
        )

        Column(
            modifier = Modifier.padding(top = padding)
        ) {
            //DrawHeader()
            DrawGrid(lazyListState, viewModel, viewModelState)
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
    fun DrawGrid(
        scrollState: LazyListState,
        viewModel: MainViewModel,
        viewModelState: MainViewModel.ScreenState
    ) {
        LazyColumn(
            //  state = scrollState
        ) {
            items(viewModelState.items.size) { rowNumber ->// "rowNumber" index starts from 0
                Log.d(
                    "GrideGuide",
                    "Calling DrawGridRowItem RowNumber : $rowNumber ProgramList : ${viewModelState.items[rowNumber]}"
                )
                DrawGridRowItem(rowNumber)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DrawHorizontalList(
        rowNumber: Int,
        height: Int
    ) {
        val lazyListState = rememberLazyListState()
        val snappingLayout =
            remember(lazyListState) {
                CreateSnapLayoutInfoProvider(
                    MainViewModel.PAGESIZE * (viewModel.state.page[rowNumber] + 1),
                    state
                )
            }
        val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

        LazyRow(
            modifier = Modifier
                .horizontalScroll(state)
                .width((MainViewModel.PAGESIZE * (viewModel.state.page[rowNumber] + 1) * maxProgramCellWidth).dp),
                state = lazyListState,
                flingBehavior = flingBehavior
        ) {
            Log.d(
                "GrideGuide",
                "DrawHorizontalList rowNumber: $rowNumber ItemSize: " + viewModel.state.items[rowNumber].size
            )

            items(viewModel.state.items[rowNumber].size) { rowItemIndex ->
                val item = viewModel.state.items[rowNumber][rowItemIndex]
                if (rowItemIndex >= viewModel.state.items[rowNumber].size - 1
                    && !viewModel.state.endReached
                    && !viewModel.state.isLoading
                    && state.isScrollInProgress
                ) {
                    Log.d(
                        "GrideGuide",
                        "Calling loadNextItems for rowNumber : $rowNumber rowItemIndex: $rowItemIndex"
                    )
                    viewModel.loadNextItems(rowNumber)
                }
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
            // we are saving snapStepSize once at the beginning when first snap happens
            // not changing this value during each snap(which should be ideal) because when horizontal pagination happens this value does not change as expected (needs to debug in future)
            if(snapStepSize == -1F){// not initialized yet
                snapStepSize = scrollState.maxValue.toFloat() / (itemCount - 1)
            }
            return snapStepSize
        }

        override fun Density.calculateSnappingOffsetBounds(): ClosedFloatingPointRange<Float> {
            val bound0 = -scrollState.value % calculateSnapStepSize()
            val bound1 = calculateSnapStepSize() + bound0

            return (if (bound0 >= 0 && bound1 < 0) bound1.rangeTo(bound0) else bound0.rangeTo(bound1))
        }
    }

    @Composable
    fun DrawRowListItem(
        name: String,
        height: Int
    ) {
        var fraction = 1F

        // here starts hack to make cell width dynamic based on duration
        val strs = name.split("-").toTypedArray()
        val col = strs[strs.size - 1].toInt()
        val row = strs[strs.size - 2].toInt()
        // here hack ends

        if (row % 4 == 0 && col % 2 != 0) {
            fraction = 0.5F
        }

        Box(
            modifier = Modifier
                .width((maxProgramCellWidth * fraction).dp)
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
    fun DrawGridRowItem(rowNumber: Int) {
        val channelNumber = rowNumber+1
        Box() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(fixedColumnWidth.dp)
                        .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                        .height(60.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        //text = item.name,
                        text = "C$channelNumber",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }
                DrawHorizontalList(rowNumber, 60)
            }
        }
    }


/*    @Composable
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
                DrawHorizontalList(
                    30,
                    viewModelState = MainViewModel.ScreenState(),
                    viewModel = MainViewModel()
                )
            }
        }
    }*/
}

val TOP_BAR_HEIGHT = 56.dp
val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0