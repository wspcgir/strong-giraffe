package org.wspcgir.strong_giraffe.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.wspcgir.strong_giraffe.model.Intensity
import org.wspcgir.strong_giraffe.model.Reps
import org.wspcgir.strong_giraffe.model.Weight
import org.wspcgir.strong_giraffe.ui.theme.StrongGiraffeTheme

@Composable
fun PreviousSetButton(
    reps: Reps,
    weight: Weight,
    intensity: Intensity,
    onClick: () -> Unit
){
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = intensityColor(intensity)
        ),
        onClick = onClick
    ) {
        Text("$reps | $weight")
    }
}

@Composable
@Preview(name = "LightMode")
private fun PreviousSetButtonPreview(){
    StrongGiraffeTheme {
        LazyColumn {
            this.items(Intensity.range) { intensity ->
                PreviousSetButton(Reps(10), Weight(100f), intensity) { }
            }
        }
    }
}