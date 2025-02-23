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

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Face Mesh Detector Demo. */
class FaceMeshDetectorProcessor() {

  private val detector: FaceMeshDetector

  init {
    val optionsBuilder = FaceMeshDetectorOptions.Builder()

    detector = FaceMeshDetection.getClient(optionsBuilder.build())
  }

  suspend fun detectInImageMesh(bitmap: Bitmap): List<FaceMesh> {
    return withContext(Dispatchers.IO) {

      var detectedFaces = emptyList<FaceMesh>()
      val image = InputImage.fromBitmap(bitmap, 0)
      Log.d("Test", "Processing image: $image")

      try {
        // Use the detector asynchronously and wait for the result
        val task = detector.process(image) // This will suspend the coroutine until done
        // If faces are detected, assign them; otherwise, empty list
        val faceMesh = Tasks.await(task)
        // Log the number of faces detected
        Log.d("Test", "Faces detected: ${faceMesh.size}")

        detectedFaces = faceMesh ?: emptyList()
      } catch (e: Exception) {
        // Log any errors that occur during detection
        Log.e("Test", "Face detection failed", e)
      }
      detectedFaces

    }

  }


  companion object {
    private const val TAG = "SelfieFaceProcessor"
  }
}
