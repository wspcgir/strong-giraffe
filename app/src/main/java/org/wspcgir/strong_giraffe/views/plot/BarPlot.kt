package org.wspcgir.strong_giraffe.views.plot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.listOf

data class SeriesValue(
    val label: String,
    val height: Float,
    val fill: Fill
)

var difficultyColorsKey = ExtraStore.Key<List<Fill>>()

@Composable
fun BarPlot(workoutSets: List<SeriesValue>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    var isChartLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(workoutSets) {
        setupCharSeries(modelProducer, workoutSets)
        isChartLoaded = true
    }
    Box {
        if (isChartLoaded) {
            Chart(modelProducer, workoutSets, modifier)
        } else {
            ChartLoading(modifier)
        }
    }
}

private suspend fun setupCharSeries(
    modelProducer: CartesianChartModelProducer,
    workoutSets: List<SeriesValue>,
): Unit {
    modelProducer.runTransaction {
        columnSeries { series(workoutSets.map { it.height }) }
        extras { store ->
            store[difficultyColorsKey] = workoutSets.map { it.fill }
        }
    }
}

@Composable
private fun ChartLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Loading plot...")
    }
}

@Composable
private fun Chart(
    modelProducer: CartesianChartModelProducer,
    workoutSets: List<SeriesValue>,
    modifier: Modifier = Modifier
) {
    val baseLineComponent = rememberLineComponent(
        strokeThickness = 0.dp,
        strokeFill = Fill(Color.Black.toArgb())
    )
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            modifier = modifier,
            modelProducer = modelProducer,
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = remember(baseLineComponent) {
                        columnProvider(baseLineComponent)
                    },
                    columnCollectionSpacing = 0.dp,
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = bottomAxis(workoutSets)
            ),
        )
    }
}

@Composable
private fun bottomAxis(workoutSets: List<SeriesValue>): HorizontalAxis<Axis.Position.Horizontal.Bottom> =
    HorizontalAxis.rememberBottom(
        valueFormatter = { _, value, _ ->
            workoutSets.getOrNull(value.toInt())?.label ?: "-"
        }
    )

private fun columnProvider(baseLineComponent: LineComponent): ColumnCartesianLayer.ColumnProvider =
    object : ColumnCartesianLayer.ColumnProvider {
        override fun getColumn(
            entry: ColumnCartesianLayerModel.Entry,
            seriesIndex: Int,
            extraStore: ExtraStore
        ): LineComponent {
            val colors = extraStore[difficultyColorsKey]
            val targetColor = colors.getOrNull(entry.x.toInt())
                ?: Fill(Color.Black.toArgb())
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun preview(){
    val start = LocalDateTime.now()
    val green = Fill(Color.Green.toArgb())
    val yellow = Fill(Color(0xFFFF9800).toArgb())
    val red = Fill(Color.Red.toArgb())
    val format = DateTimeFormatter.ofPattern("MM/dd")
    StrongGiraffeTheme() {
        BarPlot(
            (0..20).flatMap { i ->
                val day = start.plusWeeks(i.toLong())
                listOf(
                    SeriesValue(day.plusMinutes(0).format(format), (i*5) + 10f, green),
                    SeriesValue(day.plusMinutes(5).format(format), (i*5) + 10f, green),
                    SeriesValue(day.plusMinutes(10).format(format), (i*5) + 10f, listOf(yellow, green).random()),
                    SeriesValue(day.plusDays(1).format(format), 0f, green),
                    SeriesValue(day.plusDays(2).format(format), 0f, green),
                    SeriesValue(day.plusDays(3).format(format), 0f, green),
                    SeriesValue(day.plusDays(4).plusMinutes(0).format(format), (i*5) + 10f, green),
                    SeriesValue(day.plusDays(4).plusMinutes(5).format(format), (i*5) + 10f, green),
                    SeriesValue(day.plusDays(4).plusMinutes(10).format(format), (i*5) + 10f, listOf(yellow, green, red).random()),
                    SeriesValue(day.plusDays(5).format(format), 0f, green),
                    SeriesValue(day.plusDays(6).format(format), 0f, green),
                    SeriesValue(day.plusDays(7).format(format), 0f, green),
                )
            }.take(50),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)
        )
    }
}