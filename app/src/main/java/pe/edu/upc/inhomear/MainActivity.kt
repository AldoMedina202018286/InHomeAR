package pe.edu.upc.inhomear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import pe.edu.upc.inhomear.ui.theme.InHomeARTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InHomeARTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val currentModel = remember {
                            mutableStateOf("table")
                        }
                        ARScreen(currentModel.value)
                        Menu(modifier = Modifier.align(Alignment.BottomCenter)) {
                            currentModel.value = it
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Menu(
    modifier: Modifier,
    onClick: (String) -> Unit
) {
    var currentIndex by remember {
        mutableStateOf(0)
    }

    val itemList = listOf(
        Furniture("table", R.drawable.table),
        Furniture("chair", R.drawable.chair),
        Furniture("armchair", R.drawable.armchair),
        Furniture("desk", R.drawable.desk),
    )

    fun updateIndex(offset: Int) {
        currentIndex = (currentIndex + offset + itemList.size) % itemList.size
        onClick(itemList[currentIndex].name)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton( onClick = { updateIndex(-1) } ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_keyboard_arrow_left_24),
                contentDescription = "Previous"
            )
        }

        FurnitureCard(imageId = itemList[currentIndex].imageId)

        IconButton( onClick = { updateIndex(1) } ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_keyboard_arrow_right_24),
                contentDescription = "Next"
            )
        }
    }
}

@Composable
fun FurnitureCard(
    modifier: Modifier = Modifier,
    imageId: Int
) {
    Box(
        modifier = modifier
            .size(140.dp)
            .clip(CircleShape)
            .border(width = 1.dp, Color.LightGray, CircleShape)
    ) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = "Furniture Image",
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun ARScreen(model: String) {
    val nodes = remember {
        mutableListOf<ArNode>()
    }

    val modelNode = remember {
        mutableStateOf<ArModelNode?>(null)
    }

    val placeModelButton = remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer.isShadowReceiver = false
                modelNode.value = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = 0.8f
                    ) {

                    }
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = { node, hitResult ->
                        placeModelButton.value = node.isTracking
                    }
                }
                nodes.add(modelNode.value!!)
            },
            onSessionCreate = {
                planeRenderer.isVisible = false
            }
        )
        if (placeModelButton.value) {
            Button(onClick = {
                modelNode.value?.anchor()
            }, modifier = Modifier.align(Alignment.BottomCenter)) {
                Text("Place Model")
            }
        }

        LaunchedEffect(key1 = model) {
            modelNode.value?.loadModelGlbAsync(
                glbFileLocation = "models/${model}.glb",
                scaleToUnits = 0.8f
            )
            Log.e("Error loading model", "Error loading model")
        }
    }
}

data class Furniture(
    val name: String,
    val imageId: Int
)