package org.wspcgir.strong_giraffe.destinations.edit_variation

import android.os.Parcelable
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
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.wspcgir.strong_giraffe.destinations.EditLocation
import org.wspcgir.strong_giraffe.model.Location
import org.wspcgir.strong_giraffe.repository.AppRepository
import org.wspcgir.strong_giraffe.views.SelectionPage
import kotlin.reflect.KType

@Serializable
object EditPage
@Serializable
object SelectLocation

fun NavGraphBuilder.editVariationGraph(
    navController: NavController,
    repo: AppRepository,
    typeMap: Map<KType, NavType<out Parcelable>>
) {
    navigation<EditVariation>(startDestination = EditPage, typeMap = typeMap) {
        composable<EditPage>(typeMap = typeMap) {
            val parent = rememberParent(navController, it)
            val navArgs = parent.toRoute<EditVariation>()
            val view = viewModel<EditVariationPageViewModel>(parent)
            LaunchedEffect(navArgs.id) {
                view.init(repo, navController, navArgs.id)
            }
            EditVariationPage(view)
        }
        composable<SelectLocation>(typeMap = typeMap) { entry ->
            val parent = rememberParent(navController, entry)
            val view = viewModel<EditVariationPageViewModel>(parent)
            var locations: List<Location> by remember { mutableStateOf(emptyList()) }
            LaunchedEffect(Unit) {
                locations = repo.getLocations()
            }
            when(val loaded = view.data.value) {
                is EditVariationPageViewModel.Data.Loaded -> {
                    SelectionPage(
                        items = locations,
                        displayName = { it.name },
                        onEdit = { navController.navigate(EditLocation(it.name, it.id)) },
                        onSelect = {
                            loaded.updateVariationLocation(it.id, it.name)
                            navController.popBackStack()
                        },
                        onCreateNew = { view.viewModelScope.launch { repo.newLocation() }},
                    )
                }
                else -> { }
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
        navController.getBackStackEntry<EditVariation>()
    }
}