package com.example.project3.facedetector

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Face Detector Demo.  */
class FaceDetectorProcessor(){



  private val detector: FaceDetector

  init {
    //settings specific to FaceDetector
    val options = FaceDetectorOptions.Builder()
      .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
      .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
      .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
      .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
      .build()

    detector = FaceDetection.getClient(options)
  }

  fun stop() {
    detector.close()
  }

  suspend fun detectInImage(bitmap: Bitmap): List<Face> {
    return withContext(Dispatchers.IO) {

      var detectedFaces = emptyList<Face>()
      val image = InputImage.fromBitmap(bitmap, 0)
      Log.d("Test", "Processing image: $image")

      try {
        // Use the detector asynchronously and wait for the result
        val task = detector.process(image) // This will suspend the coroutine until done
        // If faces are detected, assign them; otherwise, empty list
        val faces = Tasks.await(task)
        // Log the number of faces detected
        Log.d("Test", "Faces detected: ${faces.size}")

        detectedFaces = faces ?: emptyList()
      } catch (e: Exception) {
        // Log any errors that occur during detection
        Log.e("Test", "Face detection failed", e)
      }
      detectedFaces
    }

  }



}
