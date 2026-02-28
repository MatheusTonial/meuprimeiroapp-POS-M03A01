package com.example.meuprimeiroapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.meuprimeiroapp.databinding.ActivityNewItemBinding
import com.example.meuprimeiroapp.model.ItemLocation
import com.example.meuprimeiroapp.model.ItemValue
import com.example.meuprimeiroapp.service.Result
import com.example.meuprimeiroapp.service.RetrofitClient
import com.example.meuprimeiroapp.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import java.util.Date
import java.util.Locale

class NewItemActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var binding: ActivityNewItemBinding
    private lateinit var mMap : GoogleMap
    private var selectedMarker: Marker? = null

    private lateinit var imageUri: Uri

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == RESULT_OK){
            binding.imageUrl.setText("Imagem obtida")
        }
        else{

        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        requestLocationPermission()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLong  ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions().position(latLong)
                    .draggable(true)
                    .title("lat: ${latLong.latitude}, long: ${latLong.longitude}")
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadCurrentLocation()
                }
                else{
                    Toast.makeText(this, R.string.permissao_de_localizacao_negada, Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera()
                }

            }
        }
    }

    private fun setupView(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener { saveItem() }
        binding.takePictureCta.setOnClickListener { takePicture() }
    }

    private fun takePicture(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            openCamera()
        }
        else {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri{
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(this, "com.example.meuprimeiroapp.fileprovider", imageFile)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationPermission(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //se o user permitiu localizacao, pega a ultima localizacao
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val currentLocation = location?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            }
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getDeviceLocation(){
        //verifica permissao de localizacao
        if(ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            loadCurrentLocation()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        if (!::mMap.isInitialized) {
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        fusedLocationClient
    }

    private fun saveItem(){
        if(!validateForm()){
            return
        }
        val itemPosition = selectedMarker?.position?.let {
            ItemLocation(
                name = binding.name.text.toString(),
                it.latitude,
                it.longitude
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()

            val itemValue = ItemValue(
                id = id,
                name = binding.name.text.toString(),
                surname = binding.surname.text.toString(),
                profession = binding.profession.text.toString(),
                imageUrl = binding.imageUrl.text.toString(),
                age = binding.age.text.toString().toInt(),
                location = itemPosition
            )

            val result = safeApiCall { RetrofitClient.apiService.addItem(itemValue) }
            withContext(Dispatchers.Main){
                when(result){
                    is Result.Success -> handleOnSuccess()
                    is Result.Error -> handleOnError()
                }
            }
        }
    }

    private fun handleOnError() {
        Toast.makeText(this, R.string.erro_add_item, Toast.LENGTH_SHORT).show()
    }

    private fun handleOnSuccess() {
        Toast.makeText(this, R.string.sucesso_add_item, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun validateForm(): Boolean {
        var hasError = false
        if(binding.name.text.isNullOrBlank()){
            binding.name.error = getString(R.string.campo_obrigatorio)
            hasError = true
        }
        if(binding.surname.text.isNullOrBlank()) {
            binding.surname.error = getString(R.string.campo_obrigatorio)
            hasError = true
        }
        if(binding.age.text.isNullOrBlank()) {
            binding.age.error = getString(R.string.campo_obrigatorio)
            hasError = true
        }

        return !hasError
    }



    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002

        fun newIntent(context: Context): Intent {
            return Intent(context, NewItemActivity::class.java)
        }

    }
}