/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.common.compose

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.Providers
import androidx.compose.ambientOf
import androidx.compose.getValue
import androidx.compose.onCommit
import androidx.compose.setValue
import androidx.compose.state
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.ViewAmbient
import androidx.ui.core.composed
import androidx.ui.geometry.Rect
import androidx.ui.layout.absolutePadding
import androidx.ui.unit.Density
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

/**
 * Main holder of our inset values.
 *
 * TODO add other inset types (IME, visibility, etc)
 */
data class ComposeInsets(
    val systemBars: Rect = Rect.zero
)

val InsetsAmbient = ambientOf { ComposeInsets() }

@Composable
fun ProvideInsets(children: @Composable () -> Unit) {
    val view = ViewAmbient.current
    var insetsHolder by state { ComposeInsets() }

    onCommit(view.id) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            insetsHolder = insets.toComposeInsets()
            // Return the unconsumed insets
            insets
        }

        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

    Providers(InsetsAmbient provides insetsHolder, children = children)
}

fun Modifier.systemBarsPadding(all: Boolean = false) = systemBarsPadding(all, all, all, all)

fun Modifier.systemBarsPadding(
    left: Boolean = true,
    top: Boolean = true,
    right: Boolean = true,
    bottom: Boolean = true
) = composed {
    val systemBars = InsetsAmbient.current.systemBars
    val padding = systemBars.toAbsoluteInnerPadding(DensityAmbient.current)
    absolutePadding(padding, left, top, right, bottom)
}

/**
 * Allows conditional setting of [padding] on each dimension.
 */
fun Modifier.absolutePadding(
    padding: AbsoluteInnerPadding,
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) = absolutePadding(
    left = if (left) padding.left else 0.dp,
    top = if (top) padding.top else 0.dp,
    right = if (right) padding.right else 0.dp,
    bottom = if (bottom) padding.bottom else 0.dp
)

/**
 * Allows conditional setting of [padding] on each dimension.
 */
fun Modifier.absolutePadding(padding: AbsoluteInnerPadding) = absolutePadding(
    left = padding.left,
    top = padding.top,
    right = padding.right,
    bottom = padding.bottom
)

private fun Insets.toRect() = Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

private fun WindowInsetsCompat.toComposeInsets() = ComposeInsets(
    systemBars = systemWindowInsets.toRect()
)

private fun Rect.toAbsoluteInnerPadding(density: Density) = with(density) {
    AbsoluteInnerPadding(left.toDp(), top.toDp(), right.toDp(), bottom.toDp())
}

@Immutable
data class AbsoluteInnerPadding(
    val left: Dp = 0.dp,
    val top: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp
) {
    constructor(all: Dp) : this(all, all, all, all)
}
