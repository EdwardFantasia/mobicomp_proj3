/*
 * Copyright 2022 Google LLC. All rights reserved.
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
package com.google.mlkit.vision.demo.kotlin.facemeshdetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import kotlin.math.max
import kotlin.math.min

/**
 * Graphic instance for rendering face position and mesh info within the associated graphic overlay
 * view.
 */
class FaceMeshGraphic(private val faceMesh: FaceMesh, private val imageWidth: Int, private val canvasWidth: Float)
  {

  private val positionPaint: Paint
  private val boxPaint: Paint
  private var zMin: Float
  private var zMax: Float

  @FaceMesh.ContourType
  private val DISPLAY_CONTOURS =
    intArrayOf(
      FaceMesh.FACE_OVAL,
      FaceMesh.LEFT_EYEBROW_TOP,
      FaceMesh.LEFT_EYEBROW_BOTTOM,
      FaceMesh.RIGHT_EYEBROW_TOP,
      FaceMesh.RIGHT_EYEBROW_BOTTOM,
      FaceMesh.LEFT_EYE,
      FaceMesh.RIGHT_EYE,
      FaceMesh.UPPER_LIP_TOP,
      FaceMesh.UPPER_LIP_BOTTOM,
      FaceMesh.LOWER_LIP_TOP,
      FaceMesh.LOWER_LIP_BOTTOM,
      FaceMesh.NOSE_BRIDGE
    )

  /** Draws the face annotations for position on the supplied canvas. */
  fun draw(canvas: Canvas) {

    // Draws the bounding box.
    val rect = RectF(faceMesh.boundingBox)
    // If the image is flipped, the left will be translated to right, and the right to left.
    val x0 = translateX(rect.left)
    val x1 = translateX(rect.right)
    rect.left = Math.min(x0, x1)
    rect.right = Math.max(x0, x1)
    rect.top = translateY(rect.top)
    rect.bottom = translateY(rect.bottom)
    canvas.drawRect(rect, boxPaint)

    // Draw face mesh
    val points = faceMesh.allPoints
    val triangles = faceMesh.allTriangles

    zMin = Float.MAX_VALUE
    zMax = Float.MIN_VALUE
    for (point in points) {
      zMin = Math.min(zMin, point.position.z)
      zMax = Math.max(zMax, point.position.z)
    }

    // Draw face mesh points
    for (point in points) {
      updatePaintColorByZValue(
        positionPaint,
        canvas,
        /* visualizeZ= */true,
        /* rescaleZForVisualization= */true,
        point.position.z,
        zMin,
        zMax)
      canvas.drawCircle(
        translateX(point.position.x),
        translateY(point.position.y),
        FACE_POSITION_RADIUS,
        positionPaint
      )
    }


      // Draw face mesh triangles
    for (triangle in triangles) {
      val point1 = triangle.allPoints[0].position
      val point2 = triangle.allPoints[1].position
      val point3 = triangle.allPoints[2].position
      drawLine(canvas, point1, point2)
      drawLine(canvas, point1, point3)
      drawLine(canvas, point2, point3)
    }
  }

  private fun drawLine(canvas: Canvas, point1: PointF3D, point2: PointF3D) {
    updatePaintColorByZValue(
      positionPaint,
      canvas,
      /* visualizeZ= */true,
      /* rescaleZForVisualization= */true,
      (point1.z + point2.z) / 2,
      zMin,
      zMax)
    canvas.drawLine(
      translateX(point1.x),
      translateY(point1.y),
      translateX(point2.x),
      translateY(point2.y),
      positionPaint
    )
  }

  private fun getContourPoints(faceMesh: FaceMesh): List<FaceMeshPoint> {
    val contourPoints: MutableList<FaceMeshPoint> = ArrayList()
    for (type in DISPLAY_CONTOURS) {
      contourPoints.addAll(faceMesh.getPoints(type))
    }
    return contourPoints
  }

  companion object {
    private const val USE_CASE_CONTOUR_ONLY = 999
    private const val FACE_POSITION_RADIUS = 8.0f
    private const val BOX_STROKE_WIDTH = 5.0f
  }

  init {
    val selectedColor = Color.WHITE
    positionPaint = Paint()
    positionPaint.color = selectedColor

    boxPaint = Paint()
    boxPaint.color = selectedColor
    boxPaint.style = Paint.Style.STROKE
    boxPaint.strokeWidth = BOX_STROKE_WIDTH

    zMin = java.lang.Float.MAX_VALUE
    zMax = java.lang.Float.MIN_VALUE
  }

    fun updatePaintColorByZValue(
      paint: Paint,
      canvas: Canvas,
      visualizeZ: Boolean,
      rescaleZForVisualization: Boolean,
      zInImagePixel: Float,
      zMin: Float,
      zMax: Float
    ) {
      if (!visualizeZ) {
        return
      }

      // When visualizeZ is true, sets up the paint to different colors based on z values.
      // Gets the range of z value.
      val zLowerBoundInScreenPixel: Float
      val zUpperBoundInScreenPixel: Float

      if (rescaleZForVisualization) {
        zLowerBoundInScreenPixel = min(-0.001, scale(zMin).toDouble()).toFloat()
        zUpperBoundInScreenPixel = max(0.001, scale(zMax).toDouble()).toFloat()
      } else {
        // By default, assume the range of z value in screen pixel is [-canvasWidth, canvasWidth].
        val defaultRangeFactor = 1f
        zLowerBoundInScreenPixel = -defaultRangeFactor * canvas.width
        zUpperBoundInScreenPixel = defaultRangeFactor * canvas.width
      }

      val zInScreenPixel = scale(zInImagePixel)

      if (zInScreenPixel < 0) {
        // Sets up the paint to be red if the item is in front of the z origin.
        // Maps values within [zLowerBoundInScreenPixel, 0) to [255, 0) and use it to control the
        // color. The larger the value is, the more red it will be.
        var v = (zInScreenPixel / zLowerBoundInScreenPixel * 255).toInt()
        v = v.coerceIn(0, 255)
        paint.setARGB(255, 255, 255 - v, 255 - v)
      } else {
        // Sets up the paint to be blue if the item is behind the z origin.
        // Maps values within [0, zUpperBoundInScreenPixel] to [0, 255] and use it to control the
        // color. The larger the value is, the more blue it will be.
        var v = (zInScreenPixel / zUpperBoundInScreenPixel * 255).toInt()
        v = v.coerceIn(0, 255)
        paint.setARGB(255, 255 - v, 255 - v, 255)
      }

    }



    // 1. Scale function - Adjusts the size from image to view coordinates.
    private fun scale(value: Float): Float {
      // Scale value relative to the width of the image and the width of the canvas
      return value * (canvasWidth / imageWidth)
    }

    // 2. Translate X - Converts the X coordinate from image to canvas.
    private fun translateX(x: Float): Float {
      // Scale the X value based on the canvas size
      return scale(x)
    }

    // 3. Translate Y - Converts the Y coordinate from image to canvas.
    private fun translateY(y: Float): Float {
      // Scale the Y value based on the canvas size
      return scale(y)
    }
}


