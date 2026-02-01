package org.wspcgir.strong_giraffe.views

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme

data class SortingOnGroup<T>(
    val groupAndSort: (List<T>) -> Map<String, List<T>>,
)

fun <T> alphabeticOrdering(displayName: (T) -> String): SortingOnGroup<T> {
    return SortingOnGroup { xs ->
        xs.groupBy({ displayName(it).take(1).uppercase() }, { it })
            .toSortedMap()
            .mapValues { it.value.sortedBy { item -> displayName(item) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionPage(
    items: List<T>,
    displayName: (T) -> String,
    onEdit: (T) -> Unit,
    onSelect: (T) -> Unit,
    onCreateNew: () -> Unit,
    orderings: Map<String, SortingOnGroup<T>> = emptyMap()
) {
    var selectedOrdering: SortingOnGroup<T> by remember {
        mutableStateOf(
            orderings.firstNotNullOfOrNull { it.value }
                ?: alphabeticOrdering(displayName)
        )
    }
    val groups = selectedOrdering.groupAndSort(items)
    ModalDrawerScaffold(
        title = "Exercise",
        drawerContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrderingDropdown(orderings) { ordering ->
                    orderings[ordering]?.let { selectedOrdering = it }
                }
            }
        },
        actionButton = {
            FloatingActionButton(onClick = onCreateNew) {
                Icon(Icons.Filled.Add, contentDescription = "create new")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
            LazyColumn(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                groups.forEach { (key, values) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(key, fontSize = 30.sp)
                            }
                        }
                    }
                    values.forEach { value ->
                        item {
                            Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                                ExerciseRow(
                                    item = value,
                                    displayName = displayName,
                                    onSelect = onSelect,
                                    onEdit = onEdit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> OrderingDropdown(orderings: Map<String, SortingOnGroup<T>>, onSelect: (String) -> Unit) {
    if (orderings.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Sort By", fontSize = 30.sp)
        }
        orderings.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { onSelect(entry.key) }) { Text(entry.key) }
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("No Sorting Available", fontSize = 30.sp)
        }
    }
}

@Composable
fun <T> ExerciseRow(
    item: T,
    displayName: (T) -> String,
    onSelect: (T) -> Unit,
    onEdit: (T) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Left,
        modifier = Modifier.padding(2.dp)
    ) {
        Column() {
            IconButton(
                onClick = { onSelect(item) },
                modifier = Modifier.width(40.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = "Select ")
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(0.8f)) {
            Text(displayName(item), fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(0.1f)) {
            IconButton(
                onClick = { onEdit(item) }
            ) {
                Icon(
                    Icons.Filled.Create,
                    contentDescription = "edit button",
                    modifier = Modifier.scale(1.0f)
                )
            }
        }
    }
}

@Composable
@Preview(
    name = "Dark",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
private fun Preview() {
    StrongGiraffeTheme {
        SelectionPage(
            items = listOf(
                "Crunches",
                "Bicep Curls",
                "Squats",
                "Lat Pulldowns",
                "Dips",
                "Reverse Fly",
                "Dead Lift",
                "Tricep Extensions",
                "Trap Shrugs",
                "MR Life Fitness Black"
            ),
            displayName = { it },
            onEdit = { },
            onSelect = { },
            onCreateNew = { },
            orderings = mapOf(
                "alphabetic" to alphabeticOrdering { it },
                "rankings" to SortingOnGroup {
                    it
                        .groupBy(
                            {
                                if (listOf("Dead Lift", "Squats").contains(it)) {
                                    "Awesome"
                                } else {
                                    "Lame"
                                }
                            },
                            { name -> name }
                        )
                        .toSortedMap()
                        .mapValues { entry -> entry.value.sortedBy { name -> name } }
                },
            )
        )
    }
}