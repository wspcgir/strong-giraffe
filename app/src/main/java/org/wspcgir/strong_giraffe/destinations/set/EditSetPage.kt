package org.wspcgir.strong_giraffe.destinations.set

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.wspcgir.strong_giraffe.model.Comment
import org.wspcgir.strong_giraffe.model.Intensity
import org.wspcgir.strong_giraffe.model.Reps
import org.wspcgir.strong_giraffe.model.set.SetContent
import org.wspcgir.strong_giraffe.model.Time
import org.wspcgir.strong_giraffe.model.Weight
import org.wspcgir.strong_giraffe.model.set.WorkoutSet
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.ExerciseVariationId
import org.wspcgir.strong_giraffe.model.ids.LocationId
import org.wspcgir.strong_giraffe.model.ids.SetId
import org.wspcgir.strong_giraffe.repository.AppRepository
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme
import org.wspcgir.strong_giraffe.views.FIELD_NAME_FONT_SIZE
import org.wspcgir.strong_giraffe.views.IntField
import org.wspcgir.strong_giraffe.views.ModalDrawerScaffold
import org.wspcgir.strong_giraffe.views.PreviousSetButton
import org.wspcgir.strong_giraffe.views.SelectionField
import org.wspcgir.strong_giraffe.views.intensityColor
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import kotlin.math.roundToInt

@Serializable
@Parcelize
data class EditSet(val id: SetId, val locked: Boolean) : Parcelable

const val NUM_PREVIOUS_SETS = 6

class EditSetPageViewModel() : ViewModel() {

    private var dataMut: MutableState<Data> = mutableStateOf(Data.Empty)

    val data: State<Data>
        get() = dataMut

    fun init(
        repo: AppRepository,
        dest: NavController,
        setId: SetId,
        locked: Boolean,
        refreshSetList: () -> Unit
    ) {
        when (data.value) {
            is Data.Empty -> {
                viewModelScope.launch {
                    val set = repo.getSetFromId(setId)
                    val previousSets = repo.setForExerciseAndVariationBefore(
                        cutoff = set.time,
                        set.exercise,
                        set.variation,
                        NUM_PREVIOUS_SETS
                    )
                    Log.d("EditSetPage", "Loaded ${previousSets.size} previous sets")
                    dataMut.value = Data.Loaded(
                        repo = repo,
                        dest = dest,
                        scope = viewModelScope,
                        previousSetsMut = mutableStateOf(previousSets),
                        inProgressMut = mutableStateOf(set),
                        lockedMut = mutableStateOf(locked),
                        refreshSetList = refreshSetList
                    )
                }
            }
            else -> { }
        }
    }

    sealed interface Data {
        data class Loaded(
            val repo: AppRepository,
            val dest: NavController,
            val scope: CoroutineScope,
            private val previousSetsMut: MutableState<List<WorkoutSet>>,
            private val inProgressMut: MutableState<SetContent>,
            private val lockedMut: MutableState<Boolean>,
            private val refreshSetList: () -> Unit,
        ) : Data {
            val locked: State<Boolean>
                get() = lockedMut

            val inProgress: State<SetContent>
                get() = inProgressMut

            val previousSets: State<List<WorkoutSet>>
                get() = previousSetsMut

            fun goBack() {
                dest.popBackStack()
            }

            fun submit() {
                scope.launch {
                    repo.updateWorkoutSet(
                        id = inProgress.value.id,
                        exercise = inProgress.value.exercise,
                        variation = inProgress.value.variation,
                        reps = inProgress.value.reps,
                        weight = inProgress.value.weight,
                        intensity = inProgress.value.intensity,
                        comment = inProgress.value.comment,
                        time = inProgressMut.value.time
                    )
                    refreshSetList()
                    Log.i("EditSetpage", "Submitted Set")
                }
            }

            fun changeExercise(exercise: ExerciseId, exerciseName: String) {
                Log.d("EditSetPage", "Changing exercise")
                scope.launch {
                    // Initially assume no variation until selected
                    inProgressMut.value = inProgressMut.value.copy(
                        variation = null,
                        variationName = "N/A",
                        exercise = exercise,
                        exerciseName = exerciseName
                    )
                    Log.d("EditSetPage", "exercise and variation updated")
                    previousSetsMut.value = repo.setForExerciseAndVariationBefore(
                        inProgress.value.time,
                        exercise,
                        inProgress.value.variation,
                        NUM_PREVIOUS_SETS
                    )
                    Log.d("EditSetPage", "previous sets updated")
                }
            }

            fun changeVariation(variation: ExerciseVariationId?, name: String?) {
                scope.launch {
                    inProgressMut.value = inProgress.value.copy(
                        variation = variation,
                        variationName = name
                    )
                    previousSetsMut.value = repo.setForExerciseAndVariationBefore(
                        inProgress.value.time,
                        inProgress.value.exercise,
                        variation,
                        NUM_PREVIOUS_SETS
                    )
                }
            }

            fun changeReps(new: Reps) {
                inProgressMut.value = inProgress.value.copy(reps = new)
            }

            fun changeWeight(new: Weight) {
                inProgressMut.value = inProgress.value.copy(weight = new)
            }

            fun changeIntensity(new: Intensity) {
                inProgressMut.value = inProgress.value.copy(intensity = new)
            }

            fun changeComment(new: Comment) {
                inProgressMut.value = inProgress.value.copy(comment = new)
            }

            fun delete() {
                scope.launch {
                    repo.deleteWorkoutSet(inProgress.value.id)
                }
                dest.popBackStack()
            }

            fun gotoSet(set: SetId) {
                submit()
                dest.navigate(EditSet(set, true))
            }

            fun toggleSetLock(new: Boolean) {
                lockedMut.value = new
            }

            fun selectExercise() {
                dest.navigate(SelectExercise)
            }

            fun selectVariation() {
                dest.navigate(SelectVariation)
            }

        }

        data object Empty : Data
    }

    @Deprecated("Changing location shouldn't do anything now")
    fun changeLocation(location: LocationId?) {
    }
}

@Composable
fun EditSetPage(view: EditSetPageViewModel) {
    when (val data = view.data.value) {
        is EditSetPageViewModel.Data.Empty -> CircularProgressIndicator()
        is EditSetPageViewModel.Data.Loaded -> {
            Page(
                locked = data.locked.value,
                toggleSetLock = data::toggleSetLock,
                submit = data::submit,
                goBack = data::goBack,
                delete = data::delete,
                selectExercise = data::selectExercise,
                selectVariation = data::selectVariation,
                changeReps = data::changeReps,
                changeWeight = data::changeWeight,
                changeIntensity = data::changeIntensity,
                changeComment = data::changeComment,
                gotoSet = data::gotoSet,
                starting = data.inProgress,
                previousSets = data.previousSets
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    locked: Boolean,
    toggleSetLock: (Boolean) -> Unit,
    starting: State<SetContent>,
    submit: () -> Unit,
    goBack: () -> Unit,
    delete: () -> Unit,
    selectExercise: () -> Unit,
    selectVariation: () -> Unit,
    changeReps: (Reps) -> Unit,
    changeWeight: (Weight) -> Unit,
    changeIntensity: (Intensity) -> Unit,
    changeComment: (Comment) -> Unit,
    gotoSet: (SetId) -> Unit,
    previousSets: State<List<WorkoutSet>>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val validReps = remember { mutableStateOf(true) }
    val validWeight = remember { mutableStateOf(true) }

    ModalDrawerScaffold(
        title = if (locked) {
            "View Set"
        } else {
            "Edit Set"
        },
        drawerContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .weight(0.5f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set Locked", modifier = Modifier.padding(end = 10.dp))
                    Switch(
                        checked = locked,
                        onCheckedChange = toggleSetLock,
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
                Button(
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    onClick = delete
                ) {
                    Text("Delete")
                    Spacer(modifier = Modifier.fillMaxWidth(0.03f))
                    Icon(Icons.Default.Delete, contentDescription = "delete set")
                }
            }
        },
        actionButton = {
            FloatingActionButton(onClick = {
                submit()
                goBack()
            }) {
                if (locked) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Set Locked")
                } else if (validReps.value && validWeight.value) {
                    Icon(Icons.Default.Done, contentDescription = "Save Set")
                } else {
                    Icon(Icons.Default.Warning, contentDescription = "Invalid fields")
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            val zone = TimeZone.getDefault().toZoneId()
            val date = OffsetDateTime.ofInstant(starting.value.time.value, zone)
            val dateFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
            val timeFormat = DateTimeFormatter.ofPattern("HH:MM:ss")
            Text(date.format(dateFormat), fontSize = FIELD_NAME_FONT_SIZE)
            Text(date.format(timeFormat), fontSize = FIELD_NAME_FONT_SIZE)
            Card {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    SelectionField(
                        label = "Exercise",
                        text = starting.value.exerciseName,
                        onClick = selectExercise,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    SelectionField(
                        label = "Variation",
                        text = starting.value.variationName ?: "N/A",
                        onClick = selectVariation,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
            Card {
                RepsAndWeightSelector(
                    starting,
                    validReps,
                    enabled = !locked,
                    changeReps,
                    validWeight,
                    changeWeight,
                )
            }
            Card {
                IntensitySelector(changeIntensity, starting, enabled = !locked)
            }
            Card {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    TextField(
                        label = { Text("Comment") },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        enabled = !locked,
                        value = starting.value.comment.value,
                        onValueChange = { changeComment(Comment(it)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                this.item { Spacer(modifier = Modifier.width(25.dp)) }
                this.items(previousSets.value) { set ->
                    PreviousSetButton(set.reps, set.weight, set.intensity) { gotoSet(set.id) }
                }
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
        }
    }
}

@Composable
private fun IntensitySelector(
    changeIntensity: (Intensity) -> Unit,
    starting: State<SetContent>,
    enabled: Boolean = true,
) {
    var slider by remember(key1 = starting.value.intensity) {
        mutableFloatStateOf(
            Intensity.toInt(starting.value.intensity).toFloat()
        )
    }
    val columnModifier =
        if (enabled) {
            Modifier
        } else {
            Modifier.alpha(0.45f)
        }
    Column(modifier = columnModifier) {
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Spacer(modifier = Modifier.width(10.dp))
            Text("Intensity: ${starting.value.intensity}")
        }
        Slider(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = SliderDefaults.colors(
                thumbColor = intensityColor(starting.value.intensity),
                activeTrackColor = intensityColor(starting.value.intensity)
            ),
            value = slider,
            valueRange = 0f..4f,
            steps = 3,
            onValueChange = { f -> slider = f },
            onValueChangeFinished = {
                val result = Intensity.fromInt(slider.toInt())
                if (result != null) {
                    changeIntensity(result)
                }
                slider = slider.toInt().toFloat()
            }
        )
    }
}

@Composable
private fun RepsAndWeightSelector(
    starting: State<SetContent>,
    validReps: MutableState<Boolean>,
    enabled: Boolean = true,
    changeReps: (Reps) -> Unit,
    validWeight: MutableState<Boolean>,
    changeWeight: (Weight) -> Unit
) {
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                IntField(
                    label = "Reps",
                    enabled = enabled,
                    start = starting.value.reps.value,
                ) {
                    validReps.value = it != null
                    if (it != null) {
                        changeReps(Reps(it))
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                IntField(
                    label = "Weight",
                    // TODO: Create a Float field
                    start = starting.value.weight.value.roundToInt(),
                    onChange = {
                        validWeight.value = it != null
                        if (it != null) {
                            // TODO: Remove casting
                            changeWeight(Weight(it.toFloat()))
                        }
                    },
                    enabled = enabled
                )
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
    val setTemplate =
        SetContent(
            id = SetId("a"),
            exercise = ExerciseId("a"),
            exerciseName = "foo",
            variation = ExerciseVariationId("variationA"),
            variationName = "barbell",
            reps = Reps(0),
            weight = Weight(0.0f),
            time = Time(Instant.now()),
            intensity = Intensity.Normal,
            comment = Comment("")
        )
    val prevSetTemplate =
        WorkoutSet(
            id = SetId("a"),
            exercise = ExerciseId("a"),
            variation = ExerciseVariationId("variationA"),
            reps = Reps(0),
            weight = Weight(0.0f),
            time = Time(Instant.now()),
            intensity = Intensity.Normal,
            comment = Comment(""),
            location = null,
            equipment = null
        )
    StrongGiraffeTheme {
        Page(
            locked = true,
            toggleSetLock = { },
            starting = remember { mutableStateOf(setTemplate) },
            submit = { },
            goBack = { },
            delete = { },
            selectExercise = { },
            selectVariation = { },
            changeReps = { },
            changeWeight = { },
            changeIntensity = { },
            changeComment = { },
            gotoSet = { },
            previousSets = remember {
                mutableStateOf(
                    listOf(prevSetTemplate, prevSetTemplate, prevSetTemplate, prevSetTemplate, prevSetTemplate)
                )
            }
        )
    }
}