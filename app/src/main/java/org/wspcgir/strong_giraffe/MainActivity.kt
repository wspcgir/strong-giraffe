package org.wspcgir.strong_giraffe

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.wspcgir.strong_giraffe.destinations.EditEquipment
import org.wspcgir.strong_giraffe.destinations.EditEquipmentPage
import org.wspcgir.strong_giraffe.destinations.EditEquipmentPageViewModel
import org.wspcgir.strong_giraffe.destinations.EditExercise
import org.wspcgir.strong_giraffe.destinations.EditExercisePage
import org.wspcgir.strong_giraffe.destinations.EditExercisePageViewModelImpl
import org.wspcgir.strong_giraffe.destinations.EditLocation
import org.wspcgir.strong_giraffe.destinations.EditLocationPage
import org.wspcgir.strong_giraffe.destinations.EditLocationPageViewModel
import org.wspcgir.strong_giraffe.destinations.EditMuscle
import org.wspcgir.strong_giraffe.destinations.EditMusclePage
import org.wspcgir.strong_giraffe.destinations.EditMusclePageViewModel
import org.wspcgir.strong_giraffe.destinations.edit_set.EditSet
import org.wspcgir.strong_giraffe.destinations.EquipmentList
import org.wspcgir.strong_giraffe.destinations.EquipmentListPage
import org.wspcgir.strong_giraffe.destinations.EquipmentListPageViewModel
import org.wspcgir.strong_giraffe.destinations.ExerciseList
import org.wspcgir.strong_giraffe.destinations.ExerciseListPage
import org.wspcgir.strong_giraffe.destinations.ExerciseListPageViewModel
import org.wspcgir.strong_giraffe.destinations.HomePage
import org.wspcgir.strong_giraffe.destinations.LocationList
import org.wspcgir.strong_giraffe.destinations.LocationListPage
import org.wspcgir.strong_giraffe.destinations.LocationListPageViewModel
import org.wspcgir.strong_giraffe.destinations.MuscleList
import org.wspcgir.strong_giraffe.destinations.MuscleListPage
import org.wspcgir.strong_giraffe.destinations.MuscleListPageViewModel
import org.wspcgir.strong_giraffe.destinations.RegisterSetListPage
import org.wspcgir.strong_giraffe.destinations.SetList
import org.wspcgir.strong_giraffe.destinations.edit_set.editSetGraph
import org.wspcgir.strong_giraffe.destinations.edit_variation.EditVariation
import org.wspcgir.strong_giraffe.destinations.edit_variation.editVariationGraph
import org.wspcgir.strong_giraffe.model.Backup
import org.wspcgir.strong_giraffe.model.Equipment
import org.wspcgir.strong_giraffe.model.Exercise
import org.wspcgir.strong_giraffe.model.Location
import org.wspcgir.strong_giraffe.model.Muscle
import org.wspcgir.strong_giraffe.model.set.MuscleSetHistory
import org.wspcgir.strong_giraffe.model.ids.EquipmentId
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.ExerciseVariationId
import org.wspcgir.strong_giraffe.model.ids.LocationId
import org.wspcgir.strong_giraffe.model.ids.MuscleId
import org.wspcgir.strong_giraffe.model.ids.SetId
import org.wspcgir.strong_giraffe.repository.AppDatabase
import org.wspcgir.strong_giraffe.repository.AppRepository
import org.wspcgir.strong_giraffe.repository.MIGRATION_1_2
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList
import kotlin.reflect.typeOf

const val JSON_MIME = "application/json"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "data.db")
                .addMigrations(MIGRATION_1_2)
                .build()
        val dao = db.dao()
        val repo = AppRepository(dao)
        val createBackup =
            registerForActivityResult(
                ActivityResultContracts.CreateDocument(JSON_MIME)
            ) { uri: Uri? ->
                uri?.let {
                    lifecycleScope.launch {
                        val backup = repo.createBackup()
                        val content = Json.encodeToString(backup)
                        saveBackupToFileSystem(it, content)
                    }
                }
            }
        setContent {
            val scope = rememberCoroutineScope()
            StrongGiraffeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainComponent(
                        repo = repo,
                        createBackup = {
                            scope.launch {
                                val now = LocalDateTime.now()
                                val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                                createBackup.launch("strong-giraffe-backup-${now.format(dateFormat)}")
                            }
                        },
                        restoreFromBackup = { uri ->
                            if (uri != null) {
                                loadBackupFromFileSystem(uri)?.let {
                                    scope.launch {
                                        repo.restoreFromBackup(it)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun saveBackupToFileSystem(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                Toast.makeText(this, "Backup created!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Error creating backup: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadBackupFromFileSystem(uri: Uri): Backup? {
        contentResolver.openInputStream(uri).use { inStream ->
            inStream?.bufferedReader()?.use { it.readText() }?.let {
                try {
                    val backup = Json.decodeFromString(Backup.serializer(), it)
                    return backup
                } catch (e: SerializationException) {
                    Toast.makeText(
                        this,
                        "Backup was malformed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        return null
    }
}

@Serializable
object Home

inline fun <reified T : Parcelable> parcelableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String): T? {
       return bundle.getParcelable(key, T::class.java)
    }

    override fun parseValue(value: String): T {
        return json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        return bundle.putParcelable(key, value)
    }

    override fun serializeAsValue(value: T): String = Json.encodeToString(value)
}

@Composable
fun MainComponent(
    repo: AppRepository,
    createBackup: () -> Unit,
    restoreFromBackup: (Uri?) -> Unit,
) {
    val navController = rememberNavController()
    val typeMap = mapOf(
        typeOf<SetId>() to parcelableType<SetId>(),
        typeOf<MuscleId>() to parcelableType<MuscleId>(),
        typeOf<LocationId>() to parcelableType<LocationId>(),
        typeOf<ExerciseId>() to parcelableType<ExerciseId>(),
        typeOf<ExerciseVariationId>() to parcelableType<ExerciseVariationId>(),
        typeOf<EquipmentId>() to parcelableType<EquipmentId>(),
        typeOf<EditLocation>() to parcelableType<EditLocation>(),
        typeOf<EditSet>() to parcelableType<EditSet>(),
        typeOf<EditMuscle>() to parcelableType<EditMuscle>(),
        typeOf<EditExercise>() to parcelableType<EditExercise>(),
        typeOf<EditVariation>() to parcelableType<EditVariation>(),
    )
    NavHost(navController = navController, startDestination = Home, typeMap = typeMap) {
        composable<Home> {
            val pickFileLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { restoreFromBackup(it) }
            HomePage(
                gotoLocationsList = {
                    navController.navigate(LocationList)
                }, gotoMuscleList = {
                    navController.navigate(MuscleList)
                }, gotoExerciseList = {
                    navController.navigate(ExerciseList)
                }, gotoSetList = {
                    navController.navigate(SetList)
                }, createBackup = createBackup,
                restoreFromBackup = { pickFileLauncher.launch(arrayOf(JSON_MIME)) }
            )
        }
        composable<LocationList> {
            var locations by remember { mutableStateOf(emptyList<Location>()) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(locations) {
                locations = repo.getLocations()
            }
            LocationListPage(object : LocationListPageViewModel() {
                override val locations: List<Location>
                    get() = locations

                override fun newLocation() {
                    scope.launch {
                        val loc = repo.newLocation()
                        navController.navigate(EditLocation(loc.name, loc.id))
                    }
                }

                override fun gotoEditLocationPage(loc: Location) {
                    navController.navigate(EditLocation(loc.name, loc.id))
                }
            })
        }
        composable<EditLocation>(typeMap = typeMap) {
            val navArgs: EditLocation = it.toRoute()
            val scope = rememberCoroutineScope()
            EditLocationPage(object : EditLocationPageViewModel() {
                override val startingName: String
                    get() = navArgs.startingName
                override val submit: (String) -> Unit
                    get() = { newName ->
                        scope.launch { repo.updateLocation(navArgs.id, newName) }
                        navController.popBackStack()
                    }

                override fun delete() {
                    viewModelScope.launch {
                        repo.deleteLocation(navArgs.id)
                    }
                    navController.popBackStack()
                }
            })
        }
        composable<EquipmentList> {
            var locations by remember { mutableStateOf(emptyList<Location>()) }
            var equipment by remember { mutableStateOf(emptyList<Equipment>()) }
            LaunchedEffect(equipment) {
                locations = repo.getLocations()
                equipment = repo.getEquipment()
            }
            EquipmentListPage(view = object : EquipmentListPageViewModel() {
                override val equipment: List<Equipment>
                    get() = equipment

                override val locations: List<Location>
                    get() = locations

                override fun gotoNew() {
                    viewModelScope.launch {
                        val new = repo.newEquipment(locations[0].id)
                        navController.navigate(
                            EditEquipment(
                                new.id,
                                new.name,
                                new.location
                            )
                        )
                    }
                }

                override fun goto(value: Equipment) {
                    navController.navigate(
                        EditEquipment(value.id, value.name, value.location)
                    )
                }

                override fun redirectToCreateLocation() {
                    locationRedirect(viewModelScope, repo, navController)
                }
            })
        }
        composable<EditEquipment>(typeMap = typeMap) {
            val navArgs: EditEquipment = it.toRoute()
            var locations by remember { mutableStateOf(emptyList<Location>()) }
            var equipment by remember { mutableStateOf(emptyList<Equipment>()) }
            LaunchedEffect(equipment) {
                locations = repo.getLocations()
                equipment = repo.getEquipment()
            }
            EditEquipmentPage(view = object : EditEquipmentPageViewModel() {
                override val startingName: String
                    get() = navArgs.name

                override val startingLocation: Location
                    get() = locations.first { it.id == navArgs.location }

                override val locations: List<Location>
                    get() = locations

                override fun submit(name: String, location: LocationId) {
                    viewModelScope.launch {
                        repo.updateEquipment(navArgs.id, name, location)
                    }
                    navController.popBackStack()
                }

                override fun redirectToCreateLocation() {
                    locationRedirect(viewModelScope, repo, navController)
                }

                override fun delete() {
                    viewModelScope.launch {
                        repo.deleteEquipment(navArgs.id)
                    }
                    navController.popBackStack()
                }
            })
        }
        composable<MuscleList> {
            var muscles by remember { mutableStateOf(emptyMap<MuscleId, MuscleSetHistory>()) }
            LaunchedEffect(muscles) {
                muscles = repo.setsForMusclesInWeek(Instant.now()).setCounts
            }
            MuscleListPage(view = object : MuscleListPageViewModel() {
                override val musclesWithSetCounts: Map<MuscleId, MuscleSetHistory>
                    get() = muscles

                override fun new() {
                    viewModelScope.launch {
                        val new = repo.newMuscle()
                        navController.navigate(EditMuscle(new.id, new.name))
                    }
                }

                override fun goto(value: Muscle) {
                    navController.navigate(EditMuscle(value.id, value.name))
                }
            })
        }
        composable<EditMuscle>(typeMap = typeMap) {
            val navArgs: EditMuscle = it.toRoute()
            EditMusclePage(view = object : EditMusclePageViewModel() {
                override val startingName: String
                    get() = navArgs.startingName

                override fun submit(name: String) {
                    viewModelScope.launch {
                        repo.updateMuscle(navArgs.muscleId, name)
                    }
                    navController.popBackStack()
                }

                override fun delete() {
                    viewModelScope.launch {
                        repo.deleteMuscle(navArgs.muscleId)
                    }
                    navController.popBackStack()
                }
            })
        }
        composable<ExerciseList> {
            var exercises by remember { mutableStateOf(emptyList<Exercise>()) }
            var muscles by remember { mutableStateOf(emptyList<Muscle>()) }
            LaunchedEffect(exercises, muscles) {
                exercises = repo.getExercises()
                muscles = repo.getMuscles()
            }
            ExerciseListPage(view = object : ExerciseListPageViewModel() {
                override val exercises: List<Exercise>
                    get() = exercises

                override fun gotoNew() {
                    viewModelScope.launch {
                        val new = repo.newExercise(muscles[0].id)
                        navController.navigate(EditExercise(new.id))
                    }
                }

                override fun goto(value: Exercise) {
                    navController.navigate(EditExercise(value.id))
                }

                override fun redirectToCreateMuscle() {
                    viewModelScope.launch {
                        val new = repo.newMuscle()

                        navController.navigate(EditMuscle(new.id, new.name))
                    }
                }

                override val muscles: List<Muscle>
                    get() = muscles
            })
        }
        composable<EditExercise>(typeMap = typeMap) {
            val navArgs: EditExercise = it.toRoute()
            EditExercisePage(
                view = EditExercisePageViewModelImpl(navArgs.id, repo, navController)
            )
        }
        composable<SetList> {
            RegisterSetListPage(repo, navController)
        }
        editSetGraph(navController, repo, typeMap)
        editVariationGraph(navController, repo, typeMap)
    }
}

fun locationRedirect(
    scope: CoroutineScope, repo: AppRepository, navController: NavController
) {
    scope.launch {
        val new = repo.newLocation()
        navController.navigate(EditLocation(new.name, new.id))
    }
}
