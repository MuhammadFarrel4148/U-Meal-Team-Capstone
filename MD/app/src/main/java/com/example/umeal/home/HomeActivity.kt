package com.example.umeal.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.umeal.R
import com.example.umeal.databinding.ActivityHomeBinding
import com.example.umeal.home.ui.scan.ScanImageViewModel
import com.example.umeal.home.ui.scan.ScanImageViewModelFactory
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.home.ui.scan.ScanResult
import com.example.umeal.utils.PreferenceManager
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var currentPhotoPath: String
    private var selectedFile: File? = null
    private lateinit var viewModel: ScanImageViewModel

    private lateinit var preferenceManager: PreferenceManager

    private val requestCameraAndStoragePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
        val readExternalStoragePermissionGranted = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        } else {
            true
        }

        if (cameraPermissionGranted && readExternalStoragePermissionGranted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(this, "Camera and storage permissions are required to use this feature", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.hide()

        currentPhotoPath = ""

        preferenceManager = PreferenceManager(this)

        initializeViewModel()
        setupNavigation()

        binding.buttonScan.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog()
            } else {
                requestCameraAndStoragePermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(options) { _: DialogInterface, which: Int ->
                when (which) {
                    0 -> startTakePhoto()
                    1 -> startGallery()
                }
            }
            .show()
    }
    private fun initializeViewModel() {
        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = ScanImageViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, factory)[ScanImageViewModel::class.java]
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startTakePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            createCustomTempFile(application).also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.file provider",
                    it
                )
                currentPhotoPath = it.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(takePictureIntent)
            }
        }
    }

    @SuppressLint("IntentReset")
    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        launcherIntentGallery.launch(intent)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            selectedFile = uriToFile(selectedImg, this)
            startCrop(selectedImg)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            selectedFile = myFile
            val uri = Uri.fromFile(myFile)
            startCrop(uri)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
        }
        UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                val intent = Intent(this, ScanResult::class.java).apply {
                    putExtra("CROPPED_IMAGE_URI", it.toString())
                }
                startActivity(intent)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            UCrop.getError(data!!)?.printStackTrace()
        }
    }

    private val FILENAME_FORMAT = "dd-MMM-yyyy"

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        contentResolver.openInputStream(selectedImg)?.use { inputStream ->
            FileOutputStream(myFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
        return myFile
    }
}