package org.wspcgir.strong_giraffe.views.plot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.listOf
import kotlin.random.Random

data class SetRecord(
    val dateTime: String,
    val weight: Float,
    val fill: Fill
)

var difficultyColorsKey = ExtraStore.Key<List<Fill>>()

@Composable
fun BarPlot(workoutSets: List<SetRecord>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    var isChartLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(workoutSets) {
        modelProducer.runTransaction {
            columnSeries{ series(workoutSets.map { it.weight }) }
            extras { store ->
                store[difficultyColorsKey] = workoutSets.map { it.fill }
            }
        }
        isChartLoaded = true
    }
    key(isChartLoaded) {
        Box {
            if (isChartLoaded) {
                val baseLineComponent = rememberLineComponent(
                    strokeThickness = 0.dp,
                    strokeFill = Fill(Color.Black.toArgb())
                )
                CartesianChartHost(
                    modifier = Modifier.fillMaxHeight(0.5f).fillMaxWidth(),
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
                            columnCollectionSpacing = 0.dp,
                        ),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = { _, value, _ ->
                                workoutSets.getOrNull(value.toInt())?.dateTime ?: "-"
                            }
                        )
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
    val green = Fill(Color.Green.toArgb())
    val yellow = Fill(Color.Yellow.toArgb())
    val red = Fill(Color.Red.toArgb())
    val format = DateTimeFormatter.ofPattern("MM/dd")
    BarPlot(
        (0..20).flatMap { i ->
            val day = start.plusWeeks(i.toLong())
            listOf(
                SetRecord(day.plusMinutes(0).format(format), (i*5) + 10f, green),
                SetRecord(day.plusMinutes(5).format(format), (i*5) + 10f, green),
                SetRecord(day.plusMinutes(10).format(format), (i*5) + 10f, listOf(yellow, green).random()),
                SetRecord(day.plusDays(1).format(format), 0f, green),
                SetRecord(day.plusDays(2).format(format), 0f, green),
                SetRecord(day.plusDays(3).format(format), 0f, green),
                SetRecord(day.plusDays(4).plusMinutes(0).format(format), (i*5) + 10f, green),
                SetRecord(day.plusDays(4).plusMinutes(5).format(format), (i*5) + 10f, green),
                SetRecord(day.plusDays(4).plusMinutes(10).format(format), (i*5) + 10f, listOf(yellow, green).random()),
                SetRecord(day.plusDays(5).format(format), 0f, green),
                SetRecord(day.plusDays(6).format(format), 0f, green),
                SetRecord(day.plusDays(7).format(format), 0f, green),
            ).take(100)
        }
    )
}