package org.wspcgir.strong_giraffe.destinations.set

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.wspcgir.strong_giraffe.model.Group
import org.wspcgir.strong_giraffe.model.Intensity
import org.wspcgir.strong_giraffe.model.Reps
import org.wspcgir.strong_giraffe.model.Weight
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.MuscleId
import org.wspcgir.strong_giraffe.model.ids.SetId
import org.wspcgir.strong_giraffe.model.set.SetSummary
import org.wspcgir.strong_giraffe.repository.AppRepository
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme
import org.wspcgir.strong_giraffe.views.ExerciseEditRedirection
import org.wspcgir.strong_giraffe.views.FIELD_NAME_FONT_SIZE
import org.wspcgir.strong_giraffe.views.MuscleEditRedirection
import org.wspcgir.strong_giraffe.views.PAGE_TITLE_FONT_SIZE
import org.wspcgir.strong_giraffe.views.PreviousSetButton
import org.wspcgir.strong_giraffe.views.SMALL_NAME_FONT_SIZE
import java.time.OffsetDateTime

class SetListPageViewModel : ViewModel() {

    private val _data: MutableState<Data> = mutableStateOf(Data.Empty)

    val data: State<Data>
        get() = _data

    fun init(
        repo: AppRepository,
        navController: NavController
    ) {
        viewModelScope.launch {
            val summaries = repo.getSetSummaries()
            val latest = summaries.firstOrNull()
            if (latest != null) {
                _data.value = Data.Loaded(
                    scope = viewModelScope,
                    repo = repo,
                    navController = navController,
                    _setSummaries = mutableStateOf(emptyList()),
                    _currentExercise = mutableStateOf(latest.exerciseId)
                )
            } else {
                val exercise = repo.getExercises().firstOrNull()
                if (exercise != null) {
                    _data.value = Data.Loaded(
                        scope = viewModelScope,
                        repo = repo,
                        navController = navController,
                        _setSummaries = mutableStateOf(emptyList()),
                        _currentExercise = mutableStateOf(exercise.id)
                    )
                } else {
                    val muscle = repo.getMuscles().firstOrNull()
                    _data.value = if (muscle == null) {
                        Data.NoMuscles(viewModelScope, repo, navController)
                    } else {
                        Data.NoExercises(viewModelScope, repo, navController, muscle.id)
                    }
                }
            }
        }
    }

    sealed interface Data {
        data object Empty : Data

        data class NoMuscles(
            val scope: CoroutineScope,
            val repo: AppRepository,
            val navController: NavController
        ) : Data

        data class NoExercises(
            val scope: CoroutineScope,
            val repo: AppRepository,
            val navController: NavController,
            val muscle: MuscleId
        ) : Data

        data class Loaded(
            val scope: CoroutineScope,
            val repo: AppRepository,
            val navController: NavController,
            private val _setSummaries: MutableState<List<SetSummary>>,
            private val _currentExercise: MutableState<ExerciseId>
        ) : Data {

            val sets: State<List<SetSummary>>
                get() = _setSummaries

            fun new() {
                scope.launch {
                    val set = repo.newWorkoutSet(_currentExercise.value)
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
                    navController.navigate(EditSet(id = set.id, false))
                }
            }

            fun goto(id: SetSummary) {
                scope.launch {
                    navController.navigate(EditSet(id = id.id, false))
                }
            }

            fun refresh() {
                scope.launch {
                    _setSummaries.value = repo.getSetSummaries()
                }
            }
        }
    }
}


@Composable
fun RegisterSetListPage(viewModel: SetListPageViewModel) {
    when (val data = viewModel.data.value) {
        SetListPageViewModel.Data.Empty -> {
            CircularProgressIndicator()
        }

        is SetListPageViewModel.Data.NoMuscles -> {
            MuscleEditRedirection(data.scope, data.repo, data.navController)
        }

        is SetListPageViewModel.Data.NoExercises -> {
            ExerciseEditRedirection(data.muscle, data.scope, data.repo, data.navController)
        }

        is SetListPageViewModel.Data.Loaded -> {
            Page(
                sets = data.sets,
                gotoNew = data::new,
                goto = data::goto
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Page(
    sets: State<List<SetSummary>>,
    gotoNew: () -> Unit,
    goto: (SetSummary) -> Unit
) {
    val days: List<Group<Group<SetSummary>>> =
        Group.fromList(sets.value.sortedBy { it.time }.asReversed()) {
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

                CompositionLocalProvider(
                    LocalLayoutDirection.provides(LayoutDirection.Rtl)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.Absolute.Left
                    ) {
                        listOf(sets.first).plus(sets.rest).reversed().forEach { set ->
                            PreviousSetButton(set.reps, set.weight, set.intensity) {
                                goto(set)
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                        }
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
            sets = remember {
                mutableStateOf(
                    listOf(
                        curl.copy(
                            time = curl.time.minusSeconds(20),
                            reps = Reps(5),
                            intensity = Intensity.Normal
                        ),
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
                    )
                )
            },
            gotoNew = { },
            goto = { }
        )
    }
}