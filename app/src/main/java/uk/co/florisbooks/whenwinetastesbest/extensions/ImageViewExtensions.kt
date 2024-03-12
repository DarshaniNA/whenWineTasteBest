package uk.co.florisbooks.whenwinetastesbest.extensions

import android.graphics.Matrix
import android.widget.ImageView
import kotlin.math.roundToInt

fun ImageView.getScaledSize(): AdjustedBounds {
    // Get image dimensions
    // Get image matrix values and place them in an array
    val f = FloatArray(9)
    imageMatrix.getValues(f)

    // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
    val scaleX = f[Matrix.MSCALE_X]
    val scaleY = f[Matrix.MSCALE_Y]

    // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
    val d = drawable
    val origW = d.intrinsicWidth
    val origH = d.intrinsicHeight

    // Calculate the actual dimensions
    val actW = (origW * scaleX).roundToInt()
    val actH = (origH * scaleY).roundToInt()

    return AdjustedBounds(actW, actH)
}

data class AdjustedBounds(val width: Int, val height: Int)