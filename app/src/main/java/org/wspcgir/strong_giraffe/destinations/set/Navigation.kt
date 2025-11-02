package org.wspcgir.strong_giraffe.destinations.set

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.wspcgir.strong_giraffe.destinations.EditExercise
import org.wspcgir.strong_giraffe.destinations.edit_variation.EditVariation
import org.wspcgir.strong_giraffe.model.Exercise
import org.wspcgir.strong_giraffe.model.variation.ExerciseVariation
import org.wspcgir.strong_giraffe.repository.AppRepository
import org.wspcgir.strong_giraffe.views.SelectionPage
import kotlin.reflect.KType


@Serializable
object EditPage
@Serializable
object ListPage
@Serializable
object SelectExercise
@Serializable
object SelectVariation


@Serializable
object SetPage

fun NavGraphBuilder.setGraph(
    navController: NavController,
    repo: AppRepository,
    typeMap: Map<KType, NavType<out Parcelable>>
) {
    navigation<SetPage>(startDestination = ListPage, typeMap = typeMap) {
        composable<ListPage>(typeMap = typeMap) {
            val parent = rememberParent(navController, it)
            val view = viewModel<SetListPageViewModel>(parent)
            LaunchedEffect(Unit) {
                view.init(repo, navController)
            }
            RegisterSetListPage(view)
        }
        navigation<EditSet>(startDestination = ListPage, typeMap = typeMap) {
            composable<EditPage>(typeMap = typeMap) {
                val parent = rememberParent(navController, it)
                val grandparent = rememberParent(navController, parent)
                val listView = viewModel<SetListPageViewModel>(grandparent)
                val navArgs = parent.toRoute<EditSet>()
                val view = viewModel<EditSetPageViewModel>(parent)
                LaunchedEffect(navArgs.id) {
                    Log.d("editSetGraph", "Initializing EditSet page view model")
                    view.init(repo, navController, navArgs.id, navArgs.locked)
                }
                EditSetPage(view)
            }
            composable<SelectExercise>() { entry ->
                val parent = rememberParent(navController, entry)
                val view = viewModel<EditSetPageViewModel>(parent)
                var exercises: List<Exercise> by remember { mutableStateOf(emptyList()) }
                LaunchedEffect(Unit) {
                    exercises = repo.getExercises()
                }
                SelectionPage(
                    items = exercises,
                    displayName = { it.name },
                    onSelect = { exercise ->
                        when (val data = view.data.value) {
                            is EditSetPageViewModel.Data.Loaded -> {
                                navController.popBackStack()
                                data.changeExercise(exercise.id, exercise.name)
                            }

                            else -> {}
                        }
                    },
                    onEdit = { exercise -> navController.navigate(EditExercise(id = exercise.id)) },
                    onCreateNew = {
                        view.viewModelScope.launch {
                            repo.getMuscles().firstOrNull()?.let {
                                val new = repo.newExercise(it.id)
                                navController.navigate(EditExercise(new.id))
                            }
                        }
                    }
                )
            }
            composable<SelectVariation> { entry ->
                val parent = rememberParent(navController, entry)
                val view = viewModel<EditSetPageViewModel>(parent)
                var variations: List<ExerciseVariation> by remember { mutableStateOf(emptyList()) }
                val data = view.data.value
                LaunchedEffect(data) {
                    when (data) {
                        is EditSetPageViewModel.Data.Loaded -> {
                            variations =
                                repo.getVariationsForExercise(data.inProgress.value.exercise)
                        }

                        else -> {}
                    }
                }
                SelectionPage(
                    items = variations,
                    displayName = { it.name },
                    onSelect = { variation ->
                        when (data) {
                            is EditSetPageViewModel.Data.Loaded -> {
                                data.changeVariation(variation.id, variation.name)
                                navController.popBackStack()
                            }

                            else -> {}
                        }
                    },
                    onEdit = { variation -> navController.navigate(EditVariation(id = variation.id)) },
                    onCreateNew = {
                        view.viewModelScope.launch {
                            when (data) {
                                is EditSetPageViewModel.Data.Loaded -> {
                                    val new =
                                        repo.newExerciseVariation(data.inProgress.value.exercise)
                                    navController.navigate(EditVariation(new.id))
                                }

                                else -> {}
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun rememberParent(
    navController: NavController,
    entry: NavBackStackEntry
): NavBackStackEntry {
    return remember(entry) {
        navController.getBackStackEntry<EditSet>()
    }
}