package com.example.project3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.project3.ui.theme.Project3Theme
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.project3.facedetector.FaceDetectorProcessor
import com.example.project3.facedetector.FaceGraphic
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasPermissions()){
            ActivityCompat.requestPermissions(
                this, CAMERA_PERMISSIONS, 0
            )
        }
        setContent {
            Project3Theme {
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE
                        )
                    }
                }

                var capturedImage = remember{
                    mutableStateOf<Bitmap?>(null)
                }

                var (selected, setSelected) = remember { mutableStateOf("") }

                //declared class with face detection info
                var faceDetectorProcessor = FaceDetectorProcessor()

                var detectedFaces by remember { mutableStateOf(emptyList<Face>()) }


                //when an image is captured, launch detection code
                LaunchedEffect(capturedImage.value) {
                    capturedImage.value?.let { bitmap ->
                        detectedFaces = faceDetectorProcessor.detectInImage(bitmap)
                    }
                }



                Log.d("Test", "It has begun")

                Column(
                    modifier = Modifier
                        .padding(PaddingValues()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp),
                        contentAlignment = Alignment.Center
                    ){
                        if(capturedImage.value != null){
                            Log.d("Test", "Hello!!!!")
                            Image(
                                bitmap = capturedImage.value!!.asImageBitmap(),
                                contentDescription = "Captured Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(2.dp)),
                                contentScale = ContentScale.Crop
                            )

                            //if a picture has been taken
                            if(detectedFaces.isNotEmpty()){
                                Log.d("Test", "Attempted to Draw Graphic")

                                //draws specific face graphic on canvas
                                //TODO: fix incorrect scaling/misplacement
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val f = FaceGraphic(detectedFaces[0], capturedImage.value!!.width,size.width )
                                    f.draw(drawContext.canvas.nativeCanvas)

                                }
                            }
                        //live camera
                        }else{
                            CameraPreview(
                                controller = controller,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(500.dp)
                            )


                        }
                    }

                    Spacer(modifier = Modifier
                        .height(50.dp)
                    )

                    Button(
                        modifier = Modifier
                            .width(400.dp)
                            .padding(vertical = 8.dp),
                        onClick = {
                            takePhoto(controller = controller, onPhotoTaken = { bitmap ->
                                capturedImage.value = bitmap
                            })
                        },
                        shape = RoundedCornerShape(1.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xff9910e3),
                            contentColor = Color.White
                        )
                    ){
                        Text("Take a picture", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier
                        .height(10.dp)
                    )
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(
                            selected = ("none" == selected),
                            onClick = {setSelected("none")},
                            modifier = Modifier.scale(1.5f)
                        )
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            text = "None")
                    }

                    Spacer(modifier = Modifier
                        .height(5.dp)
                    )
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(
                            selected = ("fd" == selected),
                            onClick = {setSelected("fd")},
                            modifier = Modifier.scale(1.5f)
                        )
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            text = "Face detection")
                    }
                    Spacer(modifier = Modifier
                        .height(5.dp)
                    )
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(
                            selected = ("cd" == selected),
                            onClick = {setSelected("cd")},
                            modifier = Modifier.scale(1.5f)
                        )
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            text = "Contour detection")
                    }

                    Spacer(modifier = Modifier
                        .height(5.dp)
                    )
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(
                            selected = ("md" == selected),
                            onClick = {setSelected("md")},
                            modifier = Modifier.scale(1.5f)
                        )
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            text = "Mesh detection")
                    }

                    Spacer(modifier = Modifier
                        .height(5.dp)
                    )
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(
                            selected = ("ss" == selected),
                            onClick = {setSelected("ss")},
                            modifier = Modifier.scale(1.5f)
                        )
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            text = "Selfie segmentation")
                    }
                }
            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ){
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext), object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val rotationDegrees = image.imageInfo.rotationDegrees

                    val originalBitmap = image.toBitmap()

                    val matrix = android.graphics.Matrix().apply{
                        postRotate(rotationDegrees.toFloat())
                    }

                    val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

                    image.close()
                    onPhotoTaken(rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera: ", "Couldn't take picture", exception)
                }

            }
        )
    }

    private fun hasPermissions(): Boolean{
        return CAMERA_PERMISSIONS.all{
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object{
        private val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier){
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}
