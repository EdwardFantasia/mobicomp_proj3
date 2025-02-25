package com.google.mlkit.vision.demo.kotlin.segmenter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.google.mlkit.vision.segmentation.SegmentationMask

class SegmentationGraphic(
  private val segmentationMask: SegmentationMask,
  private val imageWidth: Int,          // Original image width
  private val imageHeight: Int,         // Original image height
  private val canvasWidth: Float,       // Canvas width (box width)
  private val canvasHeight: Float       // Canvas height (box height)
) {
  private val paint: Paint = Paint().apply {
    style = Paint.Style.FILL
    color = Color.argb(100, 0, 255, 0) // Semi-transparent green
  }

  fun draw(canvas: Canvas) {
    val mask = segmentationMask.buffer
    val maskWidth = segmentationMask.width
    val maskHeight = segmentationMask.height

    // Calculate the scaling factor to fit the image within the canvas (box)
    val imageAspectRatio = imageWidth.toFloat() / imageHeight
    val canvasAspectRatio = canvasWidth / canvasHeight

    val scale: Float
    if (imageAspectRatio > canvasAspectRatio) {
      // Image is wider than the canvas, scale based on width
      scale = canvasWidth / imageWidth
    } else {
      // Image is taller than the canvas, scale based on height
      scale = canvasHeight / imageHeight
    }

    // Calculate the scaled image dimensions
    val scaledImageWidth = imageWidth * scale
    val scaledImageHeight = imageHeight * scale

    // Calculate the offset to center the scaled image within the canvas
    val offsetX = (canvasWidth - scaledImageWidth) / 2f
    val offsetY = (canvasHeight - scaledImageHeight) / 2f

    // Draw the segmentation mask
    mask.rewind()
    for (y in 0 until maskHeight) {
      for (x in 0 until maskWidth) {
        val confidence = mask.float
        if (confidence > 0.5) {
          // Scale and position the mask to match the scaled image
          val left = x * (scaledImageWidth / maskWidth) + offsetX
          val top = y * (scaledImageHeight / maskHeight) + offsetY
          val right = (x + 1) * (scaledImageWidth / maskWidth) + offsetX
          val bottom = (y + 1) * (scaledImageHeight / maskHeight) + offsetY
          canvas.drawRect(RectF(left, top, right, bottom), paint)
        }
      }
    }
  }
}