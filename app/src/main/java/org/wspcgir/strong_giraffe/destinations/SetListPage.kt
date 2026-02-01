package org.wspcgir.strong_giraffe.destinations

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import org.wspcgir.strong_giraffe.model.*
import org.wspcgir.strong_giraffe.model.ids.*
import org.wspcgir.strong_giraffe.repository.AppRepository
import org.wspcgir.strong_giraffe.views.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.wspcgir.strong_giraffe.destinations.edit_set.EditSet
import org.wspcgir.strong_giraffe.model.set.SetSummary
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme
import java.time.OffsetDateTime

@Serializable
object SetList

abstract class SetListPageViewModel : ViewModel() {
    abstract val sets: List<SetSummary>
    abstract val templateExercise: ExerciseId
    abstract fun new()
    abstract fun goto(id: SetSummary)
}

@Composable
fun RegisterSetListPage(repo: AppRepository, dest: NavController) {
    var templateMuscle by remember { mutableStateOf<MuscleId?>(null) }
    var templateExercise by remember { mutableStateOf<ExerciseId?>(null) }
    var setSummaries by remember { mutableStateOf<List<SetSummary>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(templateMuscle, templateExercise) {
        val exercise = repo.getExercises().firstOrNull()
        templateMuscle = exercise?.muscle ?: repo.getMuscles().firstOrNull()?.id
        templateExercise = exercise?.id
        setSummaries = repo.getSetSummaries()
    }

    if (templateMuscle == null) {
        MuscleEditRedirection(scope, repo, dest)
    } else if (templateExercise == null) {
        ExerciseEditRedirection(templateMuscle!!, scope, repo, dest)
    } else {

        SetListPage(object : SetListPageViewModel() {
            override val sets: List<SetSummary>
                get() = setSummaries
            override val templateExercise: ExerciseId
                get() = templateExercise!!

            override fun new() {
                viewModelScope.launch {
                    val set = repo.newWorkoutSet(templateExercise!!)
                    val latest = repo.latestSetNot(set.id)
                    if (latest != null) {
                        Log.i("NEW SET", "Using previous set '${latest.id}'")
                        repo.updateWorkoutSet(
                            original = set,
                            location = latest.location,
                            exercise = latest.exercise,
                            variation = latest.variation,
                            reps = latest.reps,
                            weight = latest.weight
                        )
                    } else {
                        Log.i("NEW SET", "No previous set available.")
                    }
                    dest.navigate(EditSet(id = set.id, false))
                }
            }

            override fun goto(id: SetSummary) {
                viewModelScope.launch {
                    dest.navigate(EditSet(id = id.id, false))
                }
            }
        })
    }
}

@Composable
fun SetListPage(view: SetListPageViewModel) {
    Page(
        sets = view.sets,
        gotoNew = view::new,
        goto = view::goto
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun Page(
    sets: List<SetSummary>,
    gotoNew: () -> Unit,
    goto: (SetSummary) -> Unit
) {
    val days: List<Group<Group<SetSummary>>> =
        Group.fromList(sets.sortedBy { it.time }.asReversed()) {
            it.time
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        }.map { group ->
            group.innerGroup { it.exerciseId }
        }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { gotoNew() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Sets", fontSize = PAGE_TITLE_FONT_SIZE)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (days.isNotEmpty()) {
                    LazyColumn { this.items(days) { day -> DaySetsCard(day, goto) } }
                } else {
                    Text("There's nothing here yet")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun DaySetsCard(
    day: Group<Group<SetSummary>>,
    goto: (SetSummary) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
                Text(
                    sets.first.exerciseName,
                    fontSize = SMALL_NAME_FONT_SIZE
                )

                FlowRow {
                    listOf(sets.first).plus(sets.rest).forEach { set ->
                        PreviousSetButton(set.reps, set.weight, set.intensity)  {
                            goto(set)
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }
            }
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
    StrongGiraffeTheme {
        val curl =
            SetSummary(
                id = SetId("a"),
                reps = Reps(10),
                weight = Weight(140f),
                time = OffsetDateTime.now(),
                intensity = Intensity.Easy,
                exerciseName = "Bicep Curls",
                exerciseId = ExerciseId("a")
            )
        val extension = curl.copy(
            exerciseName = "Tricep Extension",
            exerciseId = ExerciseId("b"),
            time = curl.time.plusSeconds(120)
        )
        Page(
            sets = listOf(
                curl.copy(
                    time = curl.time.minusSeconds(60),
                    reps = Reps(5),
                    intensity = Intensity.NoActivation
                ),
                curl.copy(
                    time = curl.time.minusSeconds(120),
                    reps = Reps(20),
                    intensity = Intensity.Pain
                ),
                curl.copy(intensity = Intensity.Normal),
                curl.copy(intensity = Intensity.EarlyFailure),
                curl,
                extension,
                extension.copy(
                    intensity = Intensity.Normal,
                    time = extension.time.plusSeconds(45)
                ),
                curl.copy(time = curl.time.minusDays(1)),
                curl.copy(time = curl.time.minusDays(2)),
                curl.copy(time = curl.time.minusDays(3)),
            ),
            gotoNew = { },
            goto = { }
        )
    }
}