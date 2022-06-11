package com.example.fooddine

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fooddine.QrCodeScanner.QRCodeAnalyZer
import com.example.fooddine.ui.theme.FoodDineTheme
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodDineTheme {
                var code by remember {
                    mutableStateOf("")
                }
                var context= LocalContext.current

                val lifeCycleOwner= LocalLifecycleOwner.current

                var camerFunctionProvider = remember {
                    ProcessCameraProvider.getInstance(context)
                }

                var hasCamerPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        )==PackageManager.PERMISSION_GRANTED
                    )
                }

                val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { granted->
                    hasCamerPermission=granted
                })
                LaunchedEffect(key1 = true)
                {
                    launcher.launch(android.Manifest.permission.CAMERA)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (hasCamerPermission) {
                        AndroidView(factory = { context ->
                            val previewView = PreviewView(context)
                            val preview = androidx.camera.core.Preview.Builder().build()
                            val selector = CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                            var imageAnalysis = ImageAnalysis.Builder().setTargetResolution(
                                android.util.Size(
                                    previewView.width,
                                    previewView.height
                                )
                            ).setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST).build()
                            imageAnalysis.setAnalyzer(
                                ContextCompat.getMainExecutor(context), QRCodeAnalyZer { result ->
                                    code = result
                                }
                            )
                            try {
                                camerFunctionProvider.get().bindToLifecycle(
                                    lifeCycleOwner, selector, preview, imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            previewView
                        },
                        modifier = Modifier.weight(1f))
                        Text(text = code, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(35.dp), style = TextStyle(color = Color.Green))
                    }
                }
            }
        }
    }
}
