package org.wspcgir.strong_giraffe.views.set

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.wspcgir.strong_giraffe.model.Group
import org.wspcgir.strong_giraffe.model.Intensity
import org.wspcgir.strong_giraffe.model.Reps
import org.wspcgir.strong_giraffe.model.Weight
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.ExerciseVariationId
import org.wspcgir.strong_giraffe.model.ids.SetId
import org.wspcgir.strong_giraffe.model.set.SetSummary
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme
import org.wspcgir.strong_giraffe.views.FIELD_NAME_FONT_SIZE
import org.wspcgir.strong_giraffe.views.PreviousSetButton
import org.wspcgir.strong_giraffe.views.SMALL_NAME_FONT_SIZE
import java.time.OffsetDateTime

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
public fun DaySetsCard(
    day: Group<Group<SetSummary>>,
    goto: (SetSummary) -> Unit,
    showExerciseNames: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.Companion.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val time = day.first.first.time
            val monthName = time.month.name
                .lowercase()
                .replaceFirstChar { it.uppercaseChar() }
            Text(
                text = "$monthName ${time.dayOfMonth}, ${time.year}",
                fontSize = FIELD_NAME_FONT_SIZE
            )

            listOf(day.first).plus(day.rest).forEach { sets ->
                if (showExerciseNames) {
                    ExerciseName(sets.first.exerciseName, sets.first.variationName)
                }

                CompositionLocalProvider(
                    LocalLayoutDirection.provides(LayoutDirection.Ltr)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.Absolute.Left
                    ) {
                        listOf(sets.first).plus(sets.rest).forEach { set ->
                            PreviousSetButton(set.reps, set.weight, set.intensity) {
                                goto(set)
                            }
                            Spacer(modifier = Modifier.Companion.width(5.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseName(name: String, variation: String?) {
    var open by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = variation != null,
                onClick = { open = !open }
            )
    ){
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(name, fontSize = SMALL_NAME_FONT_SIZE)
            if (variation != null) {
                val icon = if(open) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Icon(
                    imageVector = icon,
                    contentDescription = "Display exercise variation name",
                )
            }
        }
        if (open && variation != null) {
            Text(variation, fontSize = SMALL_NAME_FONT_SIZE)
        }
    }
}

@Preview(
    name = "Dark",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
@Preview(
    name = "Light",
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
@Composable
private fun Preview() {
    val proto = SetSummary(
        id = SetId("a"),
        exerciseName = "Some Really Long Exercise",
        exerciseId = ExerciseId("a"),
        variationName = "Var",
        variationId = ExerciseVariationId("a"),
        reps = Reps(54),
        weight = Weight(10.0f),
        time = OffsetDateTime.now(),
        intensity = Intensity.Easy
    )
    val proto1 = proto.copy(
        id = SetId("B"),
        exerciseId = ExerciseId("B"),
        exerciseName = "Another Exercise"
    )
    StrongGiraffeTheme {
        DaySetsCard(
            day = Group(
                first = Group (first = proto, emptyList()),
                rest = listOf(Group(first = proto1, emptyList()))
            ),
            goto = { },
        )
    }
}
