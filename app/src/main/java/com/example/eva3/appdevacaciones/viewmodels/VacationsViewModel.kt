package com.example.eva3.appdevacaciones.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import android.content.Intent

class VacationsViewModel : ViewModel() {

    val placeName = mutableStateOf("")

    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images: StateFlow<List<Uri>> = _images

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> get() = _location

    var imageCapture: ImageCapture? = null

    fun takePhoto(context: Context) {
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "${System.currentTimeMillis()}.jpg"
                        )
                    ).build()
                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = outputFileResults.savedUri
                                if (savedUri != null) {
                                    val updatedImages =
                                        _images.value.toMutableList().apply { add(savedUri) }
                                    _images.value = updatedImages
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("ImageCaptureException", "Error al capturar imagen")
                            }
                        })
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("ImageCaptureException", "Error al iniciar captura de imagen")
                }
            })
    }

    fun showMap(context: Context, latitude: Double, longitude: Double) {
        val geoUri = Uri.parse("geo:$latitude,$longitude")

        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            Log.e("ActivacionMapa", "Error al obtener actividad del mapa")
        }
    }
    fun getLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            viewModelScope.launch {
                try {
                    val locationResult = Tasks.await(fusedLocationClient.lastLocation)
                    _location.value = locationResult
                } catch (e: SecurityException) {
                    Log.e("GetLocation", "Error al obtener la locacion: ${e.message}")
                } catch (e: Exception) {
                    Log.e("GetLocation", "Error: ${e.message}")
                }
            }
        } else {
            Log.e("GetLocation", "No se tienen los permisos necesarios para obtener la locacion")
        }
    }

}