package com.bhavdip.animatedButton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.bhavdip.animatedButton.ui.theme.AnimatedButtonTheme
import com.bhavdip.animatedButton.ui.theme.Purple80
import com.bhavdip.animatedButton.ui.theme.PurpleGrey80
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sign


private const val ICON_ALPHA_INITIAL = 0.5f
private const val BACKGROUND_ALPHA_INITIAL = 0.7f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimatedButtonTheme {
                // A surface container using the 'background' color from the theme

                val modifier = Modifier
                Surface(
                    modifier = modifier.fillMaxSize(),
                    color = colorScheme.primary
                ) {
                    ButtonView(modifier = modifier)
                }
            }
        }
    }
}

@Composable
fun ButtonView(
    modifier: Modifier = Modifier
) {

    var containerWidth by remember {
        mutableStateOf(0f)
    }

    var valueCounter by remember {
        mutableStateOf(0)
    }

    Box(
        contentAlignment = Alignment.Center, modifier = modifier
    ) {


        val offsetX = remember { Animatable(0f) }

        ContainerView(modifier, onRemoveClick = {
            valueCounter = maxOf(valueCounter - 1, 0)
        }, onAddClick = {
            valueCounter += 1
        }, onSizeChanged = {
            containerWidth = it
        }, offsetX = offsetX.value)

        if (containerWidth > 0) {
            DraggableButton(
                valueCounter.toString(),
                onClick = {},
                modifier = modifier,
                dragOffset = containerWidth,
                onAddClick = {
                    valueCounter += 1
                },
                onRemoveClick = {
                    valueCounter = maxOf(valueCounter - 1, 0)
                },
                offsetX = offsetX
            )
        }

    }
}

private const val CONTAINER_OFFSET_FACTOR = 0.2f

@Composable
fun ContainerView(
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit,
    onAddClick: () -> Unit,
    onSizeChanged: (size: Float) -> Unit,
    offsetX: Float
) {
    Row(
        modifier = modifier
            .offset {
                IntOffset(
                    (offsetX * CONTAINER_OFFSET_FACTOR).toInt(),
                    0
                )
            }
            .width(250.dp)
            .height(60.dp)
            .onGloballyPositioned {
                onSizeChanged(it.size.toSize().width)
            }
            .clip(RoundedCornerShape(30.dp))
            .background(color = Purple80.copy(alpha = BACKGROUND_ALPHA_INITIAL))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButtonView(
            icon = Icons.Filled.Remove,
            contentDescription = "Decrement",
            tintColor = Color.Black.copy(alpha = ICON_ALPHA_INITIAL),
            onClick = {
                onRemoveClick()
            })

        IconButtonView(
            icon = Icons.Filled.Add,
            contentDescription = "Increment",
            tintColor = Color.Black.copy(alpha = ICON_ALPHA_INITIAL),
            onClick = {
                onAddClick()
            })
    }

}


@Composable
fun DraggableButton(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    dragOffset: Float,
    offsetX: Animatable<Float, AnimationVector1D>,
) {
    val dragLimit = dragOffset / 0.9.dp.dpToPx()

//    val dragLimit = 70.dp.dpToPx()

    val coroutineScope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset {
                IntOffset(
                    offsetX.value.toInt(),
                    0
                )
            }
            .shadow(10.dp, shape = CircleShape)
            .size(44.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(PurpleGrey80)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach {
                            coroutineScope.launch {

                                val targetValue =
                                    offsetX.value + it.positionChange().x

                                val targetValueWithinBounds = targetValue.coerceIn(
                                    -dragLimit,
                                    dragLimit
                                )
                                offsetX.snapTo(targetValueWithinBounds)
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (offsetX.value.absoluteValue >= dragLimit) {
                        if (offsetX.value.sign > 0) {
                            onAddClick()
                        } else {
                            onRemoveClick()
                        }
                    }
                    coroutineScope.launch {
                        if (offsetX.value != 0f) {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = StiffnessLow
                                )
                            )
                        }
                    }
                }
            }
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }

}

@Composable
fun IconButtonView(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tintColor: Color = Color.White,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(32.dp)
        )
    }
}


@Composable
private fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AnimatedButtonTheme {
        ButtonView()
    }
}