package com.example.eva3.appdevacaciones

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.eva3.appdevacaciones.viewmodels.VacationsViewModel

class MainActivity : ComponentActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val STORAGE_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicia solicitando los permisos
        checkAndRequestLocationPermission()

        setContent {
            VacationFormScreen()
        }
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            checkAndRequestStoragePermission()
        }
    }

    private fun checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de ubicación concedido, ahora pide los permisos de almacenamiento
                    Log.d("Permisos", "Permiso de ubicación concedido")
                    checkAndRequestStoragePermission()
                } else {
                    // Permiso de ubicación denegado
                    Log.d("Permisos", "Permiso de ubicación denegado")
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de almacenamiento concedido
                    Log.d("Permisos", "Permiso de almacenamiento concedido")
                } else {
                    // Permiso de almacenamiento denegado
                    Log.d("Permisos", "Permiso de almacenamiento denegado")
                }
            }
        }
    }
}

@Composable
fun VacationFormScreen() {
    val viewModel: VacationsViewModel = viewModel()

    // Recoger el estado de la ubicación del ViewModel
    val location by viewModel.location.collectAsState()
    val context = LocalContext.current
    val images by viewModel.images.collectAsState(initial = emptyList<Uri>())
    var imageToShow by remember { mutableStateOf<Uri?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = viewModel.placeName.value,
            onValueChange = { viewModel.placeName.value = it },
            label = { Text("Nombre del Lugar") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            Log.d("Aqui error", "boton de foto")
            viewModel.takePhoto(context) }) {
            Text("Agregar Foto")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow {
            items(images) { imageUri ->
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            imageToShow = imageUri
                        }
                )
            }
        }

        imageToShow?.let { uri ->
            FullScreenImageView(uri)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            Log.d("Aqui error", "boton de mapa")
            location?.let { location?.latitude?.let { it1 ->
                location!!.longitude.let { it2 ->
                    viewModel.showMap(context, it1, it2)
                }
            } }
        }) {
            Text(text = "Show Map")
        }

        Spacer(modifier = Modifier.height(8.dp))

        location?.let {
            Text("Ubicación: Latitud ${it.latitude}, Longitud ${it.longitude}")
        }
    }
}

@Composable
fun FullScreenImageView(uri: Uri) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Full Screen Image",
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = { /* Handle close */ },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
        }
    }
}