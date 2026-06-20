package org.wspcgir.strong_giraffe.views.plot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.listOf

data class SetRecord(
    val dateTime: LocalDateTime,
    val weight: Float,
    val difficulty: Int
)

var difficultyColorsKey = ExtraStore.Key<List<Fill>>()

@Composable
fun BarPlot(workoutSets: List<SetRecord>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val formatter = remember { DateTimeFormatter.ofPattern("MM/dd HH:mm") }
    val xToDateMap = remember(workoutSets) {
        workoutSets.associateBy ({ it.dateTime }, {it})
    }
    val difficultyColors = remember(workoutSets) {
        workoutSets.map { set ->
            when (set.difficulty) {
                1 -> Fill(Color.Green.toArgb())
                2 -> Fill(Color.Yellow.toArgb())
                3 -> Fill(Color.Red.toArgb())
                else -> Fill(Color.Black.toArgb())
            }
        }
    }
    var isChartLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(workoutSets) {
        modelProducer.runTransaction {
            columnSeries{ series(workoutSets.map { it.weight }) }
            extras { store ->
                store[difficultyColorsKey] = difficultyColors
            }
        }
        isChartLoaded = true
    }
    key(isChartLoaded) {
        Box {
            if (isChartLoaded) {
                val baseLineComponent = rememberLineComponent(thickness = 16.dp)
                CartesianChartHost(
                    modifier = Modifier.fillMaxSize(),
                    modelProducer = modelProducer,
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = remember(baseLineComponent) {
                                object : ColumnCartesianLayer.ColumnProvider {
                                    override fun getColumn(
                                        entry: ColumnCartesianLayerModel.Entry,
                                        seriesIndex: Int,
                                        extraStore: ExtraStore
                                    ): LineComponent {
                                        val colors = extraStore[difficultyColorsKey]
                                        val targetColor = colors.getOrNull(entry.x.toInt()) ?: Fill(Color.Black.toArgb())
                                        return baseLineComponent.copy(
                                            fill = targetColor,
                                        )
                                    }

                                    override fun getWidestSeriesColumn(
                                        seriesIndex: Int,
                                        extraStore: ExtraStore
                                    ): LineComponent {
                                        return baseLineComponent
                                    }
                                }
                            },
                        ),
                        startAxis = VerticalAxis.start(),
                        bottomAxis = HorizontalAxis.bottom()
                    ),
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading plot...")
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun preview(){
    val start = LocalDateTime.now()
    BarPlot(
        listOf(
            SetRecord(start.minusMinutes(10), 10f, 1),
            SetRecord(start.minusMinutes(7), 15f, 2),
            SetRecord(start.minusMinutes(3), 20f, 3)
            )
    )
}